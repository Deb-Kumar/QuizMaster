package com.example.onlinequizapp.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.SessionStatus
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.domain.model.UserQuestionAnswer
import com.example.onlinequizapp.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(
    private val quizId: String,
    private val repository: QuizRepository = QuizRepositoryImpl.instance
) : ViewModel() {

    private val _sessionState = MutableStateFlow<QuizSessionState?>(null)
    val sessionState: StateFlow<QuizSessionState?> = _sessionState.asStateFlow()

    init {
        loadResult()
    }

    fun loadResult() {
        viewModelScope.launch {
            var session = repository.getLatestQuizSession(quizId)
            if (session == null) {
                val quiz = repository.getQuizById(quizId)
                if (quiz != null) {
                    val user = com.example.onlinequizapp.data.session.AuthSessionManager.getSavedUser()
                    val userId = user?.id ?: ""
                    if (userId.isNotBlank()) {
                        val results = repository.getQuizResultsForUser(userId)
                        val match = results.firstOrNull { it.quizId == quizId }
                        if (match != null && repository is QuizRepositoryImpl) {
                            session = repository.reconstructSessionFromQuizResult(quiz, match)
                        }
                    }
                }
            }
            _sessionState.value = session
        }
    }

    companion object {
        fun provideFactory(
            quizId: String,
            repository: QuizRepository = QuizRepositoryImpl.instance
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ResultViewModel(quizId, repository) as T
            }
        }
    }
}
