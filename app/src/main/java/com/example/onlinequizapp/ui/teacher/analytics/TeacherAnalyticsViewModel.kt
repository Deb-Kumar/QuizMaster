package com.example.onlinequizapp.ui.teacher.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.repository.AuthRepository
import com.example.onlinequizapp.domain.repository.BatchRepository
import com.example.onlinequizapp.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AnalyticsTimeframe(val label: String) {
    ALL_TIME("All Time"),
    PAST_30_DAYS("Past 30 Days"),
    PAST_7_DAYS("Past 7 Days")
}

data class ScoreBracket(
    val label: String,
    val rangeText: String,
    val count: Int,
    val percentage: Float, // 0f to 1f
    val colorHex: Long
)

data class QuizAnalyticsItem(
    val quiz: Quiz,
    val attemptsCount: Int,
    val averageScore: Int,
    val highestScore: Int,
    val lowestScore: Int,
    val passRate: Int,
    val completionCount: Int
)

data class StudentPerformanceItem(
    val studentId: String,
    val studentName: String,
    val attemptsCount: Int,
    val averageScore: Int,
    val highestScore: Int,
    val lastAttemptQuizTitle: String,
    val isNeedsAttention: Boolean // avgScore < 50%
)

data class TeacherAnalyticsUiState(
    val isLoading: Boolean = true,
    val teacherUser: User? = null,
    val allQuizzes: List<Quiz> = emptyList(),
    val allResults: List<QuizResult> = emptyList(),
    val batches: List<Batch> = emptyList(),

    // Filters
    val selectedBatchId: String? = null, // null = All Batches
    val selectedQuizId: String? = null, // null = All Quizzes
    val selectedTimeframe: AnalyticsTimeframe = AnalyticsTimeframe.ALL_TIME,

    // Derived Aggregated Metrics
    val totalAttempts: Int = 0,
    val totalQuizzesAnalyzed: Int = 0,
    val averageScore: Int = 0,
    val passRate: Int = 0, // >= 60%
    val certifiedCount: Int = 0, // >= 80%
    val highestScoreRecorded: Int = 0,
    val lowestScoreRecorded: Int = 0,

    // Chart Brackets
    val scoreBrackets: List<ScoreBracket> = emptyList(),

    // Per Quiz & Student lists
    val quizAnalyticsList: List<QuizAnalyticsItem> = emptyList(),
    val topPerformers: List<StudentPerformanceItem> = emptyList(),
    val studentsNeedingAttention: List<StudentPerformanceItem> = emptyList()
)

class TeacherAnalyticsViewModel(
    private val quizRepository: QuizRepository = QuizRepositoryImpl.instance,
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance,
    private val batchRepository: BatchRepository = BatchRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAnalyticsUiState())
    val uiState: StateFlow<TeacherAnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUser = authRepository.getCurrentUser()
            val teacherId = currentUser?.id.orEmpty()

            val teacherQuizzes = if (teacherId.isNotBlank()) {
                quizRepository.getQuizzesByTeacher(teacherId).sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
            } else {
                emptyList()
            }

            val teacherResults = if (teacherId.isNotBlank()) {
                quizRepository.getQuizResultsForTeacher(teacherId)
            } else {
                emptyList()
            }

            // Immediately post loaded quizzes and results before waiting on batches stream
            _uiState.update { current ->
                current.copy(
                    teacherUser = currentUser,
                    allQuizzes = teacherQuizzes,
                    allResults = teacherResults,
                    isLoading = false
                )
            }
            recalculateAnalytics()

            batchRepository.getBatchesByTeacher(teacherId).collect { batchesList ->
                val refreshedResults = if (teacherId.isNotBlank()) {
                    quizRepository.getQuizResultsForTeacher(teacherId)
                } else {
                    emptyList()
                }
                _uiState.update { current ->
                    current.copy(
                        teacherUser = currentUser,
                        allQuizzes = teacherQuizzes,
                        allResults = refreshedResults,
                        batches = batchesList,
                        isLoading = false
                    )
                }
                recalculateAnalytics()
            }
        }
    }

    fun onSelectBatch(batchId: String?) {
        _uiState.update { it.copy(selectedBatchId = batchId) }
        recalculateAnalytics()
    }

    fun onSelectQuiz(quizId: String?) {
        _uiState.update { it.copy(selectedQuizId = quizId) }
        recalculateAnalytics()
    }

    fun onSelectTimeframe(timeframe: AnalyticsTimeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
        recalculateAnalytics()
    }

    private fun recalculateAnalytics() {
        val state = _uiState.value
        val allQuizzes = state.allQuizzes
        var filteredResults: List<QuizResult> = state.allResults

        // 1. Filter by Quiz if specified
        if (!state.selectedQuizId.isNullOrBlank()) {
            filteredResults = filteredResults.filter { it.quizId == state.selectedQuizId }
        }

        // 2. Filter by Batch if specified (using targetBatchIds)
        if (!state.selectedBatchId.isNullOrBlank()) {
            val batchQuizIds = allQuizzes.filter { state.selectedBatchId in it.targetBatchIds }.map { it.id }.toSet()
            filteredResults = filteredResults.filter { it.quizId in batchQuizIds }
        }

        // 3. Filter by Timeframe
        val now = System.currentTimeMillis()
        when (state.selectedTimeframe) {
            AnalyticsTimeframe.PAST_7_DAYS -> {
                val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                filteredResults = filteredResults.filter { it.submittedAtMillis >= sevenDaysAgo }
            }
            AnalyticsTimeframe.PAST_30_DAYS -> {
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                filteredResults = filteredResults.filter { it.submittedAtMillis >= thirtyDaysAgo }
            }
            AnalyticsTimeframe.ALL_TIME -> Unit
        }

        val totalAttempts = filteredResults.size
        val avgScore = if (filteredResults.isEmpty()) 0 else filteredResults.map { it.scorePercentage }.average().toInt()
        val passingCount = filteredResults.count { it.scorePercentage >= 60 }
        val passRate = if (totalAttempts > 0) ((passingCount.toFloat() / totalAttempts) * 100).toInt() else 0
        val certifiedCount = filteredResults.count { it.scorePercentage >= 80 }
        val highestScore = filteredResults.maxOfOrNull { it.scorePercentage } ?: 0
        val lowestScore = filteredResults.minOfOrNull { it.scorePercentage } ?: 0

        // Score Distribution Brackets
        val b90to100 = filteredResults.count { it.scorePercentage in 90..100 }
        val b75to89 = filteredResults.count { it.scorePercentage in 75..89 }
        val b50to74 = filteredResults.count { it.scorePercentage in 50..74 }
        val bBelow50 = filteredResults.count { it.scorePercentage < 50 }

        val denom = if (totalAttempts > 0) totalAttempts.toFloat() else 1f
        val brackets = listOf(
            ScoreBracket("Mastery", "90% - 100%", b90to100, b90to100 / denom, 0xFF10B981), // Emerald
            ScoreBracket("Proficient", "75% - 89%", b75to89, b75to89 / denom, 0xFF6366F1), // Indigo
            ScoreBracket("Average", "50% - 74%", b50to74, b50to74 / denom, 0xFFF59E0B), // Amber
            ScoreBracket("Needs Attention", "< 50%", bBelow50, bBelow50 / denom, 0xFFEF4444) // Red
        )

        // Per-Quiz analytics
        val quizzesToAnalyze = if (state.selectedQuizId != null) {
            allQuizzes.filter { it.id == state.selectedQuizId }
        } else {
            allQuizzes
        }

        val quizAnalyticsItems = quizzesToAnalyze.map { quiz ->
            val quizSubs = filteredResults.filter { it.quizId == quiz.id }
            val count = quizSubs.size
            val qAvg = if (count > 0) quizSubs.map { it.scorePercentage }.average().toInt() else 0
            val qHigh = quizSubs.maxOfOrNull { it.scorePercentage } ?: 0
            val qLow = quizSubs.minOfOrNull { it.scorePercentage } ?: 0
            val qPassCount = quizSubs.count { it.scorePercentage >= 60 }
            val qPassRate = if (count > 0) ((qPassCount.toFloat() / count) * 100).toInt() else 0

            QuizAnalyticsItem(
                quiz = quiz,
                attemptsCount = count,
                averageScore = qAvg,
                highestScore = qHigh,
                lowestScore = qLow,
                passRate = qPassRate,
                completionCount = count
            )
        }.sortedByDescending { it.attemptsCount }

        // Per-Student Performance aggregation
        val studentGroups = filteredResults.groupBy { it.userId.ifBlank { "unknown" } }
        val studentPerformanceItems: List<StudentPerformanceItem> = studentGroups.map { (studentId, resList) ->
            val sAvg = resList.map { it.scorePercentage }.average().toInt()
            val sHigh = resList.maxOfOrNull { it.scorePercentage } ?: 0
            val firstSub = resList.firstOrNull()
            val sName = firstSub?.userName?.takeIf { it.isNotBlank() } ?: "Student ${studentId.takeLast(4)}"
            val lastQuizTitle = resList.lastOrNull()?.quizTitle.orEmpty()

            StudentPerformanceItem(
                studentId = studentId,
                studentName = sName,
                attemptsCount = resList.size,
                averageScore = sAvg,
                highestScore = sHigh,
                lastAttemptQuizTitle = lastQuizTitle,
                isNeedsAttention = sAvg < 50
            )
        }

        val topPerformers = studentPerformanceItems
            .filter { !it.isNeedsAttention }
            .sortedByDescending { it.averageScore }
            .take(5)

        val needsAttention = studentPerformanceItems
            .filter { it.isNeedsAttention }
            .sortedBy { it.averageScore }
            .take(5)

        _uiState.update { current ->
            current.copy(
                totalAttempts = totalAttempts,
                totalQuizzesAnalyzed = quizzesToAnalyze.size,
                averageScore = avgScore,
                passRate = passRate,
                certifiedCount = certifiedCount,
                highestScoreRecorded = highestScore,
                lowestScoreRecorded = lowestScore,
                scoreBrackets = brackets,
                quizAnalyticsList = quizAnalyticsItems,
                topPerformers = topPerformers,
                studentsNeedingAttention = needsAttention
            )
        }
    }
}
