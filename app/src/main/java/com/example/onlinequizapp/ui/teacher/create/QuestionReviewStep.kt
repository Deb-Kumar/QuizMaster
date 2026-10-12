package com.example.onlinequizapp.ui.teacher.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.onlinequizapp.ui.teacher.components.TeacherConfirmationDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.QuestionDraft
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

private val Letters = listOf("A", "B", "C", "D", "E", "F")

@Composable
fun QuestionReviewStep(
    questions: List<QuestionDraft>,
    allowHints: Boolean = false,
    onAddQuestion: () -> Unit,
    onImportBulkQuestions: (List<QuestionDraft>, Int?) -> Unit = { _, _ -> },
    onRemoveQuestion: (Int) -> Unit,
    onDuplicateQuestion: (Int) -> Unit,
    onMoveQuestion: (Int, Int) -> Unit,
    onUpdatePrompt: (Int, String) -> Unit,
    onUpdateType: (Int, QuestionType) -> Unit,
    onUpdateOptionText: (Int, Int, String) -> Unit,
    onAddOption: (Int) -> Unit,
    onRemoveOption: (Int, Int) -> Unit,
    onToggleCorrectAnswer: (Int, String) -> Unit,
    onUpdateHint: (Int, String) -> Unit,
    onUpdateExplanation: (Int, String) -> Unit,
    onBackClick: () -> Unit,
    onSaveDraftClick: () -> Unit,
    onPublishClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClearAllQuestions: (() -> Unit)? = null
) {
    val isDark = isTeacherInDarkTheme()
    var showBulkDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    if (showBulkDialog) {
        BulkImportDialog(
            onDismiss = { showBulkDialog = false },
            onImport = { imported ->
                onImportBulkQuestions(imported, null)
                showBulkDialog = false
            }
        )
    }

    if (showClearAllConfirm) {
        TeacherConfirmationDialog(
            title = "Delete All Questions?",
            subtitle = "Permanent action",
            message = "Are you sure you want to delete all ${questions.size} question${if (questions.size > 1) "s" else ""}? This action cannot be undone.",
            confirmText = "Delete All",
            onConfirm = {
                onClearAllQuestions?.invoke()
                showClearAllConfirm = false
            },
            onDismiss = { showClearAllConfirm = false }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Review Header Banner
        val bannerBg = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.5f) else Color.White
        val bannerBorder = if (isDark) Color(0xFF4338CA) else Color(0xFFC7D2FE)
        val bannerTitle = if (isDark) Color.White else Color(0xFF1E1B4B)
        val bannerSubtitle = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bannerBg)
                .border(1.dp, bannerBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Question Review",
                            color = bannerTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${questions.size} Questions",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Action buttons: "All Clear" (if questions exist) and "Paste Questions"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onClearAllQuestions != null && questions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF450A0A).copy(alpha = 0.5f) else Color(0xFFFEF2F2))
                                    .border(1.dp, if (isDark) Color(0xFF991B1B) else Color(0xFFFECACA), RoundedCornerShape(8.dp))
                                    .clickable { showClearAllConfirm = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "All Clear",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "All Clear",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Paste / Bulk Import header pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF))
                                .border(1.dp, if (isDark) Color(0xFF6366F1) else Color(0xFFC7D2FE), RoundedCornerShape(8.dp))
                                .clickable { showBulkDialog = true }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste Questions",
                                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Paste Questions",
                                color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Review, edit, delete, or add questions before publishing.",
                    color = bannerSubtitle,
                    fontSize = 12.sp
                )
            }
        }

        // Empty state when no questions exist
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF8FAFC))
                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(vertical = 36.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No questions in this quiz yet",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use 'Paste Questions' to paste and split questions or 'Add Question' below.",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 12.5.sp
                    )
                }
            }
        }

        // List of question draft review cards
        questions.forEachIndexed { qIdx, qDraft ->
            QuestionReviewCard(
                index = qIdx,
                totalQuestions = questions.size,
                draft = qDraft,
                allowHints = allowHints,
                onPromptChanged = { onUpdatePrompt(qIdx, it) },
                onBulkPaste = { imported -> onImportBulkQuestions(imported, qIdx) },
                onOpenPasteDialog = { showBulkDialog = true },
                onTypeChanged = { onUpdateType(qIdx, it) },
                onOptionChanged = { optIdx, text -> onUpdateOptionText(qIdx, optIdx, text) },
                onAddOption = { onAddOption(qIdx) },
                onRemoveOption = { optIdx -> onRemoveOption(qIdx, optIdx) },
                onToggleCorrect = { letter -> onToggleCorrectAnswer(qIdx, letter) },
                onHintChanged = { onUpdateHint(qIdx, it) },
                onExplanationChanged = { onUpdateExplanation(qIdx, it) },
                onDuplicate = { onDuplicateQuestion(qIdx) },
                onDelete = { onRemoveQuestion(qIdx) },
                onMoveUp = if (qIdx > 0) { { onMoveQuestion(qIdx, qIdx - 1) } } else null,
                onMoveDown = if (qIdx < questions.size - 1) { { onMoveQuestion(qIdx, qIdx + 1) } } else null
            )
        }

        // Add Question & Paste Questions Buttons
        val addBtnBg = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.35f) else Color(0xFFEEF2FF)
        val addBtnContent = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
        val addBtnBorder = if (isDark) Color(0xFF6366F1) else Color(0xFF818CF8)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onAddQuestion,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = addBtnBg,
                    contentColor = addBtnContent
                ),
                border = BorderStroke(1.dp, addBtnBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = addBtnContent,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Question",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = addBtnContent
                )
            }

            OutlinedButton(
                onClick = { showBulkDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isDark) Color(0xFF312E81).copy(alpha = 0.35f) else Color(0xFFF5F3FF),
                    contentColor = if (isDark) Color(0xFFC4B5FD) else Color(0xFF7C3AED)
                ),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF8B5CF6) else Color(0xFFDDD6FE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFFC4B5FD) else Color(0xFF7C3AED),
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Paste Questions",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFC4B5FD) else Color(0xFF7C3AED)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons Row (Save Draft, Publish Quiz)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val backBorder = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
            val backContent = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
            val backContainer = if (isDark) Color.Transparent else Color.White

            OutlinedButton(
                onClick = onBackClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = backContainer,
                    contentColor = backContent
                ),
                border = BorderStroke(1.dp, backBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(0.9f).height(48.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back", fontSize = 13.sp)
            }

            val draftBorder = if (isDark) Color(0xFF0284C7) else Color(0xFF7DD3FC)
            val draftContent = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            val draftContainer = if (isDark) Color.Transparent else Color(0xFFF0F9FF)

            OutlinedButton(
                onClick = onSaveDraftClick,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = draftContainer,
                    contentColor = draftContent
                ),
                border = BorderStroke(1.dp, draftBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.1f).height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Draft", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = onPublishClick,
                enabled = !isLoading && questions.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.3f).height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Quiz", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun QuestionReviewCard(
    index: Int,
    totalQuestions: Int,
    draft: QuestionDraft,
    allowHints: Boolean = false,
    onPromptChanged: (String) -> Unit,
    onBulkPaste: (List<QuestionDraft>) -> Unit = {},
    onOpenPasteDialog: () -> Unit = {},
    onTypeChanged: (QuestionType) -> Unit,
    onOptionChanged: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onToggleCorrect: (String) -> Unit,
    onHintChanged: (String) -> Unit,
    onExplanationChanged: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    val isDark = isTeacherInDarkTheme()
    var expandedDetails by remember { mutableStateOf(false) }

    val cardBg = if (isDark) Color(0xFF0F172A) else Color.White
    val cardBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Card Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Q${index + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    var typeDropdownExpanded by remember { mutableStateOf(false) }

                    val typeBg = if (isDark) {
                        if (draft.type == QuestionType.MULTIPLE) Color(0xFF312E81).copy(alpha = 0.6f) else Color(0xFF1E293B)
                    } else {
                        if (draft.type == QuestionType.MULTIPLE) Color(0xFFEEF2FF) else Color(0xFFF1F5F9)
                    }
                    val typeBorder = if (isDark) {
                        if (draft.type == QuestionType.MULTIPLE) Color(0xFF818CF8) else Color(0xFF334155)
                    } else {
                        if (draft.type == QuestionType.MULTIPLE) Color(0xFFC7D2FE) else Color(0xFFCBD5E1)
                    }
                    val typeText = if (isDark) {
                        if (draft.type == QuestionType.MULTIPLE) Color(0xFFA5B4FC) else Color(0xFFE2E8F0)
                    } else {
                        if (draft.type == QuestionType.MULTIPLE) Color(0xFF4338CA) else Color(0xFF334155)
                    }

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(typeBg)
                                .border(1.dp, typeBorder, RoundedCornerShape(8.dp))
                                .clickable { typeDropdownExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (draft.type == QuestionType.MULTIPLE) "Multiple Choice" else "Single Choice",
                                color = typeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change Question Type",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false },
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF0F172A) else Color.White)
                                .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (draft.type == QuestionType.SINGLE) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "Single Choice",
                                            color = if (draft.type == QuestionType.SINGLE) (if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)) else (if (isDark) Color.White else Color(0xFF0F172A)),
                                            fontSize = 13.sp,
                                            fontWeight = if (draft.type == QuestionType.SINGLE) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    onTypeChanged(QuestionType.SINGLE)
                                    typeDropdownExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (draft.type == QuestionType.MULTIPLE) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "Multiple Choice",
                                            color = if (draft.type == QuestionType.MULTIPLE) (if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)) else (if (isDark) Color.White else Color(0xFF0F172A)),
                                            fontSize = 13.sp,
                                            fontWeight = if (draft.type == QuestionType.MULTIPLE) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    onTypeChanged(QuestionType.MULTIPLE)
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Controls: Move up/down, Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onMoveUp != null) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                        }
                    }
                    if (onMoveDown != null) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Question Prompt Input with smart bulk paste detection
            OutlinedTextField(
                value = draft.prompt,
                onValueChange = { newText ->
                    if (BulkQuestionParser.isBulkText(newText)) {
                        val parsed = BulkQuestionParser.parse(newText)
                        if (parsed.isNotEmpty()) {
                            onBulkPaste(parsed)
                        } else {
                            onPromptChanged(newText)
                        }
                    } else {
                        onPromptChanged(newText)
                    }
                },
                label = { Text("Question Prompt", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontSize = 12.sp) },
                placeholder = { Text("Enter question prompt or paste copied questions...", color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)) },
                trailingIcon = {
                    IconButton(onClick = onOpenPasteDialog) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste / Split Questions",
                            tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                    unfocusedBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1),
                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    focusedContainerColor = if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC),
                    unfocusedContainerColor = if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (draft.type == QuestionType.MULTIPLE) "Options (Tap letters to select one or more correct answers):" else "Options (Tap letter to set as correct answer):",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                draft.options.forEachIndexed { optIdx, optText ->
                    val letter = Letters.getOrElse(optIdx) { "opt" }
                    val isCorrect = draft.correctAnswers.contains(letter)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter badge (toggleable as correct)
                        val letterBg = if (isCorrect) {
                            if (isDark) Color(0xFF10B981) else Color(0xFF059669)
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        }
                        val letterBorder = if (isCorrect) {
                            if (isDark) Color(0xFF34D399) else Color(0xFF10B981)
                        } else {
                            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                        }
                        val letterTextColor = if (isCorrect) {
                            Color.White
                        } else {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(letterBg)
                                .border(1.dp, letterBorder, RoundedCornerShape(8.dp))
                                .clickable { onToggleCorrect(letter) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                color = letterTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Option text field
                        val optFieldContainer = if (isCorrect) {
                            if (isDark) Color(0xFF064E3B).copy(alpha = 0.25f) else Color(0xFFECFDF5)
                        } else {
                            if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC)
                        }
                        val optFieldBorder = if (isCorrect) {
                            if (isDark) Color(0xFF059669) else Color(0xFF10B981)
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                        }
                        val optFieldFocusedBorder = if (isCorrect) {
                            if (isDark) Color(0xFF10B981) else Color(0xFF059669)
                        } else {
                            if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                        }

                        OutlinedTextField(
                            value = optText,
                            onValueChange = { onOptionChanged(optIdx, it) },
                            placeholder = { Text("Option $letter text...", color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8), fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = optFieldFocusedBorder,
                                unfocusedBorderColor = optFieldBorder,
                                focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                focusedContainerColor = optFieldContainer,
                                unfocusedContainerColor = optFieldContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        if (draft.options.size > 2) {
                            IconButton(onClick = { onRemoveOption(optIdx) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove option", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (draft.options.size < 6) {
                    Text(
                        text = "+ Add Option",
                        color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(onClick = onAddOption)
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // Question Hint (Only shown when hints are enabled in Quiz settings)
            if (allowHints) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDetails = !expandedDetails }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (draft.hint.isNotBlank()) "Hint: ${draft.hint.take(25)}..." else "Add Hint (Optional)",
                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = if (expandedDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = expandedDetails) {
                    OutlinedTextField(
                        value = draft.hint,
                        onValueChange = onHintChanged,
                        label = { Text("Hint (Optional clue for students)", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), fontSize = 12.sp) },
                        placeholder = { Text("Enter a hint to help students...", color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            unfocusedBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            focusedContainerColor = if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
