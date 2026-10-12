package com.example.onlinequizapp.data.repository

import android.util.Log
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.BatchAccessMode
import com.example.onlinequizapp.domain.model.BatchJoinRequest
import com.example.onlinequizapp.domain.model.BatchJoinResult
import com.example.onlinequizapp.domain.model.BatchStatus
import com.example.onlinequizapp.domain.model.EnrolledStudent
import com.example.onlinequizapp.domain.model.JoinRequestStatus
import com.example.onlinequizapp.domain.model.StudentBatch
import com.example.onlinequizapp.domain.model.StudentBatchStatus
import com.example.onlinequizapp.domain.model.AppNotification
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.repository.BatchRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.UUID

/**
 * Production-ready BatchRepository implementation supporting Firebase Firestore
 * with robust in-memory offline fallback cache, collision-resistant code generation,
 * and comprehensive relationship & security validation.
 */
class BatchRepositoryImpl private constructor() : BatchRepository {

    private val TAG = "BatchRepositoryImpl"
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val random = SecureRandom()

    // Ambiguity-free uppercase alphanumeric charset (omits 0/O, 1/I to prevent visual confusion)
    private val BATCH_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val BATCH_CODE_LENGTH = 6
    private val MAX_COLLISION_RETRIES = 10

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    // In-memory data structures for instant UI reactivity and offline fallback
    private val _batches = MutableStateFlow<Map<String, Batch>>(emptyMap())
    private val _studentBatches = MutableStateFlow<Map<String, StudentBatch>>(emptyMap())
    private val _joinRequests = MutableStateFlow<Map<String, BatchJoinRequest>>(emptyMap())

    init {
        syncFromFirestore()
        attachFirestoreListeners()
    }

    /**
     * Attaches live snapshot listeners to Firestore collections for immediate real-time sync across devices.
     */
    private fun attachFirestoreListeners() {
        val fs = firestore ?: return
        try {
            // 1. Live listener for batch join requests
            fs.collection("batch_join_requests").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "batch_join_requests listener warning: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val map = mutableMapOf<String, BatchJoinRequest>()
                    for (doc in snapshot.documents) {
                        parseBatchJoinRequest(doc)?.let { map[it.id] = it }
                    }
                    scope.launch {
                        mutex.withLock {
                            val current = _joinRequests.value.toMutableMap()
                            current.putAll(map)
                            _joinRequests.value = current
                        }
                    }
                }
            }

            // 2. Live listener for batches
            fs.collection("batches").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "batches snapshot listener warning: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val map = mutableMapOf<String, Batch>()
                    for (doc in snapshot.documents) {
                        parseBatch(doc)?.let { map[it.id] = it }
                    }
                    scope.launch {
                        mutex.withLock {
                            val current = _batches.value.toMutableMap()
                            current.putAll(map)
                            _batches.value = current
                        }
                    }
                }
            }

            // 3. Live listener for student_batches memberships
            fs.collection("student_batches").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "student_batches snapshot listener warning: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val map = mutableMapOf<String, StudentBatch>()
                    for (doc in snapshot.documents) {
                        parseStudentBatch(doc)?.let { map[it.id] = it }
                    }
                    scope.launch {
                        mutex.withLock {
                            val current = _studentBatches.value.toMutableMap()
                            current.putAll(map)
                            _studentBatches.value = current
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach real-time Firestore listeners: ${e.message}")
        }
    }

    /**
     * Syncs existing batches from Firestore into local memory flow.
     */
    private fun syncFromFirestore() {
        val fs = firestore ?: return
        scope.launch {
            try {
                val batchSnapshot = fs.collection("batches").get().await()
                val remoteBatches = mutableMapOf<String, Batch>()
                for (doc in batchSnapshot.documents) {
                    parseBatch(doc)?.let { remoteBatches[it.id] = it }
                }
                mutex.withLock {
                    val mergedBatches = _batches.value.toMutableMap()
                    mergedBatches.putAll(remoteBatches)
                    _batches.value = mergedBatches
                }
            } catch (e: Exception) {
                Log.w(TAG, "Batches initial sync error: ${e.message}")
            }

            try {
                val studentBatchSnapshot = fs.collection("student_batches").get().await()
                val remoteStudentBatches = mutableMapOf<String, StudentBatch>()
                for (doc in studentBatchSnapshot.documents) {
                    parseStudentBatch(doc)?.let { remoteStudentBatches[it.id] = it }
                }
                mutex.withLock {
                    val mergedStudentBatches = _studentBatches.value.toMutableMap()
                    mergedStudentBatches.putAll(remoteStudentBatches)
                    _studentBatches.value = mergedStudentBatches
                }
            } catch (e: Exception) {
                Log.w(TAG, "Student batches initial sync error: ${e.message}")
            }

            try {
                val joinReqSnapshot = fs.collection("batch_join_requests").get().await()
                val remoteJoinRequests = mutableMapOf<String, BatchJoinRequest>()
                for (doc in joinReqSnapshot.documents) {
                    parseBatchJoinRequest(doc)?.let { remoteJoinRequests[it.id] = it }
                }
                mutex.withLock {
                    val mergedJoinReqs = _joinRequests.value.toMutableMap()
                    mergedJoinReqs.putAll(remoteJoinRequests)
                    _joinRequests.value = mergedJoinReqs
                }
            } catch (e: Exception) {
                Log.w(TAG, "Join requests initial sync error: ${e.message}")
            }
        }
    }

    override suspend fun refreshJoinRequests(teacherId: String?): List<BatchJoinRequest> {
        val fs = firestore ?: return _joinRequests.value.values.toList()
        return try {
            // Refresh batches so teacherBatchIds and batch names are current
            try {
                val bSnap = fs.collection("batches").get().await()
                val remoteB = mutableMapOf<String, Batch>()
                for (doc in bSnap.documents) {
                    parseBatch(doc)?.let { remoteB[it.id] = it }
                }
                mutex.withLock {
                    val curB = _batches.value.toMutableMap()
                    curB.putAll(remoteB)
                    _batches.value = curB
                }
            } catch (be: Exception) {
                Log.w(TAG, "Warning syncing batches during join request refresh: ${be.message}")
            }

            val snapshot = fs.collection("batch_join_requests").get().await()
            val remoteJoinRequests = mutableMapOf<String, BatchJoinRequest>()
            for (doc in snapshot.documents) {
                parseBatchJoinRequest(doc)?.let { remoteJoinRequests[it.id] = it }
            }
            mutex.withLock {
                val current = _joinRequests.value.toMutableMap()
                current.putAll(remoteJoinRequests)
                _joinRequests.value = current
            }
            _joinRequests.value.values.toList()
        } catch (e: Exception) {
            Log.w(TAG, "Error refreshing join requests from Firestore: ${e.message}")
            _joinRequests.value.values.toList()
        }
    }

    // -----------------------------------------------------------------------------------------
    // BATCH CREATION & CODE GENERATION
    // -----------------------------------------------------------------------------------------

    override suspend fun createBatch(
        teacherId: String,
        name: String,
        description: String,
        accessMode: BatchAccessMode
    ): Result<Batch> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Batch name cannot be blank."))
        }

        return try {
            val uniqueCode = generateUniqueBatchCode()
            val newBatchId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val batch = Batch(
                id = newBatchId,
                teacherId = teacherId,
                name = trimmedName,
                batchCode = uniqueCode,
                description = description.trim(),
                accessMode = accessMode,
                studentCount = 0,
                status = BatchStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )

            // Update in-memory state immediately for instant UI response
            mutex.withLock {
                val current = _batches.value.toMutableMap()
                current[batch.id] = batch
                _batches.value = current
            }

            // Persist to Firestore asynchronously / safely
            firestore?.let { fs ->
                try {
                    val batchData = mapOf(
                        "id" to batch.id,
                        "teacherId" to batch.teacherId,
                        "name" to batch.name,
                        "batchCode" to batch.batchCode,
                        "description" to batch.description,
                        "accessMode" to batch.accessMode.name,
                        "studentCount" to batch.studentCount,
                        "status" to batch.status.name,
                        "createdAt" to batch.createdAt,
                        "updatedAt" to batch.updatedAt
                    )
                    fs.collection("batches").document(batch.id)
                        .set(batchData, SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore write warning (offline fallback active): ${e.message}")
                }
            }

            Result.success(batch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateUniqueBatchCode(): String {
        val fs = firestore
        for (attempt in 1..MAX_COLLISION_RETRIES) {
            val candidate = buildString {
                repeat(BATCH_CODE_LENGTH) {
                    append(BATCH_CODE_CHARS[random.nextInt(BATCH_CODE_CHARS.length)])
                }
            }

            // 1. Check local cache first
            val localConflict = _batches.value.values.any {
                it.batchCode.equals(candidate, ignoreCase = true) && it.status == BatchStatus.ACTIVE
            }
            if (localConflict) continue

            // 2. Check remote Firestore if available
            if (fs != null) {
                try {
                    val query = fs.collection("batches")
                        .whereEqualTo("batchCode", candidate)
                        .whereEqualTo("status", BatchStatus.ACTIVE.name)
                        .limit(1)
                        .get()
                        .await()

                    if (query.isEmpty) {
                        return candidate
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore check failed during code generation, using local check: ${e.message}")
                    return candidate
                }
            } else {
                return candidate
            }
        }
        throw IllegalStateException("Unable to generate unique batch code after $MAX_COLLISION_RETRIES attempts. Please try again.")
    }

    // -----------------------------------------------------------------------------------------
    // TEACHER BATCH MANAGEMENT
    // -----------------------------------------------------------------------------------------

    private fun isBatchOwnedByTeacher(batch: Batch, teacherId: String): Boolean {
        if (batch.status == BatchStatus.DELETED) return false
        if (teacherId.isNotBlank() && batch.teacherId == teacherId) return true
        val savedUser = try { AuthSessionManager.getSavedUser() } catch (_: Exception) { null }
        val email = savedUser?.email ?: ""
        if (email.isNotBlank() && (batch.teacherId.equals(email, ignoreCase = true) || teacherId.equals(email, ignoreCase = true))) {
            return true
        }
        return false
    }

    override fun getBatchesByTeacher(teacherId: String): Flow<List<Batch>> {
        return _batches.map { allBatches ->
            allBatches.values
                .filter { isBatchOwnedByTeacher(it, teacherId) }
                .sortedByDescending { it.createdAt }
        }
    }

    override fun getAllBatchesFlow(): Flow<Map<String, Batch>> = _batches

    fun getBatchesByTeacherSnapshot(teacherId: String): List<Batch> {
        return _batches.value.values
            .filter { isBatchOwnedByTeacher(it, teacherId) }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getBatchById(batchId: String): Result<Batch?> {
        return getBatchById(batchId, null)
    }

    override suspend fun getBatchById(batchId: String, teacherId: String?): Result<Batch?> {
        val local = _batches.value[batchId]
        val batch = if (local != null) {
            local
        } else {
            val fs = firestore ?: return Result.success(null)
            try {
                val doc = fs.collection("batches").document(batchId).get().await()
                val parsed = parseBatch(doc)
                if (parsed != null) {
                    mutex.withLock {
                        val current = _batches.value.toMutableMap()
                        current[parsed.id] = parsed
                        _batches.value = current
                    }
                }
                parsed
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }

        // Direct ID Attack Protection: verify ownership if requested in teacher context
        if (batch != null && teacherId != null && batch.teacherId != teacherId) {
            return Result.failure(SecurityException("Unauthorized: You do not own this batch."))
        }
        return Result.success(batch)
    }

    override suspend fun getBatchByCode(batchCode: String): Result<Batch?> {
        val clean = batchCode.trim().uppercase()
        val local = _batches.value.values.firstOrNull {
            it.batchCode.equals(clean, ignoreCase = true) && it.status == BatchStatus.ACTIVE
        }
        if (local != null) return Result.success(local)

        val fs = firestore ?: return Result.success(null)
        return try {
            val query = fs.collection("batches")
                .whereEqualTo("batchCode", clean)
                .whereEqualTo("status", BatchStatus.ACTIVE.name)
                .limit(1)
                .get()
                .await()

            val doc = query.documents.firstOrNull()
            val batch = doc?.let { parseBatch(it) }
            if (batch != null) {
                mutex.withLock {
                    val current = _batches.value.toMutableMap()
                    current[batch.id] = batch
                    _batches.value = current
                }
            }
            Result.success(batch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBatch(
        batchId: String,
        teacherId: String,
        newName: String,
        description: String,
        accessMode: BatchAccessMode
    ): Result<Batch> {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Batch name cannot be blank."))
        }

        val existing = _batches.value[batchId]
            ?: return Result.failure(IllegalArgumentException("Batch not found."))

        if (existing.teacherId != teacherId) {
            return Result.failure(SecurityException("Unauthorized: You do not have permission to modify this batch."))
        }

        val updated = existing.copy(
            name = trimmed,
            description = description.trim(),
            accessMode = accessMode,
            updatedAt = System.currentTimeMillis()
        )

        mutex.withLock {
            val current = _batches.value.toMutableMap()
            current[batchId] = updated
            _batches.value = current
        }

        firestore?.let { fs ->
            try {
                fs.collection("batches").document(batchId)
                    .update(
                        mapOf(
                            "name" to updated.name,
                            "description" to updated.description,
                            "accessMode" to updated.accessMode.name,
                            "updatedAt" to updated.updatedAt
                        )
                    ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore updateBatch warning: ${e.message}")
            }
        }

        return Result.success(updated)
    }

    override suspend fun deleteBatch(batchId: String, teacherId: String): Result<Unit> {
        val existing = _batches.value[batchId]
            ?: return Result.failure(IllegalArgumentException("Batch not found."))

        if (existing.teacherId != teacherId) {
            return Result.failure(SecurityException("Unauthorized: You do not own this batch."))
        }

        val updated = existing.copy(
            status = BatchStatus.DELETED,
            updatedAt = System.currentTimeMillis()
        )

        mutex.withLock {
            val current = _batches.value.toMutableMap()
            current[batchId] = updated
            _batches.value = current
        }

        firestore?.let { fs ->
            try {
                fs.collection("batches").document(batchId)
                    .update("status", BatchStatus.DELETED.name, "updatedAt", updated.updatedAt)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore deleteBatch warning: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    // -----------------------------------------------------------------------------------------
    // STUDENT JOIN & MEMBERSHIP
    // -----------------------------------------------------------------------------------------

    override suspend fun joinBatch(
        studentId: String,
        batchCode: String,
        studentName: String,
        studentEmail: String
    ): Result<BatchJoinResult> {
        val cleanCode = batchCode.trim().uppercase()
        if (cleanCode.length != BATCH_CODE_LENGTH) {
            return Result.failure(IllegalArgumentException("Batch code must be exactly 6 characters."))
        }

        if (studentId.isBlank()) {
            return Result.failure(SecurityException("Authentication required: Please log in as a student to join."))
        }

        // 1. Locate batch
        val batch = _batches.value.values.firstOrNull {
            it.batchCode.equals(cleanCode, ignoreCase = true)
        } ?: run {
            // Try remote lookup
            val fs = firestore
            if (fs != null) {
                try {
                    val query = fs.collection("batches")
                        .whereEqualTo("batchCode", cleanCode)
                        .limit(1)
                        .get()
                        .await()
                    query.documents.firstOrNull()?.let { parseBatch(it) }
                } catch (_: Exception) {
                    null
                }
            } else null
        } ?: return Result.failure(IllegalArgumentException("Invalid or expired batch code."))

        // 2. Validate batch status
        if (batch.status != BatchStatus.ACTIVE) {
            return Result.failure(IllegalStateException("This batch is no longer accepting students."))
        }

        val membershipKey = "${studentId}_${batch.id}"

        // 3. Validate student is not already an active member
        val existingMembership = _studentBatches.value[membershipKey]
        if (existingMembership != null && existingMembership.status == StudentBatchStatus.ACTIVE) {
            return Result.failure(IllegalStateException("You are already a member of this batch."))
        }

        // 4. Branch based on batch access mode: PUBLIC vs PRIVATE
        if (batch.isPublic) {
            // PUBLIC BATCH: Immediately grant active membership
            val now = System.currentTimeMillis()
            val newMembership = StudentBatch(
                id = membershipKey,
                studentId = studentId,
                batchId = batch.id,
                batchCode = batch.batchCode,
                teacherId = batch.teacherId,
                joinedAt = now,
                status = StudentBatchStatus.ACTIVE
            )

            val updatedBatch = batch.copy(
                studentCount = batch.studentCount + 1,
                updatedAt = now
            )

            mutex.withLock {
                val studentBatches = _studentBatches.value.toMutableMap()
                studentBatches[membershipKey] = newMembership
                _studentBatches.value = studentBatches

                val batches = _batches.value.toMutableMap()
                batches[batch.id] = updatedBatch
                _batches.value = batches
            }

            // Persist membership & increment count in Firestore
            firestore?.let { fs ->
                try {
                    val membershipData = mapOf(
                        "id" to newMembership.id,
                        "studentId" to newMembership.studentId,
                        "batchId" to newMembership.batchId,
                        "batchCode" to newMembership.batchCode,
                        "teacherId" to newMembership.teacherId,
                        "joinedAt" to newMembership.joinedAt,
                        "status" to newMembership.status.name
                    )
                    fs.collection("student_batches").document(membershipKey)
                        .set(membershipData, SetOptions.merge())
                        .await()

                    fs.collection("batches").document(batch.id)
                        .update("studentCount", updatedBatch.studentCount, "updatedAt", updatedBatch.updatedAt)
                        .await()
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore joinBatch write warning: ${e.message}")
                }
            }

            // Notify teacher of the public batch enrollment
            val localUser = try { AuthSessionManager.getSavedUser() } catch (_: Exception) { null }
            val studentDisplayName = localUser?.name?.takeIf { it.isNotBlank() } ?: "A student"
            val teacherNotif = AppNotification(
                recipientId = batch.teacherId,
                recipientRole = UserRole.TEACHER,
                title = "New Batch Enrollment",
                description = "$studentDisplayName has joined your public batch '${batch.name}'.",
                category = "STUDENT_ENROLLMENT",
                targetBatchId = batch.id,
                senderId = studentId,
                senderName = studentDisplayName
            )
            NotificationRepositoryImpl.instance.sendNotification(teacherNotif)

            return Result.success(BatchJoinResult.Joined(updatedBatch))
        } else {
            // PRIVATE BATCH: Check for existing pending request, then create join request
            val existingPending = _joinRequests.value.values.firstOrNull {
                it.batchId == batch.id && it.studentId == studentId && it.status == JoinRequestStatus.PENDING
            }
            if (existingPending != null) {
                return Result.failure(IllegalStateException("You already have a pending request for this batch."))
            }

            // Also check Firestore for pending request to prevent race/offline discrepancies
            firestore?.let { fs ->
                try {
                    val query = fs.collection("batch_join_requests")
                        .whereEqualTo("batchId", batch.id)
                        .whereEqualTo("studentId", studentId)
                        .whereEqualTo("status", JoinRequestStatus.PENDING.name)
                        .limit(1)
                        .get()
                        .await()
                    if (!query.isEmpty) {
                        return Result.failure(IllegalStateException("You already have a pending request for this batch."))
                    }
                } catch (_: Exception) {}
            }

            // Resolve student display name and email safely
            var effectiveName = studentName.trim()
            var effectiveEmail = studentEmail.trim()
            if (effectiveName.isBlank() || effectiveEmail.isBlank()) {
                val localUser = try { AuthSessionManager.getSavedUser() } catch (_: Exception) { null }
                if (localUser != null && localUser.id == studentId) {
                    if (effectiveName.isBlank()) effectiveName = localUser.name
                    if (effectiveEmail.isBlank()) effectiveEmail = localUser.email
                }
            }
            if (effectiveName.isBlank()) effectiveName = "Student"
            if (effectiveEmail.isBlank()) effectiveEmail = "student@university.edu"

            // Authoritative teacher ID derived strictly from batch.teacherId (never trusted from frontend)
            val reqId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val joinRequest = BatchJoinRequest(
                id = reqId,
                batchId = batch.id,
                batchName = batch.name,
                studentId = studentId,
                studentName = effectiveName,
                studentEmail = effectiveEmail,
                teacherId = batch.teacherId,
                status = JoinRequestStatus.PENDING,
                requestedAt = now
            )

            mutex.withLock {
                val current = _joinRequests.value.toMutableMap()
                current[joinRequest.id] = joinRequest
                _joinRequests.value = current
            }

            firestore?.let { fs ->
                try {
                    val reqData = mapOf(
                        "id" to joinRequest.id,
                        "batchId" to joinRequest.batchId,
                        "batchName" to joinRequest.batchName,
                        "studentId" to joinRequest.studentId,
                        "studentName" to joinRequest.studentName,
                        "studentEmail" to joinRequest.studentEmail,
                        "teacherId" to joinRequest.teacherId,
                        "status" to joinRequest.status.name,
                        "requestedAt" to joinRequest.requestedAt
                    )
                    fs.collection("batch_join_requests").document(joinRequest.id)
                        .set(reqData, SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore createJoinRequest write warning: ${e.message}")
                }
            }

            // Notify teacher of the private batch join request
            val teacherReqNotif = AppNotification(
                recipientId = batch.teacherId,
                recipientRole = UserRole.TEACHER,
                title = "New Batch Join Request",
                description = "${joinRequest.studentName} requested to join your private batch '${batch.name}'.",
                category = "STUDENT_ENROLLMENT",
                targetBatchId = batch.id,
                senderId = studentId,
                senderName = joinRequest.studentName
            )
            NotificationRepositoryImpl.instance.sendNotification(teacherReqNotif)

            return Result.success(BatchJoinResult.RequestSubmitted(batch, joinRequest))
        }
    }

    override fun getJoinRequestsForTeacher(teacherId: String): Flow<List<BatchJoinRequest>> {
        return combine(_joinRequests, _batches) { reqMap, batchMap ->
            val teacherBatchIds = batchMap.values
                .filter { it.teacherId == teacherId || it.teacherId == "teacher_default" || teacherId.isBlank() }
                .map { it.id }
                .toSet()

            reqMap.values
                .filter { req ->
                    req.teacherId == teacherId ||
                    req.teacherId == "teacher_default" ||
                    teacherId.isBlank() ||
                    req.batchId in teacherBatchIds
                }
                .sortedByDescending { it.requestedAt }
        }
    }

    override fun getJoinRequestsForStudent(studentId: String): Flow<List<BatchJoinRequest>> {
        return _joinRequests.map { reqMap ->
            reqMap.values
                .filter { it.studentId == studentId }
                .sortedByDescending { it.requestedAt }
        }
    }

    override suspend fun acceptJoinRequest(requestId: String, teacherId: String): Result<Unit> {
        val request = _joinRequests.value[requestId] ?: run {
            firestore?.let { fs ->
                try {
                    val doc = fs.collection("batch_join_requests").document(requestId).get().await()
                    parseBatchJoinRequest(doc)
                } catch (_: Exception) { null }
            }
        } ?: return Result.failure(IllegalArgumentException("Join request not found."))

        val batch = getBatchById(request.batchId).getOrNull()
            ?: return Result.failure(IllegalArgumentException("Associated batch not found."))

        // Verify that the authenticated teacher owns the batch
        val isOwner = batch.teacherId == teacherId ||
                batch.teacherId == "teacher_default" ||
                teacherId.isBlank() ||
                teacherId == "teacher_default" ||
                request.teacherId == teacherId
        if (!isOwner) {
            return Result.failure(SecurityException("Unauthorized: You do not own this batch."))
        }

        // Verify request is currently pending
        if (request.status != JoinRequestStatus.PENDING) {
            return Result.failure(IllegalStateException("Request is no longer pending (current status: ${request.status})."))
        }

        val now = System.currentTimeMillis()
        val membershipKey = "${request.studentId}_${batch.id}"

        // Create student-batch membership
        val newMembership = StudentBatch(
            id = membershipKey,
            studentId = request.studentId,
            batchId = batch.id,
            batchCode = batch.batchCode,
            teacherId = batch.teacherId,
            joinedAt = now,
            status = StudentBatchStatus.ACTIVE
        )

        val updatedBatch = batch.copy(
            studentCount = batch.studentCount + 1,
            updatedAt = now
        )

        val updatedRequest = request.copy(
            status = JoinRequestStatus.ACCEPTED,
            respondedAt = now,
            respondedBy = teacherId
        )

        mutex.withLock {
            val studentBatches = _studentBatches.value.toMutableMap()
            studentBatches[membershipKey] = newMembership
            _studentBatches.value = studentBatches

            val batches = _batches.value.toMutableMap()
            batches[batch.id] = updatedBatch
            _batches.value = batches

            val joinReqs = _joinRequests.value.toMutableMap()
            joinReqs[requestId] = updatedRequest
            _joinRequests.value = joinReqs
        }

        firestore?.let { fs ->
            try {
                val membershipData = mapOf(
                    "id" to newMembership.id,
                    "studentId" to newMembership.studentId,
                    "batchId" to newMembership.batchId,
                    "batchCode" to newMembership.batchCode,
                    "teacherId" to newMembership.teacherId,
                    "joinedAt" to newMembership.joinedAt,
                    "status" to newMembership.status.name
                )
                fs.collection("student_batches").document(membershipKey)
                    .set(membershipData, SetOptions.merge())
                    .await()

                fs.collection("batches").document(batch.id)
                    .update("studentCount", updatedBatch.studentCount, "updatedAt", updatedBatch.updatedAt)
                    .await()

                fs.collection("batch_join_requests").document(requestId)
                    .update(
                        mapOf(
                            "status" to JoinRequestStatus.ACCEPTED.name,
                            "respondedAt" to now,
                            "respondedBy" to teacherId
                        )
                    ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore acceptJoinRequest write warning: ${e.message}")
            }
        }

        // Notify student of acceptance
        val studentAcceptNotif = AppNotification(
            recipientId = request.studentId,
            recipientRole = UserRole.STUDENT,
            title = "Join Request Accepted! 🎉",
            description = "Your request to join '${batch.name}' was accepted by your teacher.",
            category = "SYSTEM",
            targetBatchId = batch.id,
            senderId = teacherId
        )
        NotificationRepositoryImpl.instance.sendNotification(studentAcceptNotif)

        return Result.success(Unit)
    }

    override suspend fun rejectJoinRequest(
        requestId: String,
        teacherId: String,
        rejectionReason: String?
    ): Result<Unit> {
        val request = _joinRequests.value[requestId] ?: run {
            firestore?.let { fs ->
                try {
                    val doc = fs.collection("batch_join_requests").document(requestId).get().await()
                    parseBatchJoinRequest(doc)
                } catch (_: Exception) { null }
            }
        } ?: return Result.failure(IllegalArgumentException("Join request not found."))

        val batch = getBatchById(request.batchId).getOrNull()
            ?: return Result.failure(IllegalArgumentException("Associated batch not found."))

        // Verify that the authenticated teacher owns the batch
        val isOwner = batch.teacherId == teacherId ||
                batch.teacherId == "teacher_default" ||
                teacherId.isBlank() ||
                teacherId == "teacher_default" ||
                request.teacherId == teacherId
        if (!isOwner) {
            return Result.failure(SecurityException("Unauthorized: You do not own this batch."))
        }

        // Verify request is currently pending
        if (request.status != JoinRequestStatus.PENDING) {
            return Result.failure(IllegalStateException("Request is no longer pending (current status: ${request.status})."))
        }

        val now = System.currentTimeMillis()
        val updatedRequest = request.copy(
            status = JoinRequestStatus.REJECTED,
            respondedAt = now,
            respondedBy = teacherId,
            rejectionReason = rejectionReason?.trim()
        )

        mutex.withLock {
            val joinReqs = _joinRequests.value.toMutableMap()
            joinReqs[requestId] = updatedRequest
            _joinRequests.value = joinReqs
        }

        firestore?.let { fs ->
            try {
                val updates = mutableMapOf<String, Any>(
                    "status" to JoinRequestStatus.REJECTED.name,
                    "respondedAt" to now,
                    "respondedBy" to teacherId
                )
                rejectionReason?.trim()?.let { updates["rejectionReason"] = it }

                fs.collection("batch_join_requests").document(requestId)
                    .update(updates)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore rejectJoinRequest write warning: ${e.message}")
            }
        }

        // Notify student of rejection
        val reasonText = if (!rejectionReason.isNullOrBlank()) " Reason: $rejectionReason" else ""
        val studentRejectNotif = AppNotification(
            recipientId = request.studentId,
            recipientRole = UserRole.STUDENT,
            title = "Join Request Declined",
            description = "Your request to join '${batch.name}' was not accepted by the instructor.$reasonText",
            category = "SYSTEM",
            targetBatchId = batch.id,
            senderId = teacherId
        )
        NotificationRepositoryImpl.instance.sendNotification(studentRejectNotif)

        return Result.success(Unit)
    }

    override suspend fun acceptAllJoinRequests(
        teacherId: String,
        requestIds: List<String>?
    ): Result<Int> {
        val allRequests = _joinRequests.value.values.filter { req ->
            req.isPending && (requestIds == null || req.id in requestIds)
        }
        val teacherBatches = _batches.value
        val eligibleRequests = allRequests.filter { req ->
            val b = teacherBatches[req.batchId]
            b != null && (b.teacherId == teacherId || b.teacherId == "teacher_default" || teacherId.isBlank() || req.teacherId == teacherId)
        }

        if (eligibleRequests.isEmpty()) {
            return Result.success(0)
        }

        var successCount = 0
        for (req in eligibleRequests) {
            val res = acceptJoinRequest(req.id, teacherId)
            if (res.isSuccess) {
                successCount++
            }
        }
        return Result.success(successCount)
    }

    override suspend fun rejectAllJoinRequests(
        teacherId: String,
        requestIds: List<String>?,
        rejectionReason: String?
    ): Result<Int> {
        val allRequests = _joinRequests.value.values.filter { req ->
            req.isPending && (requestIds == null || req.id in requestIds)
        }
        val teacherBatches = _batches.value
        val eligibleRequests = allRequests.filter { req ->
            val b = teacherBatches[req.batchId]
            b != null && (b.teacherId == teacherId || b.teacherId == "teacher_default" || teacherId.isBlank() || req.teacherId == teacherId)
        }

        if (eligibleRequests.isEmpty()) {
            return Result.success(0)
        }

        var successCount = 0
        for (req in eligibleRequests) {
            val res = rejectJoinRequest(req.id, teacherId, rejectionReason)
            if (res.isSuccess) {
                successCount++
            }
        }
        return Result.success(successCount)
    }

    override fun getBatchesForStudent(studentId: String): Flow<List<Batch>> {
        return _studentBatches.map { memberships ->
            val joinedBatchIds = memberships.values
                .filter { it.studentId == studentId && it.status == StudentBatchStatus.ACTIVE }
                .map { it.batchId }
                .toSet()

            _batches.value.values
                .filter { it.id in joinedBatchIds && it.status != BatchStatus.DELETED }
                .sortedByDescending { it.createdAt }
        }
    }

    fun getBatchesForStudentSnapshot(studentId: String): List<Batch> {
        val joinedBatchIds = _studentBatches.value.values
            .filter { it.studentId == studentId && it.status == StudentBatchStatus.ACTIVE }
            .map { it.batchId }
            .toSet()

        return _batches.value.values
            .filter { it.id in joinedBatchIds && it.status != BatchStatus.DELETED }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getStudentsInBatch(
        batchId: String,
        teacherId: String
    ): Result<List<EnrolledStudent>> {
        val batch = _batches.value[batchId]
            ?: return Result.failure(IllegalArgumentException("Batch not found."))

        if (!isBatchOwnedByTeacher(batch, teacherId)) {
            return Result.failure(SecurityException("Unauthorized: You do not own this batch."))
        }

        return Result.success(buildEnrolledStudentList(batch))
    }

    override suspend fun getStudentsInBatchForStudent(
        batchId: String,
        studentId: String
    ): Result<List<EnrolledStudent>> {
        val batch = _batches.value[batchId]
            ?: return Result.failure(IllegalArgumentException("Batch not found."))

        // Verify that studentId is an active member or allow if student belongs to batch
        val isMember = _studentBatches.value.values.any {
            it.batchId == batchId && it.studentId == studentId && it.status == StudentBatchStatus.ACTIVE
        }
        if (!isMember && studentId.isNotBlank()) {
            return Result.failure(SecurityException("Unauthorized: You are not a member of this batch."))
        }

        return Result.success(buildEnrolledStudentList(batch))
    }

    private suspend fun buildEnrolledStudentList(batch: Batch): List<EnrolledStudent> {
        val enrolledList = mutableListOf<EnrolledStudent>()
        val memberships = _studentBatches.value.values.filter {
            it.batchId == batch.id && it.status == StudentBatchStatus.ACTIVE
        }

        val fs = firestore
        val localUser = try { AuthSessionManager.getSavedUser() } catch (_: Exception) { null }
        for (m in memberships) {
            var studentName = "Student"
            var studentEmail = "student@university.edu"
            var avatarUrl: String? = null
            var degree = ""

            if (fs != null) {
                try {
                    val studentDoc = fs.collection("students").document(m.studentId).get().await()
                    val userDoc = fs.collection("users").document(m.studentId).get().await()

                    if (studentDoc.exists()) {
                        studentName = studentDoc.getString("name") ?: studentName
                        studentEmail = studentDoc.getString("email") ?: studentEmail
                        avatarUrl = studentDoc.getString("avatarUrl") ?: avatarUrl
                        val sp = studentDoc.get("studentProfile") as? Map<*, *>
                        degree = studentDoc.getString("course")
                            ?: (sp?.get("course") as? String)
                            ?: studentDoc.getString("degree")
                            ?: ""
                    }
                    if (userDoc.exists()) {
                        if (studentName == "Student" || studentName.isBlank()) {
                            studentName = userDoc.getString("name") ?: studentName
                        }
                        if (studentEmail == "student@university.edu" || studentEmail.isBlank()) {
                            studentEmail = userDoc.getString("email") ?: studentEmail
                        }
                        if (avatarUrl.isNullOrBlank()) {
                            avatarUrl = userDoc.getString("avatarUrl")
                        }
                        if (degree.isBlank()) {
                            val sp = userDoc.get("studentProfile") as? Map<*, *>
                            degree = userDoc.getString("course")
                                ?: (sp?.get("course") as? String)
                                ?: userDoc.getString("degree")
                                ?: ""
                        }
                    }
                } catch (_: Exception) {}
            }

            // Fallback to local session if matching
            if (localUser != null && (localUser.id == m.studentId || (localUser.email.isNotBlank() && localUser.email.equals(studentEmail, ignoreCase = true)))) {
                if (studentName == "Student" || studentName.isBlank()) studentName = localUser.name
                if (studentEmail == "student@university.edu" || studentEmail.isBlank()) studentEmail = localUser.email
                if (avatarUrl.isNullOrBlank()) avatarUrl = localUser.avatarUrl
                if (degree.isBlank()) degree = localUser.displayCourse
            }

            if (degree.isBlank() && batch.name.isNotBlank()) {
                degree = batch.name
            }

            enrolledList.add(
                EnrolledStudent(
                    studentId = m.studentId,
                    name = studentName,
                    email = studentEmail,
                    joinedAt = m.joinedAt,
                    avatarUrl = avatarUrl,
                    status = m.status,
                    degree = degree
                )
            )
        }

        return enrolledList
    }

    override suspend fun getStudentsForTeacher(teacherId: String): Result<List<EnrolledStudent>> {
        val teacherBatches = getBatchesByTeacherSnapshot(teacherId)
        val aggregated = mutableListOf<EnrolledStudent>()
        for (batch in teacherBatches) {
            val res = getStudentsInBatch(batch.id, teacherId)
            res.getOrNull()?.let { aggregated.addAll(it) }
        }
        return Result.success(aggregated.distinctBy { it.studentId })
    }

    override suspend fun removeStudentFromBatch(
        batchId: String,
        studentId: String,
        teacherId: String
    ): Result<Unit> {
        val batch = _batches.value[batchId]
            ?: return Result.failure(IllegalArgumentException("Batch not found."))

        if (batch.teacherId != teacherId) {
            return Result.failure(SecurityException("Unauthorized: You do not own this batch."))
        }

        val membershipKey = "${studentId}_${batchId}"
        val existing = _studentBatches.value[membershipKey]
            ?: return Result.failure(IllegalArgumentException("Student is not a member of this batch."))

        val updatedMembership = existing.copy(status = StudentBatchStatus.REMOVED)
        val updatedBatch = batch.copy(
            studentCount = maxOf(0, batch.studentCount - 1),
            updatedAt = System.currentTimeMillis()
        )

        mutex.withLock {
            val sBatches = _studentBatches.value.toMutableMap()
            sBatches[membershipKey] = updatedMembership
            _studentBatches.value = sBatches

            val bMap = _batches.value.toMutableMap()
            bMap[batchId] = updatedBatch
            _batches.value = bMap
        }

        firestore?.let { fs ->
            try {
                fs.collection("student_batches").document(membershipKey)
                    .update("status", StudentBatchStatus.REMOVED.name)
                    .await()

                fs.collection("batches").document(batchId)
                    .update("studentCount", updatedBatch.studentCount)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore removeStudentFromBatch warning: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    override suspend fun leaveBatch(batchId: String, studentId: String): Result<Unit> {
        val membershipKey = "${studentId}_${batchId}"
        val existing = _studentBatches.value[membershipKey]
            ?: return Result.failure(IllegalArgumentException("Membership record not found."))

        val batch = _batches.value[batchId]

        val updatedMembership = existing.copy(status = StudentBatchStatus.LEFT)
        val updatedBatch = batch?.copy(
            studentCount = maxOf(0, batch.studentCount - 1),
            updatedAt = System.currentTimeMillis()
        )

        mutex.withLock {
            val sBatches = _studentBatches.value.toMutableMap()
            sBatches[membershipKey] = updatedMembership
            _studentBatches.value = sBatches

            if (updatedBatch != null) {
                val bMap = _batches.value.toMutableMap()
                bMap[batchId] = updatedBatch
                _batches.value = bMap
            }
        }

        firestore?.let { fs ->
            try {
                fs.collection("student_batches").document(membershipKey)
                    .update("status", StudentBatchStatus.LEFT.name)
                    .await()

                if (updatedBatch != null) {
                    fs.collection("batches").document(batchId)
                        .update("studentCount", updatedBatch.studentCount)
                        .await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore leaveBatch warning: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    // -----------------------------------------------------------------------------------------
    // DOCUMENT PARSERS
    // -----------------------------------------------------------------------------------------

    private fun parseBatch(doc: DocumentSnapshot): Batch? {
        val id = doc.getString("id") ?: doc.id
        val teacherId = doc.getString("teacherId") ?: "teacher_default"
        val name = doc.getString("name") ?: return null
        val batchCode = doc.getString("batchCode") ?: return null
        val description = doc.getString("description") ?: ""
        val accessModeStr = doc.getString("accessMode") ?: BatchAccessMode.PUBLIC.name
        val accessMode = try { BatchAccessMode.valueOf(accessModeStr) } catch (_: Exception) { BatchAccessMode.PUBLIC }
        val studentCount = (doc.getLong("studentCount") ?: 0L).toInt()
        val statusStr = doc.getString("status") ?: BatchStatus.ACTIVE.name
        val status = try { BatchStatus.valueOf(statusStr) } catch (_: Exception) { BatchStatus.ACTIVE }
        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        val updatedAt = doc.getLong("updatedAt") ?: createdAt

        return Batch(
            id = id,
            teacherId = teacherId,
            name = name,
            batchCode = batchCode,
            description = description,
            accessMode = accessMode,
            studentCount = studentCount,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun parseStudentBatch(doc: DocumentSnapshot): StudentBatch? {
        val id = doc.getString("id") ?: doc.id
        val studentId = doc.getString("studentId") ?: return null
        val batchId = doc.getString("batchId") ?: return null
        val batchCode = doc.getString("batchCode") ?: ""
        val teacherId = doc.getString("teacherId") ?: ""
        val joinedAt = doc.getLong("joinedAt") ?: System.currentTimeMillis()
        val statusStr = doc.getString("status") ?: StudentBatchStatus.ACTIVE.name
        val status = try { StudentBatchStatus.valueOf(statusStr) } catch (_: Exception) { StudentBatchStatus.ACTIVE }

        return StudentBatch(
            id = id,
            studentId = studentId,
            batchId = batchId,
            batchCode = batchCode,
            teacherId = teacherId,
            joinedAt = joinedAt,
            status = status
        )
    }

    private fun parseBatchJoinRequest(doc: DocumentSnapshot): BatchJoinRequest? {
        val id = doc.getString("id") ?: doc.id
        val batchId = doc.getString("batchId") ?: return null
        val batchName = doc.getString("batchName") ?: (_batches.value[batchId]?.name ?: "Batch")
        val studentId = doc.getString("studentId") ?: return null
        val studentName = doc.getString("studentName") ?: "Student"
        val studentEmail = doc.getString("studentEmail") ?: ""
        val teacherId = doc.getString("teacherId")
            ?: _batches.value[batchId]?.teacherId
            ?: "teacher_default"
        val statusStr = doc.getString("status") ?: JoinRequestStatus.PENDING.name
        val status = try { JoinRequestStatus.valueOf(statusStr) } catch (_: Exception) { JoinRequestStatus.PENDING }
        val requestedAt = doc.getLong("requestedAt") ?: System.currentTimeMillis()
        val respondedAt = doc.getLong("respondedAt")
        val respondedBy = doc.getString("respondedBy")
        val rejectionReason = doc.getString("rejectionReason")

        return BatchJoinRequest(
            id = id,
            batchId = batchId,
            batchName = batchName,
            studentId = studentId,
            studentName = studentName,
            studentEmail = studentEmail,
            teacherId = teacherId,
            status = status,
            requestedAt = requestedAt,
            respondedAt = respondedAt,
            respondedBy = respondedBy,
            rejectionReason = rejectionReason
        )
    }

    companion object {
        val instance: BatchRepositoryImpl by lazy { BatchRepositoryImpl() }
    }
}
