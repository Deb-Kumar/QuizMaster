package com.example.onlinequizapp.ui.home.certificates

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.model.CertificateSummaryStats
import com.example.onlinequizapp.domain.model.EarnedCertificateItem
import com.example.onlinequizapp.domain.model.QuizToUnlockCertificateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CertificatesUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0: Earned Certificates, 1: Unlock More
    val studentName: String = "Student",
    val studentEmail: String = "",
    val avatarUrl: String? = null,
    val earnedCertificates: List<EarnedCertificateItem> = emptyList(),
    val quizzesToUnlock: List<QuizToUnlockCertificateItem> = emptyList(),
    val summaryStats: CertificateSummaryStats = CertificateSummaryStats(),
    val selectedCertificateForPreview: EarnedCertificateItem? = null,
    val error: String? = null
)

class CertificatesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CertificatesUiState())
    val uiState: StateFlow<CertificatesUiState> = _uiState.asStateFlow()

    private val quizRepository = QuizRepositoryImpl.instance
    private val batchRepository = BatchRepositoryImpl.instance

    init {
        loadData()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectCertificateForPreview(cert: EarnedCertificateItem?) {
        _uiState.update { it.copy(selectedCertificateForPreview = cert) }
    }

    fun refresh() {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = AuthSessionManager.getSavedUser()
                val studentId = user?.id ?: "student"
                val studentName = user?.name?.ifBlank { "Student" } ?: "Student"
                val studentEmail = user?.email ?: ""
                val avatarUrl = user?.avatarUrl

                _uiState.update {
                    it.copy(
                        studentName = studentName,
                        studentEmail = studentEmail,
                        avatarUrl = avatarUrl
                    )
                }

                // 1. Fetch user's quiz completion results
                val userResults = quizRepository.getQuizResultsForUser(studentId)

                // 2. Fetch accessible quizzes (assigned via batch, individually or public)
                val accessibleQuizzes = quizRepository.getAccessibleQuizzesForStudent(studentId)
                val quizMap = accessibleQuizzes.associateBy { it.id }

                // 3. Process Earned Certificates (score >= 80%)
                // Group by quizId to take the student's best score for that quiz
                val certifiedResults = userResults
                    .filter { it.scorePercentage >= 80 }
                    .groupBy { it.quizId }
                    .mapValues { entry -> entry.value.maxByOrNull { it.scorePercentage }!! }
                    .values
                    .sortedByDescending { it.submittedAtMillis }

                val earnedList = certifiedResults.map { result ->
                    val quiz = quizMap[result.quizId]
                    val effectiveCategory = result.quizCategory.ifBlank { quiz?.category ?: "General" }
                    val effectiveIcon = if (result.quizIcon.isNotBlank() && result.quizIcon != "code") {
                        result.quizIcon
                    } else {
                        quiz?.icon ?: "code"
                    }
                    val timestampPart = if (result.submittedAtMillis > 0) {
                        result.submittedAtMillis.toString().takeLast(6)
                    } else {
                        System.currentTimeMillis().toString().takeLast(6)
                    }
                    val certId = "QM-${result.quizId.takeLast(4).uppercase()}-$timestampPart"

                    EarnedCertificateItem(
                        certificateId = certId,
                        quizId = result.quizId,
                        quizTitle = result.quizTitle.ifBlank { quiz?.title ?: "Quiz" },
                        quizCategory = effectiveCategory,
                        quizIcon = effectiveIcon,
                        scorePercentage = result.scorePercentage,
                        submittedAtMillis = result.submittedAtMillis,
                        studentName = studentName
                    )
                }

                val certifiedQuizIds = earnedList.map { it.quizId }.toSet()

                // 4. Process Quizzes To Unlock (attempts with score < 80% or available unattempted quizzes)
                val unlockList = mutableListOf<QuizToUnlockCertificateItem>()

                for (quiz in accessibleQuizzes) {
                    if (certifiedQuizIds.contains(quiz.id)) continue // Already earned!

                    // Find best attempt for this quiz if any
                    val previousAttempt = userResults
                        .filter { it.quizId == quiz.id }
                        .maxByOrNull { it.scorePercentage }

                    unlockList.add(
                        QuizToUnlockCertificateItem(
                            quizId = quiz.id,
                            quizTitle = quiz.title,
                            quizCategory = quiz.category,
                            quizIcon = quiz.icon,
                            previousScore = previousAttempt?.scorePercentage,
                            questionsCount = quiz.questions.size
                        )
                    )
                }

                // Sort: Quizzes already attempted but scored < 80% first (closest to 80% top), then unattempted
                val sortedUnlockList = unlockList.sortedWith(
                    compareByDescending<QuizToUnlockCertificateItem> { it.previousScore != null }
                        .thenByDescending { it.previousScore ?: 0 }
                )

                // 5. Calculate Summary Stats
                val total = earnedList.size
                val distinctions = earnedList.count { it.isDistinction }
                val merits = earnedList.count { !it.isDistinction }
                val avg = if (earnedList.isNotEmpty()) {
                    earnedList.map { it.scorePercentage }.average().toInt()
                } else {
                    0
                }

                val stats = CertificateSummaryStats(
                    totalEarned = total,
                    distinctionCount = distinctions,
                    meritCount = merits,
                    averageScore = avg
                )

                _uiState.update {
                    it.copy(
                        earnedCertificates = earnedList,
                        quizzesToUnlock = sortedUnlockList,
                        summaryStats = stats,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Log.e("CertificatesViewModel", "Failed to load certificates data", e)
                _uiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Failed to load certificates",
                        isLoading = false
                    )
                }
            }
        }
    }
}
