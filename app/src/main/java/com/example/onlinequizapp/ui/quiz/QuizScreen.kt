package com.example.onlinequizapp.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Lock
import com.example.onlinequizapp.ui.components.LeaveQuizModal
import com.example.onlinequizapp.ui.components.QuizTimeExpiredDialog
import com.example.onlinequizapp.ui.theme.LocalQuizDark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.domain.model.FeedbackMode
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.SessionStatus
import com.example.onlinequizapp.domain.model.TimerMode
import androidx.compose.ui.platform.LocalContext
import com.example.onlinequizapp.ui.components.ExplanationCard
import com.example.onlinequizapp.ui.components.FeedbackBanner
import com.example.onlinequizapp.ui.components.HintCard
import com.example.onlinequizapp.ui.components.OptionItem
import com.example.onlinequizapp.ui.components.OptionVisualState
import com.example.onlinequizapp.ui.components.ProgressSection
import com.example.onlinequizapp.ui.components.QuestionCardHeader
import com.example.onlinequizapp.ui.components.QuestionGridDialog
import com.example.onlinequizapp.ui.components.QuizNavigationBar
import com.example.onlinequizapp.ui.components.QuizTopBar
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.theme.BrandPrimary

@Composable
fun QuizScreen(
    quizId: String,
    onNavigateBack: () -> Unit,
    onQuizCompleted: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuizViewModel = viewModel(
        factory = QuizViewModel.provideFactory(quizId)
    )
) {
    val state by viewModel.sessionState.collectAsStateWithLifecycle()
    val isRestored by viewModel.isRestoredSession.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    var showGridDialog by remember { mutableStateOf(false) }
    var showTimeExpiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRestored) {
        if (isRestored) {
            context.showToast("Resumed quiz where you left off 📝")
        }
    }

    BackHandler {
        if (showTimeExpiredDialog) {
            // Must view results
            onQuizCompleted(quizId)
        } else if (state?.status == SessionStatus.IN_PROGRESS) {
            showExitDialog = true
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(state?.status) {
        when (state?.status) {
            SessionStatus.COMPLETED -> {
                // If it was triggered by time expiration, dialog handles the transition
                if (!showTimeExpiredDialog) {
                    context.showToast("Quiz submitted! Calculating your score...")
                    onQuizCompleted(quizId)
                }
            }
            SessionStatus.TIMED_OUT -> {
                // Show the dedicated Time Expired modal dialog
                showTimeExpiredDialog = true
            }
            else -> {}
        }
    }

    val isDark = LocalQuizDark.current

    if (showTimeExpiredDialog) {
        val totalQ = state?.totalQuestions ?: 0
        val answered = state?.answers?.values?.count { it.isAnswered } ?: 0
        QuizTimeExpiredDialog(
            isDark = isDark,
            answeredCount = answered,
            totalQuestions = totalQ,
            onViewResults = {
                showTimeExpiredDialog = false
                onQuizCompleted(quizId)
            }
        )
    }

    if (showExitDialog) {
        LeaveQuizModal(
            isDark = isDark,
            onContinue = { showExitDialog = false },
            onExitAndSave = {
                showExitDialog = false
                context.showToast("Progress saved. You can resume anytime!")
                onNavigateBack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    val unavailableReason by viewModel.unavailableReason.collectAsStateWithLifecycle()

    if (unavailableReason != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) androidx.compose.ui.graphics.Color(0xFF0F172A) else androidx.compose.ui.graphics.Color(0xFFF8FAFC))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(20.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E293B) else androidx.compose.ui.graphics.Color.White
                ),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isDark) androidx.compose.ui.graphics.Color(0xFFF87171) else androidx.compose.ui.graphics.Color(0xFFDC2626),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Quiz Not Accessible",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = unavailableReason ?: "",
                        fontSize = 13.sp,
                        color = if (isDark) androidx.compose.ui.graphics.Color(0xFF94A3B8) else androidx.compose.ui.graphics.Color(0xFF64748B),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF6366F1) else androidx.compose.ui.graphics.Color(0xFF4F46E5)
                        )
                    ) {
                        Text(
                            text = "Back to Home",
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
        return
    }

    val currentState = state
    if (currentState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    if (showGridDialog) {
        QuestionGridDialog(
            state = currentState,
            onQuestionSelected = { viewModel.jumpToQuestion(it) },
            onSubmitQuiz = {
                context.showToast("Submitting quiz...")
                viewModel.submitQuiz()
            },
            onDismiss = { showGridDialog = false }
        )
    }

    val currentQuestion = currentState.currentQuestion
    val currentAnswer = currentState.currentAnswer
    val selectedOptionIds = currentState.currentSelectedOptionIds
    val isCountDown = currentState.quiz.config.timerMode == TimerMode.COUNTDOWN &&
            currentState.quiz.config.timeLimitSeconds > 0
    val isTimerWarning = isCountDown && currentState.remainingTimeSeconds in 1..30

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            QuizTopBar(
                title = currentState.quiz.title,
                timerMode = if (isCountDown) TimerMode.COUNTDOWN else TimerMode.NONE,
                seconds = if (isCountDown) {
                    currentState.remainingTimeSeconds
                } else {
                    currentState.elapsedTimeSeconds
                },
                isWarning = isTimerWarning,
                onBackClick = { showExitDialog = true }
            )
        },
        bottomBar = {
            QuizNavigationBar(
                isFirstQuestion = currentState.isFirstQuestion,
                isLastQuestion = currentState.isLastQuestion,
                canNavigateNext = selectedOptionIds.isNotEmpty() || !currentState.showImmediateFeedback,
                onPreviousClick = { viewModel.navigatePrevious() },
                onNextClick = { viewModel.navigateNext() },
                onSubmitClick = {
                    context.showToast("Submitting quiz...")
                    viewModel.submitQuiz()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Progress Section
            item {
                ProgressSection(
                    currentIndex = currentState.currentIndex,
                    totalQuestions = currentState.totalQuestions,
                    onOpenGrid = { showGridDialog = true }
                )
            }

            // Question Card Header
            item {
                QuestionCardHeader(
                    difficulty = currentQuestion.difficulty,
                    questionType = currentQuestion.type,
                    prompt = currentQuestion.prompt
                )
            }

            // Immediate Feedback Banner (if active)
            if (currentState.showImmediateFeedback && currentAnswer != null) {
                item {
                    FeedbackBanner(
                        isCorrect = currentAnswer.isCorrect,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Options List
            items(currentQuestion.options.size) { index ->
                val option = currentQuestion.options[index]
                val isSelected = selectedOptionIds.contains(option.id)

                val visualState = when {
                    currentState.showImmediateFeedback -> {
                        when {
                            currentQuestion.correctAnswers.contains(option.id) -> OptionVisualState.CORRECT
                            isSelected -> OptionVisualState.INCORRECT
                            else -> OptionVisualState.NORMAL
                        }
                    }
                    isSelected -> OptionVisualState.SELECTED
                    else -> OptionVisualState.NORMAL
                }

                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    OptionItem(
                        option = option,
                        questionType = currentQuestion.type,
                        isSelected = isSelected,
                        visualState = visualState,
                        enabled = !currentState.showImmediateFeedback,
                        onSelect = { viewModel.selectOption(option.id) }
                    )
                }
            }

            // Confirm Answer Button for Multiple Choice in Immediate Feedback mode
            if (currentState.quiz.config.feedbackMode == FeedbackMode.IMMEDIATE &&
                currentQuestion.type == QuestionType.MULTIPLE &&
                !currentState.showImmediateFeedback &&
                selectedOptionIds.isNotEmpty()
            ) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Button(
                            onClick = { viewModel.submitCurrentImmediateFeedback() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Text(
                                text = "Check Answer",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Hint Accordion Card
            if (currentState.quiz.config.allowHints && !currentQuestion.hint.isNullOrBlank()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        HintCard(
                            hint = currentQuestion.hint,
                            isRevealed = currentState.isHintRevealed,
                            onToggleReveal = {
                                if (!currentState.isHintRevealed) {
                                    context.showToast("Hint revealed")
                                }
                                viewModel.toggleHint()
                            }
                        )
                    }
                }
            }

            // Explanation Card (shown in Immediate feedback mode after answer is verified)
            if (currentState.showImmediateFeedback && currentQuestion.explanation.isNotBlank()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        ExplanationCard(explanation = currentQuestion.explanation)
                    }
                }
            }
        }
    }
}
