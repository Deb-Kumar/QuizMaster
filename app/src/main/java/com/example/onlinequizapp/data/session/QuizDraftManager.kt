package com.example.onlinequizapp.data.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents an in-progress quiz attempt saved to persistent storage.
 */
data class QuizDraft(
    val quizId: String,
    val quizTitle: String,
    val userId: String,
    val currentIndex: Int,
    val answers: Map<String, Set<String>>, // questionId -> selectedOptionIds
    val remainingTimeSeconds: Int,
    val elapsedTimeSeconds: Int,
    val totalQuestions: Int,
    val answeredCount: Int,
    val savedAtTimestamp: Long,
    val startTimeTimestamp: Long = 0L,
    val deadlineTimestamp: Long = 0L,
    val quizIcon: String = "",
    val questionIdsOrder: List<String> = emptyList(),
    val optionIdsOrderByQuestion: Map<String, List<String>> = emptyMap()
)

/**
 * Manages auto-saving and restoring draft quiz attempts so students can safely
 * resume quizzes if the app is minimized, killed, or reopened later.
 */
object QuizDraftManager {

    private const val TAG = "QuizDraftManager"
    private const val PREFS_NAME = "quiz_master_draft_prefs"
    private const val KEY_LATEST_DRAFT_QUIZ_ID_PREFIX = "latest_draft_quiz_id_"
    private const val KEY_DRAFT_PREFIX = "draft_"

    private var context: Context? = null
    private var prefs: SharedPreferences? = null

    private val _activeDraftFlow = MutableStateFlow<QuizDraft?>(null)
    val activeDraftFlow: StateFlow<QuizDraft?> = _activeDraftFlow.asStateFlow()

    fun init(context: Context) {
        this.context = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        refreshActiveDraft()
    }

    private fun getPrefs(): SharedPreferences? {
        if (prefs == null && context != null) {
            prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return prefs
    }

    /**
     * Refreshes the currently active draft StateFlow for the current user.
     */
    fun refreshActiveDraft(userId: String? = null) {
        val uid = userId ?: AuthSessionManager.getSavedUser()?.id ?: return
        _activeDraftFlow.value = getActiveDraft(uid)
    }

    /**
     * Auto-saves the current quiz session state to SharedPreferences.
     */
    fun saveDraft(userId: String, state: QuizSessionState) {
        val p = getPrefs() ?: return
        if (state.status != SessionStatus.IN_PROGRESS && state.status != SessionStatus.NOT_STARTED) {
            return
        }
        if (state.quiz.questions.isEmpty()) return

        try {
            val answeredMap = state.answers.mapValues { it.value.selectedOptionIds }
            val answeredCount = state.answers.values.count { it.isAnswered }
            val qIdsOrder = state.quiz.questions.map { it.id }
            val optIdsMap = state.quiz.questions.associate { it.id to it.options.map { opt -> opt.id } }

            val draft = QuizDraft(
                quizId = state.quiz.id,
                quizTitle = state.quiz.title,
                userId = userId,
                currentIndex = state.currentIndex,
                answers = answeredMap,
                remainingTimeSeconds = state.remainingTimeSeconds,
                elapsedTimeSeconds = state.elapsedTimeSeconds,
                totalQuestions = state.totalQuestions,
                answeredCount = answeredCount,
                savedAtTimestamp = System.currentTimeMillis(),
                startTimeTimestamp = state.startTimeTimestamp,
                deadlineTimestamp = state.deadlineTimestamp,
                quizIcon = state.quiz.icon,
                questionIdsOrder = qIdsOrder,
                optionIdsOrderByQuestion = optIdsMap
            )

            val json = JSONObject().apply {
                put("quizId", draft.quizId)
                put("quizTitle", draft.quizTitle)
                put("quizIcon", draft.quizIcon)
                put("userId", draft.userId)
                put("currentIndex", draft.currentIndex)
                put("remainingTimeSeconds", draft.remainingTimeSeconds)
                put("elapsedTimeSeconds", draft.elapsedTimeSeconds)
                put("totalQuestions", draft.totalQuestions)
                put("answeredCount", draft.answeredCount)
                put("savedAtTimestamp", draft.savedAtTimestamp)
                put("startTimeTimestamp", draft.startTimeTimestamp)
                put("deadlineTimestamp", draft.deadlineTimestamp)

                val answersObj = JSONObject()
                draft.answers.forEach { (qId, optionIds) ->
                    val arr = JSONArray()
                    optionIds.forEach { arr.put(it) }
                    answersObj.put(qId, arr)
                }
                put("answers", answersObj)

                val qIdsArr = JSONArray()
                draft.questionIdsOrder.forEach { qIdsArr.put(it) }
                put("questionIdsOrder", qIdsArr)

                val optIdsObj = JSONObject()
                draft.optionIdsOrderByQuestion.forEach { (qId, optIds) ->
                    val arr = JSONArray()
                    optIds.forEach { arr.put(it) }
                    optIdsObj.put(qId, arr)
                }
                put("optionIdsOrderByQuestion", optIdsObj)
            }

            p.edit()
                .putString(buildDraftKey(userId, draft.quizId), json.toString())
                .putString(KEY_LATEST_DRAFT_QUIZ_ID_PREFIX + userId, draft.quizId)
                .apply()

            _activeDraftFlow.value = draft
            Log.d(TAG, "Draft auto-saved for quiz ${draft.quizId} (Q${draft.currentIndex + 1}/${draft.totalQuestions})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save draft", e)
        }
    }

    /**
     * Gets a draft for a specific quiz and user, if available.
     */
    fun getDraftForQuiz(userId: String, quizId: String): QuizDraft? {
        val p = getPrefs() ?: return null
        val rawJson = p.getString(buildDraftKey(userId, quizId), null) ?: return null
        return parseDraftJson(rawJson)
    }

    /**
     * Retrieves the latest incomplete quiz draft for the user.
     */
    fun getActiveDraft(userId: String): QuizDraft? {
        val p = getPrefs() ?: return null
        val latestQuizId = p.getString(KEY_LATEST_DRAFT_QUIZ_ID_PREFIX + userId, null)
        if (!latestQuizId.isNullOrBlank()) {
            val draft = getDraftForQuiz(userId, latestQuizId)
            if (draft != null) return draft
        }

        // Fallback: search all keys for this user
        val prefix = KEY_DRAFT_PREFIX + userId + "_"
        val allEntries = p.all
        var mostRecent: QuizDraft? = null
        for ((key, value) in allEntries) {
            if (key.startsWith(prefix) && value is String) {
                val parsed = parseDraftJson(value)
                if (parsed != null) {
                    if (mostRecent == null || parsed.savedAtTimestamp > mostRecent.savedAtTimestamp) {
                        mostRecent = parsed
                    }
                }
            }
        }
        return mostRecent
    }

    /**
     * Deletes a draft when a quiz is submitted or discarded.
     */
    fun clearDraft(userId: String, quizId: String) {
        val p = getPrefs() ?: return
        val editor = p.edit()
        editor.remove(buildDraftKey(userId, quizId))

        val latestQuizId = p.getString(KEY_LATEST_DRAFT_QUIZ_ID_PREFIX + userId, null)
        if (latestQuizId == quizId) {
            editor.remove(KEY_LATEST_DRAFT_QUIZ_ID_PREFIX + userId)
        }
        editor.apply()

        // Update flow
        _activeDraftFlow.value = getActiveDraft(userId)
        Log.d(TAG, "Draft cleared for quiz $quizId")
    }

    /**
     * Clears all drafts for the given user (e.g. on logout).
     */
    fun clearAllDrafts(userId: String) {
        val p = getPrefs() ?: return
        val prefix = KEY_DRAFT_PREFIX + userId + "_"
        val editor = p.edit()
        editor.remove(KEY_LATEST_DRAFT_QUIZ_ID_PREFIX + userId)

        for (key in p.all.keys) {
            if (key.startsWith(prefix)) {
                editor.remove(key)
            }
        }
        editor.apply()
        _activeDraftFlow.value = null
    }

    private fun buildDraftKey(userId: String, quizId: String): String {
        return "$KEY_DRAFT_PREFIX${userId}_$quizId"
    }

    private fun parseDraftJson(rawJson: String): QuizDraft? {
        return try {
            val json = JSONObject(rawJson)
            val answersMap = mutableMapOf<String, Set<String>>()
            val answersObj = json.optJSONObject("answers")
            if (answersObj != null) {
                val keys = answersObj.keys()
                while (keys.hasNext()) {
                    val qId = keys.next()
                    val arr = answersObj.optJSONArray(qId)
                    val set = mutableSetOf<String>()
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            set.add(arr.getString(i))
                        }
                    }
                    answersMap[qId] = set
                }
            }

            val qOrderList = mutableListOf<String>()
            val qOrderArr = json.optJSONArray("questionIdsOrder")
            if (qOrderArr != null) {
                for (i in 0 until qOrderArr.length()) {
                    qOrderList.add(qOrderArr.getString(i))
                }
            }

            val optOrderMap = mutableMapOf<String, List<String>>()
            val optOrderObj = json.optJSONObject("optionIdsOrderByQuestion")
            if (optOrderObj != null) {
                val keys = optOrderObj.keys()
                while (keys.hasNext()) {
                    val qId = keys.next()
                    val arr = optOrderObj.optJSONArray(qId)
                    val list = mutableListOf<String>()
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            list.add(arr.getString(i))
                        }
                    }
                    optOrderMap[qId] = list
                }
            }

            QuizDraft(
                quizId = json.getString("quizId"),
                quizTitle = json.optString("quizTitle", "Quiz"),
                userId = json.optString("userId", ""),
                currentIndex = json.optInt("currentIndex", 0),
                answers = answersMap,
                remainingTimeSeconds = json.optInt("remainingTimeSeconds", 0),
                elapsedTimeSeconds = json.optInt("elapsedTimeSeconds", 0),
                totalQuestions = json.optInt("totalQuestions", 0),
                answeredCount = json.optInt("answeredCount", 0),
                savedAtTimestamp = json.optLong("savedAtTimestamp", 0L),
                startTimeTimestamp = json.optLong("startTimeTimestamp", 0L),
                deadlineTimestamp = json.optLong("deadlineTimestamp", 0L),
                quizIcon = json.optString("quizIcon", ""),
                questionIdsOrder = qOrderList,
                optionIdsOrderByQuestion = optOrderMap
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse draft JSON", e)
            null
        }
    }
}
