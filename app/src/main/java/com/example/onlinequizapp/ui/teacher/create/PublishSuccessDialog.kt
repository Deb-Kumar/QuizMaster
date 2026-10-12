package com.example.onlinequizapp.ui.teacher.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
fun PublishSuccessDialog(
    quiz: Quiz,
    onDismiss: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isTeacherInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDark) Color(0xFF0F172A) else Color.White)
                .border(1.dp, if (isDark) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFA7F3D0), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5))
                        .border(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFF34D399), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Quiz Published!",
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                val isPublicQuiz = quiz.targetBatchIds.isEmpty() && quiz.targetStudentIds.isEmpty()

                Text(
                    text = if (isPublicQuiz) {
                        "\"${quiz.title}\" is published as a Public Quiz. Anyone with this code can attempt it!"
                    } else {
                        "\"${quiz.title}\" is now active and ready for your students."
                    },
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                // 6-Character Quiz Access Code Display Box
                val codeGradient = if (isDark) {
                    Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(codeGradient)
                        .border(1.5.dp, if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1), RoundedCornerShape(16.dp))
                        .clickable {
                            copyQuizCodeToClipboard(context, quiz.quizCode)
                        }
                        .padding(vertical = 18.dp, horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPublicQuiz) "OFFICIAL PUBLIC QUIZ CODE" else "OFFICIAL QUIZ CODE",
                            color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = quiz.quizCode,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF312E81) else Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tap to copy code",
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Clarification note regarding Quiz Code vs Batch Code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPublicQuiz) {
                                "This is a Public quiz! Anyone can enter this 6-character code in the \"Join Quiz\" screen on their home dashboard to attempt this quiz."
                            } else {
                                "Share this 6-character Quiz Code with students. They can enter it on their home dashboard to access this specific quiz."
                            },
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Done Button
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

private fun copyQuizCodeToClipboard(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Quiz Code", code)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Quiz Code $code copied to clipboard!", Toast.LENGTH_SHORT).show()
}
