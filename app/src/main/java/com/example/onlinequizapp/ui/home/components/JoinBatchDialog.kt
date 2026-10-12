package com.example.onlinequizapp.ui.home.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.BatchJoinResult
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.home.profile.ModalDivider
import kotlinx.coroutines.launch

private val PurplePrimary = Color(0xFF8B5CF6)
private val PurpleGradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
private val SuccessGreen = Color(0xFF10B981)
private val PendingAmber = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFEF4444)

/**
 * Join Batch Dialog for Students.
 *
 * Implements strict 6-character code formatting, case-insensitivity,
 * backend authorization and membership uniqueness checks.
 * Styled to match PersonalInfoDialog / AchievementsDialog modal standards.
 */
@Composable
fun JoinBatchDialog(
    studentId: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSuccessJoined: (Batch) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var batchCodeInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var joinResultState by remember { mutableStateOf<BatchJoinResult?>(null) }

    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    fun submitJoin() {
        val cleanCode = batchCodeInput.trim().uppercase()
        if (cleanCode.length != 6) {
            errorMessage = "Please enter a valid 6-character code."
            return
        }

        isSubmitting = true
        errorMessage = null

        coroutineScope.launch {
            val savedUser = try { com.example.onlinequizapp.data.session.AuthSessionManager.getSavedUser() } catch (_: Exception) { null }
            val effectiveStudentId = studentId.ifBlank { savedUser?.id ?: "" }
            val studentName = savedUser?.name ?: ""
            val studentEmail = savedUser?.email ?: ""

            val result = BatchRepositoryImpl.instance.joinBatch(
                studentId = effectiveStudentId,
                batchCode = cleanCode,
                studentName = studentName,
                studentEmail = studentEmail
            )
            isSubmitting = false

            result.fold(
                onSuccess = { joinRes ->
                    joinResultState = joinRes
                    if (joinRes is BatchJoinResult.Joined) {
                        onSuccessJoined(joinRes.batch)
                    }
                },
                onFailure = { ex ->
                    errorMessage = ex.message ?: "Failed to join batch."
                }
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (joinResultState == null) {
                    // Header Row with Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PurpleGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Join Batch",
                                    style = TextStyle(
                                        color = textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                )
                                Text(
                                    text = "Enroll with teacher's join code",
                                    style = TextStyle(
                                        color = textSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    ModalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                    Text(
                        text = "Enter the 6-character joining code provided by your teacher to enroll in their batch.",
                        fontSize = 13.sp,
                        color = textSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 6-Character Monospaced Input Box
                    OutlinedTextField(
                        value = batchCodeInput,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                            batchCodeInput = filtered
                            errorMessage = null
                        },
                        label = {
                            Text(
                                text = "6-Character Join Code",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        placeholder = {
                            Text(
                                text = "6-Character Join Code",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = textSecondary.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center,
                            color = textPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitJoin() }
                        ),
                        supportingText = {
                            Text(
                                text = "${batchCodeInput.length}/6 characters",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = if (batchCodeInput.length == 6) PurplePrimary else textSecondary,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg
                        )
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { error ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorRed.copy(alpha = 0.12f))
                                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = error,
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Text("Cancel", color = textSecondary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { submitJoin() },
                            enabled = !isSubmitting && batchCodeInput.length == 6,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Join Batch", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    val result = joinResultState!!
                    when (result) {
                        is BatchJoinResult.Joined -> {
                            val batch = result.batch
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.18f))
                                    .align(Alignment.CenterHorizontally),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "Successfully Joined!",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Text(
                                text = "You are now enrolled in:",
                                fontSize = 13.sp,
                                color = textSecondary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = batch.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    }
                                    if (batch.description.isNotBlank()) {
                                        Text(
                                            text = batch.description,
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                    Text(
                                        text = "Code: ${batch.batchCode} • ${batch.studentCount} Students",
                                        fontSize = 11.5.sp,
                                        color = PurplePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text("View My Batches", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        is BatchJoinResult.RequestSubmitted -> {
                            val batch = result.batch
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(PendingAmber.copy(alpha = 0.18f))
                                    .align(Alignment.CenterHorizontally),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = PendingAmber,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Text(
                                text = "Join Request Sent!",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Text(
                                text = "This is a Private Batch. Your request has been sent to the teacher for approval.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = batch.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                    }
                                    if (batch.description.isNotBlank()) {
                                        Text(
                                            text = batch.description,
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PendingAmber.copy(alpha = 0.15f))
                                                .border(0.8.dp, PendingAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Status: Pending Approval",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PendingAmber
                                            )
                                        }
                                        Text(
                                            text = "Code: ${batch.batchCode}",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
