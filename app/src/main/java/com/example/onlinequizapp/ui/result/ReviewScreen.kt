package com.example.onlinequizapp.ui.result

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.ui.components.ExplanationCard
import com.example.onlinequizapp.ui.components.OptionItem
import com.example.onlinequizapp.ui.components.OptionVisualState
import com.example.onlinequizapp.ui.components.QuestionCardHeader
import com.example.onlinequizapp.ui.theme.AccentAmber
import com.example.onlinequizapp.ui.theme.BrandPrimary
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    quizId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    viewModel: ResultViewModel = viewModel(
        factory = ResultViewModel.provideFactory(quizId)
    )
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()

    val state = sessionState
    if (state == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    val questions = state.quiz.questions
    val safeInitialIndex = initialIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeInitialIndex)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Review Answers",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${state.correctCount} Correct • ${state.incorrectCount} Wrong • ${state.unansweredCount} Left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Results",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Question Quick-Jumping Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(questions) { idx, q ->
                    val ans = state.answers[q.id]
                    val isAns = ans?.isAnswered == true
                    val isCor = isAns && ans?.isCorrect == true

                    val (chipBg, chipBorder, chipText) = when {
                        isCor -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, SuccessGreen)
                        isAns -> Triple(DangerRed.copy(alpha = 0.15f), DangerRed, DangerRed)
                        else -> Triple(Color(0xFFF1F5F9), Color(0xFFCBD5E1), Color(0xFF64748B))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(idx)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Q${idx + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = chipText
                            )
                            if (isCor) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = chipText, modifier = Modifier.size(11.dp))
                            } else if (isAns) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = chipText, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
            }

            // Question Cards List
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(questions.size) { index ->
                    val question = questions[index]
                    val answer = state.answers[question.id]
                    val selectedIds = answer?.selectedOptionIds ?: emptySet()
                    val isAnswered = answer?.isAnswered == true
                    val isCorrect = isAnswered && answer?.isCorrect == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header Row: Question Number and Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${index + 1} of ${state.totalQuestions}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )
                                )

                                val (badgeText, badgeBg, badgeColor) = when {
                                    isCorrect -> Triple("✓ Correct", SuccessGreen.copy(alpha = 0.12f), SuccessGreen)
                                    isAnswered -> Triple("✕ Incorrect", DangerRed.copy(alpha = 0.12f), DangerRed)
                                    else -> Triple("Left / Skipped", Color(0xFFF1F5F9), Color(0xFF64748B))
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = badgeColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            QuestionCardHeader(
                                difficulty = question.difficulty,
                                questionType = question.type,
                                prompt = question.prompt,
                                modifier = Modifier.padding(0.dp)
                            )

                            // Informative Banner if user left/skipped the question
                            if (!isAnswered) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AccentAmber.copy(alpha = 0.1f))
                                        .border(1.dp, AccentAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = AccentAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "You left this question unanswered. The correct answer is highlighted below.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            question.options.forEach { option ->
                                val isCorrectAnswer = question.correctAnswers.contains(option.id)
                                val isUserSelected = selectedIds.contains(option.id)

                                val visualState = when {
                                    isCorrectAnswer -> OptionVisualState.CORRECT
                                    isUserSelected -> OptionVisualState.INCORRECT
                                    else -> OptionVisualState.NORMAL
                                }

                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    OptionItem(
                                        option = option,
                                        questionType = question.type,
                                        isSelected = isUserSelected,
                                        visualState = visualState,
                                        enabled = false,
                                        onSelect = { }
                                    )
                                }
                            }

                            if (question.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                ExplanationCard(explanation = question.explanation)
                            }
                        }
                    }
                }
            }
        }
    }
}
