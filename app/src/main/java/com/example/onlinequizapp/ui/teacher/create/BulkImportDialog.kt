package com.example.onlinequizapp.ui.teacher.create

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import com.example.onlinequizapp.ui.teacher.components.TeacherConfirmationDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.domain.model.QuestionDraft
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
fun BulkImportDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onImport: (List<QuestionDraft>) -> Unit
) {
    val isDark = isTeacherInDarkTheme()
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf(initialText) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val parsedQuestions = remember(inputText) {
        if (inputText.isNotBlank()) BulkQuestionParser.parse(inputText) else emptyList()
    }

    val dialogBg = if (isDark) Color(0xFF0F172A) else Color.White
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, dialogBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Paste & Split Questions",
                                    color = textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Paste questions copied from any document or website",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary
                        )
                    }
                }

                // Quick Action Bar: "Paste from Clipboard" & "Clear"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .clickable {
                                clipboardManager.getText()?.text?.let { clipText ->
                                    if (clipText.isNotBlank()) {
                                        inputText = clipText
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Paste from Clipboard",
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (inputText.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF450A0A).copy(alpha = 0.5f) else Color(0xFFFEF2F2))
                                .border(1.dp, if (isDark) Color(0xFF991B1B) else Color(0xFFFECACA), RoundedCornerShape(8.dp))
                                .clickable { showClearConfirmDialog = true }
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "All Clear",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "All Clear",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Multiline Input TextField
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Paste questions here...\n\nFormat:\n1. Question prompt\nA) Option A\nB) Option B\nC) Option C\nD) Option D\nCorrect Answer: A",
                            color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF090D1A) else Color(0xFFF8FAFC),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        unfocusedBorderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                    )
                )

                // Live Preview / Detection Banner
                if (parsedQuestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFECFDF5))
                            .border(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFFA7F3D0), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "✨ Detected ${parsedQuestions.size} question${if (parsedQuestions.size > 1) "s" else ""} ready to import",
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Compact preview of detected questions
                    Text(
                        text = "Preview of detected questions:",
                        color = textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(parsedQuestions) { idx, q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = q.prompt.ifBlank { "Question ${idx + 1}" },
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${q.options.count { it.isNotBlank() }} opts",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else if (inputText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF451A03).copy(alpha = 0.35f) else Color(0xFFFFFBEB))
                            .border(1.dp, if (isDark) Color(0xFFD97706) else Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "No questions detected yet. Ensure each question has a prompt followed by options like A) ... B) ...",
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                            fontSize = 12.sp
                        )
                    }
                }

                // Action Buttons: Cancel & Import
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) Color.Transparent else Color.White,
                            contentColor = textSecondary
                        )
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (parsedQuestions.isNotEmpty()) {
                                onImport(parsedQuestions)
                            }
                        },
                        enabled = parsedQuestions.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5),
                            disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                        )
                    ) {
                        Text(
                            text = if (parsedQuestions.isNotEmpty()) "Import ${parsedQuestions.size} Questions" else "Import Questions",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for All Clear
    if (showClearConfirmDialog) {
        TeacherConfirmationDialog(
            title = "Clear All Text?",
            subtitle = "Reset input content",
            message = "Are you sure you want to clear all entered text? Any pasted questions will be removed from this input.",
            confirmText = "Clear All",
            onConfirm = {
                inputText = ""
                showClearConfirmDialog = false
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }
}
