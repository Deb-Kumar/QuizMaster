package com.example.onlinequizapp.ui.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.model.QuizCreationMethod
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.teacher.create.AiQuizMetadataDialog
import com.example.onlinequizapp.ui.teacher.create.BasicInfoStep
import com.example.onlinequizapp.ui.teacher.create.CreateQuizStep
import com.example.onlinequizapp.ui.teacher.create.MethodSelectStep
import com.example.onlinequizapp.ui.teacher.create.ProviderSelectStep
import com.example.onlinequizapp.ui.teacher.create.PublishSuccessDialog
import com.example.onlinequizapp.ui.teacher.create.PublishTargetDialog
import com.example.onlinequizapp.ui.teacher.create.QuestionReviewStep

private val BgDarkNavyTop = Color(0xFF030A24)
private val BgDarkNavyCenter = Color(0xFF070E32)
private val BgDarkNavyBottom = Color(0xFF040618)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCreateQuizScreen(
    quizId: String? = null,
    startAtReview: Boolean = false,
    onNavigateBack: () -> Unit,
    onQuizCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeacherCreateQuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val teacherUser = AuthSessionManager.getSavedUser()
        val teacherId = teacherUser?.id ?: "teacher_default"
        viewModel.loadTeacherData(teacherId)
    }

    LaunchedEffect(quizId, startAtReview) {
        if (!quizId.isNullOrBlank()) {
            viewModel.loadQuizForEditing(quizId, startAtReview = startAtReview)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            context.showToast(msg)
        }
    }

    // Intercept back button to navigate wizard steps
    BackHandler(enabled = true) {
        if (uiState.currentStep == CreateQuizStep.METHOD_SELECT ||
            (uiState.editingQuizId != null && uiState.currentStep == CreateQuizStep.BASIC_INFO)
        ) {
            onNavigateBack()
        } else {
            viewModel.navigateBackStep()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(QuizBuilderCanvasGradient)
                )
                .padding(innerPadding)
        ) {
            // Ambient bottom waves graphic (Method Selection step only)
            if (uiState.currentStep == CreateQuizStep.METHOD_SELECT) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.onlinequizapp.R.drawable.img_quiz_builder_bottom_decor),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = if (uiState.currentStep == CreateQuizStep.METHOD_SELECT) 140.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Header Row
                item {
                    CreateQuizWizardHeader(
                        currentStep = uiState.currentStep,
                        creationMethod = uiState.creationMethod,
                        onBackClick = {
                            if (uiState.currentStep == CreateQuizStep.METHOD_SELECT ||
                                (uiState.editingQuizId != null && uiState.currentStep == CreateQuizStep.BASIC_INFO)
                            ) {
                                onNavigateBack()
                            } else {
                                viewModel.navigateBackStep()
                            }
                        },
                        onCloseClick = onNavigateBack
                    )
                }

                // Error Banner
                uiState.errorMessage?.let { err ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF450A0A))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = err, color = Color(0xFFFCA5A5), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Step Contents
                item {
                    when (uiState.currentStep) {
                        CreateQuizStep.METHOD_SELECT -> {
                            MethodSelectStep(
                                onSelectMethod = { method ->
                                    viewModel.selectCreationMethod(method)
                                }
                            )
                        }

                        CreateQuizStep.PROVIDER_SELECT -> {
                            ProviderSelectStep(
                                selectedProvider = uiState.selectedAiProvider,
                                onSelectProvider = { provider ->
                                    viewModel.selectAiProvider(provider)
                                },
                                isGenerating = uiState.isAiGenerating,
                                generatingStatusText = uiState.aiStatusMessage,
                                onBackClick = { viewModel.navigateBackStep() },
                                onGenerateClick = { viewModel.startAiQuestionGeneration() }
                            )
                        }

                        CreateQuizStep.BASIC_INFO -> {
                            BasicInfoStep(
                                creationMethod = uiState.creationMethod,
                                title = uiState.title,
                                onTitleChanged = { viewModel.onTitleChanged(it) },
                                selectedLogo = uiState.selectedLogo,
                                onLogoSelected = { viewModel.onLogoSelected(it) },
                                description = uiState.description,
                                onDescriptionChanged = { viewModel.onDescriptionChanged(it) },
                                selectedCategories = uiState.categories,
                                onCategoriesChanged = { viewModel.onCategoriesChanged(it) },
                                category = uiState.category,
                                onCategoryChanged = { viewModel.onCategoryChanged(it) },
                                difficulty = uiState.difficulty,
                                onDifficultyChanged = { viewModel.onDifficultyChanged(it) },
                                questionCount = uiState.questionCountToGenerate,
                                onQuestionCountChanged = { viewModel.onQuestionCountToGenerateChanged(it) },
                                questionType = uiState.questionType,
                                onQuestionTypeChanged = { viewModel.onQuestionTypeChanged(it) },
                                timerMode = uiState.timerMode,
                                onTimerModeChanged = { viewModel.onTimerModeChanged(it) },
                                timeLimitMinutes = uiState.timeLimitMinutes,
                                onTimeLimitMinutesChanged = { viewModel.onTimeLimitMinutesChanged(it) },
                                allowHints = uiState.allowHints,
                                onAllowHintsChanged = { viewModel.onAllowHintsChanged(it) },
                                isGenerating = uiState.isAiGenerating,
                                generatingStatusText = uiState.aiStatusMessage,
                                onBackClick = {
                                    if (uiState.editingQuizId != null) {
                                        onNavigateBack()
                                    } else {
                                        viewModel.navigateBackStep()
                                    }
                                },
                                isEditing = uiState.editingQuizId != null,
                                onSubmitClick = {
                                    if (uiState.editingQuizId != null) {
                                        viewModel.proceedToManualQuestionEditor()
                                    } else if (uiState.creationMethod == QuizCreationMethod.AI_GENERATED) {
                                        viewModel.proceedToProviderSelect()
                                    } else {
                                        viewModel.proceedToManualQuestionEditor()
                                    }
                                }
                            )
                        }

                        CreateQuizStep.QUESTION_REVIEW -> {
                            QuestionReviewStep(
                                questions = uiState.questions,
                                allowHints = uiState.allowHints,
                                onAddQuestion = { viewModel.addQuestion() },
                                onImportBulkQuestions = { newQuestions, replaceAt ->
                                    viewModel.importBulkQuestions(newQuestions, replaceAt)
                                    context.showToast("✨ Added ${newQuestions.size} question${if (newQuestions.size > 1) "s" else ""}!")
                                },
                                onRemoveQuestion = { viewModel.removeQuestion(it) },
                                onDuplicateQuestion = { viewModel.duplicateQuestion(it) },
                                onMoveQuestion = { from, to -> viewModel.moveQuestion(from, to) },
                                onUpdatePrompt = { idx, p -> viewModel.updateQuestionPrompt(idx, p) },
                                onUpdateType = { idx, t -> viewModel.updateQuestionType(idx, t) },
                                onUpdateOptionText = { qIdx, optIdx, text -> viewModel.updateOptionText(qIdx, optIdx, text) },
                                onAddOption = { viewModel.addOption(it) },
                                onRemoveOption = { qIdx, optIdx -> viewModel.removeOption(qIdx, optIdx) },
                                onToggleCorrectAnswer = { qIdx, letter -> viewModel.toggleCorrectAnswer(qIdx, letter) },
                                onUpdateHint = { idx, h -> viewModel.updateQuestionHint(idx, h) },
                                onUpdateExplanation = { idx, exp -> viewModel.updateQuestionExplanation(idx, exp) },
                                onBackClick = { viewModel.navigateBackStep() },
                                onSaveDraftClick = {
                                    viewModel.saveDraft { saved ->
                                        context.showToast("Draft saved successfully!")
                                        onQuizCreated(saved.id)
                                    }
                                },
                                onPublishClick = { viewModel.openPublishDialog() },
                                isLoading = uiState.isLoading,
                                onClearAllQuestions = { viewModel.clearAllQuestions() }
                            )
                        }
                    }
                }
            }

            // Dialog: AI Quiz Metadata Configuration (Title, Logo, Category)
            if (uiState.showAiMetadataDialog) {
                AiQuizMetadataDialog(
                    initialTitle = uiState.title,
                    initialLogo = uiState.selectedLogo,
                    initialCategories = uiState.categories,
                    onDismiss = { viewModel.dismissAiMetadataDialog() },
                    onConfirm = { title, logo, categories ->
                        viewModel.confirmAiMetadata(title, logo, categories)
                    }
                )
            }

            // Dialog: Publish Target Selection
            if (uiState.showPublishDialog) {
                PublishTargetDialog(
                    targetType = uiState.publishTargetType,
                    onTargetTypeChanged = { viewModel.setPublishTargetType(it) },
                    batches = uiState.availableBatches,
                    selectedBatchIds = uiState.targetBatchIds,
                    onToggleBatch = { viewModel.toggleBatchSelection(it) },
                    students = uiState.availableStudents,
                    selectedStudentIds = uiState.targetStudentIds,
                    onToggleStudent = { viewModel.toggleStudentSelection(it) },
                    hasScheduleWindow = uiState.hasScheduleWindow,
                    onToggleScheduleWindow = { viewModel.onToggleScheduleWindow(it) },
                    availabilityStartMillis = uiState.availabilityStartMillis,
                    onAvailabilityStartMillisChanged = { viewModel.onAvailabilityStartMillisChanged(it) },
                    availabilityEndMillis = uiState.availabilityEndMillis,
                    onAvailabilityEndMillisChanged = { viewModel.onAvailabilityEndMillisChanged(it) },
                    shuffleQuestionsAndOptions = uiState.shuffleQuestionsAndOptions,
                    onToggleShuffleQuestionsAndOptions = { viewModel.onToggleShuffleQuestionsAndOptions(it) },
                    quizTitle = uiState.title,
                    quizSubtitle = uiState.description,
                    quizLogo = uiState.selectedLogo,
                    difficulty = uiState.difficulty,
                    categories = uiState.categories,
                    questionCount = uiState.questions.size,
                    timerMode = uiState.timerMode,
                    onTimerModeChanged = { viewModel.onTimerModeChanged(it) },
                    timeLimitMinutes = uiState.timeLimitMinutes,
                    onTimeLimitMinutesChanged = { viewModel.onTimeLimitMinutesChanged(it) },
                    onDismiss = { viewModel.dismissPublishDialog() },
                    onConfirmPublish = {
                        viewModel.publishQuizConfirmed { publishedQuiz ->
                            // Success dialog will be shown
                        }
                    },
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage
                )
            }

            // Dialog: Publish Success & 6-character Code Display
            if (uiState.showPublishSuccessDialog && uiState.publishedQuiz != null) {
                PublishSuccessDialog(
                    quiz = uiState.publishedQuiz!!,
                    onDismiss = {
                        viewModel.dismissPublishSuccessDialog()
                        onQuizCreated(uiState.publishedQuiz!!.id)
                    },
                    onDone = {
                        viewModel.dismissPublishSuccessDialog()
                        onQuizCreated(uiState.publishedQuiz!!.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateQuizWizardHeader(
    currentStep: CreateQuizStep,
    creationMethod: QuizCreationMethod,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Back Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(QuizBuilderNavBtnBg)
                    .border(1.dp, QuizBuilderNavBtnBorder, CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = QuizBuilderNavBtnTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title with Sparkle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Quiz Builder",
                    color = QuizBuilderTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFC084FC),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Circular Close Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(QuizBuilderNavBtnBg)
                    .border(1.dp, QuizBuilderNavBtnBorder, CircleShape)
                    .clickable(onClick = onCloseClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = QuizBuilderTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Stepper Progress Indicators
        val step1Active = true
        val step2Active = currentStep != CreateQuizStep.METHOD_SELECT
        val step3Active = currentStep == CreateQuizStep.QUESTION_REVIEW

        val inactiveColor = QuizBuilderStepperInactive

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Step 1 Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (step1Active) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(inactiveColor, inactiveColor))
                        }
                    )
            )

            // Step 2 Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (step2Active) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(inactiveColor, inactiveColor))
                        }
                    )
            )

            // Step 3 Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (step3Active) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(inactiveColor, inactiveColor))
                        }
                    )
            )
        }
    }
}
