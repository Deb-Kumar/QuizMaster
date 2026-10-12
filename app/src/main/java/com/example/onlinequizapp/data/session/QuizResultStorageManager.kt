package com.example.onlinequizapp.data.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.onlinequizapp.data.repository.toQuizResult
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.SessionStatus
import com.example.onlinequizapp.domain.model.UserQuestionAnswer
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages persistent local device storage (SharedPreferences) for completed quiz results.
 * This guarantees results survive app restarts and are accessible offline.
 */
object QuizResultStorageManager {

    private const val TAG = "QuizResultStorage"
    private const val PREFS_NAME = "quiz_master_completed_results_prefs"
    private const val KEY_RESULT_PREFIX = "quiz_result_"

    private var context: Context? = null
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getPrefs(): SharedPreferences? {
        if (prefs == null && context != null) {
            prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return prefs
    }

    private fun buildKey(userId: String, quizId: String): String {
        return "$KEY_RESULT_PREFIX${userId}_$quizId"
    }

    /**
     * Saves a completed quiz result locally on disk.
     */
    fun saveResultLocally(userId: String, state: QuizSessionState) {
        val p = getPrefs() ?: return
        try {
            val key = buildKey(userId, state.quiz.id)
            val json = JSONObject().apply {
                put("quizId", state.quiz.id)
                put("userId", userId)
                put("userName", AuthSessionManager.getSavedUser()?.name ?: "Student")
                put("userEmail", AuthSessionManager.getSavedUser()?.email ?: "")
                put("currentIndex", state.currentIndex)
                put("remainingTimeSeconds", state.remainingTimeSeconds)
                put("elapsedTimeSeconds", state.elapsedTimeSeconds)
                put("status", state.status.name)
                put("savedAt", System.currentTimeMillis())
                put("scorePercentage", state.scorePercentage)
                put("correctCount", state.correctCount)
                put("totalQuestions", state.totalQuestions)
                put("quizTitle", state.quiz.title)
                put("quizCategory", state.quiz.category)
                put("quizIcon", state.quiz.icon)
                put("teacherId", state.quiz.teacherId)

                val answersObj = JSONObject()
                state.answers.forEach { (qId, ans) ->
                    val aObj = JSONObject().apply {
                        put("questionId", ans.questionId)
                        put("isAnswered", ans.isAnswered)
                        put("isCorrect", ans.isCorrect)
                        put("timeSpentSeconds", ans.timeSpentSeconds)
                        val optArray = JSONArray()
                        ans.selectedOptionIds.forEach { optArray.put(it) }
                        put("selectedOptionIds", optArray)
                    }
                    answersObj.put(qId, aObj)
                }
                put("answers", answersObj)
            }

            p.edit().putString(key, json.toString()).apply()
            Log.d(TAG, "Saved quiz result locally on disk for quiz: ${state.quiz.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save quiz result locally", e)
        }
    }

    /**
     * Retrieves a saved result for a specific quiz and user, if available.
     */
    fun getSavedResult(userId: String, quiz: Quiz): QuizSessionState? {
        val p = getPrefs() ?: return null
        val key = buildKey(userId, quiz.id)
        val rawJson = p.getString(key, null) ?: return null
        return parseResultJson(rawJson, quiz)
    }

    /**
     * Checks synchronously whether a saved completed result exists locally on disk for this quiz.
     */
    fun hasSavedResult(userId: String, quizId: String): Boolean {
        val p = getPrefs() ?: return false
        val key = buildKey(userId, quizId)
        return p.contains(key)
    }

    /**
     * Retrieves all saved results for a user mapped to the provided quizzes.
     */
    fun getAllSavedResults(userId: String, quizzes: List<Quiz>): List<QuizSessionState> {
        val p = getPrefs() ?: return emptyList()
        val results = mutableListOf<QuizSessionState>()
        val prefix = "$KEY_RESULT_PREFIX${userId}_"

        val quizMap = quizzes.associateBy { it.id }

        p.all.forEach { (key, value) ->
            if (key.startsWith(prefix) && value is String) {
                try {
                    val jsonObj = JSONObject(value)
                    val qId = jsonObj.optString("quizId")
                    val quiz = quizMap[qId]
                    if (quiz != null) {
                        parseResultJson(value, quiz)?.let { results.add(it) }
                    }
                } catch (_: Exception) {}
            }
        }
        return results
    }

    /**
     * Retrieves all saved results on this device for a given quizId across any user.
     */
    fun getAllSavedResultsForQuiz(quizId: String, quiz: Quiz?): List<QuizResult> {
        val p = getPrefs() ?: return emptyList()
        val results = mutableListOf<QuizResult>()

        p.all.forEach { (key, value) ->
            if (key.startsWith(KEY_RESULT_PREFIX) && value is String) {
                try {
                    val jsonObj = JSONObject(value)
                    val qId = jsonObj.optString("quizId")
                    if (qId == quizId) {
                        val uId = jsonObj.optString("userId", "student")
                        val uName = jsonObj.optString("userName", AuthSessionManager.getSavedUser()?.name ?: "Student")
                        val savedAt = jsonObj.optLong("savedAt", System.currentTimeMillis())
                        val elapsed = jsonObj.optInt("elapsedTimeSeconds", 0)

                        if (quiz != null) {
                            val session = parseResultJson(value, quiz)
                            if (session != null) {
                                results.add(session.toQuizResult(userId = uId, userName = uName, submittedAtMillis = savedAt))
                            }
                        } else {
                            val score = jsonObj.optInt("scorePercentage", 0)
                            val totalQ = jsonObj.optInt("totalQuestions", 0)
                            val correct = jsonObj.optInt("correctCount", 0)
                            val qTitle = jsonObj.optString("quizTitle", "Quiz")
                            val qCat = jsonObj.optString("quizCategory", "General")
                            val tId = jsonObj.optString("teacherId", "")
                            results.add(
                                QuizResult(
                                    resultId = "local_${uId}_${qId}",
                                    userId = uId,
                                    userName = uName,
                                    quizId = qId,
                                    quizTitle = qTitle,
                                    quizCategory = qCat,
                                    quizIcon = "code",
                                    teacherId = tId,
                                    totalQuestions = totalQ,
                                    correctCount = correct,
                                    incorrectCount = (totalQ - correct).coerceAtLeast(0),
                                    unansweredCount = 0,
                                    scorePercentage = score,
                                    elapsedTimeSeconds = elapsed,
                                    submittedAtMillis = savedAt,
                                    status = "COMPLETED"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed parsing saved result for quiz $quizId", e)
                }
            }
        }
        return results
    }

    private fun parseResultJson(rawJson: String, quiz: Quiz): QuizSessionState? {
        return try {
            val json = JSONObject(rawJson)
            val answersMap = mutableMapOf<String, UserQuestionAnswer>()
            val answersObj = json.optJSONObject("answers")

            if (answersObj != null) {
                val keys = answersObj.keys()
                while (keys.hasNext()) {
                    val qId = keys.next()
                    val aObj = answersObj.getJSONObject(qId)
                    val optArray = aObj.optJSONArray("selectedOptionIds")
                    val selSet = mutableSetOf<String>()
                    if (optArray != null) {
                        for (i in 0 until optArray.length()) {
                            selSet.add(optArray.getString(i))
                        }
                    }
                    answersMap[qId] = UserQuestionAnswer(
                        questionId = aObj.optString("questionId", qId),
                        selectedOptionIds = selSet,
                        isAnswered = aObj.optBoolean("isAnswered", false),
                        isCorrect = aObj.optBoolean("isCorrect", false),
                        timeSpentSeconds = aObj.optInt("timeSpentSeconds", 0)
                    )
                }
            }

            QuizSessionState(
                quiz = quiz,
                currentIndex = json.optInt("currentIndex", 0),
                answers = answersMap,
                remainingTimeSeconds = json.optInt("remainingTimeSeconds", 0),
                elapsedTimeSeconds = json.optInt("elapsedTimeSeconds", 0),
                status = SessionStatus.valueOf(json.optString("status", SessionStatus.COMPLETED.name))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse result json", e)
            null
        }
    }
}
