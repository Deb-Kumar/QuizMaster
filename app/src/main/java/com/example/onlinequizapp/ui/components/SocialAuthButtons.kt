package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.UserRole

@Composable
fun GoogleIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Icon(
        painter = painterResource(id = R.drawable.ic_google_logo),
        contentDescription = "Google",
        tint = Color.Unspecified,
        modifier = modifier
    )
}

@Composable
fun GithubIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Icon(
        painter = painterResource(id = R.drawable.ic_github_logo),
        contentDescription = "GitHub",
        tint = Color.White,
        modifier = modifier
    )
}

@Composable
fun SocialAuthSection(
    onGoogleClick: () -> Unit,
    onGithubClick: () -> Unit,
    isRegister: Boolean = false,
    modifier: Modifier = Modifier,
    buttonHeight: Dp = 52.dp,
    spacing: Dp = 10.dp
) {
    val actionText = "Continue"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Divider with label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFF1E284D)
            )
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 14.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFF1E284D)
            )
        }

        // Google Button (Centered Logo & Text, No Right Arrow)
        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E284D)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF0F1532)
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                GoogleIcon()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$actionText with Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // GitHub Button (Centered Logo & Text, No Right Arrow)
        OutlinedButton(
            onClick = onGithubClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E284D)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF0F1532)
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                GithubIcon()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$actionText with GitHub",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun GithubSignInDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, photoUrl: String?) -> Unit,
    role: UserRole = UserRole.STUDENT
) {
    if (!isOpen) return

    var username by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1532)),
                border = BorderStroke(1.5.dp, Color(0xFF6366F1).copy(alpha = 0.8f))
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // GitHub Logo badge
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E284D)),
                    contentAlignment = Alignment.Center
                ) {
                    GithubIcon(modifier = Modifier.size(34.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "GitHub Sign-In",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your GitHub username or handle to continue directly:",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (isInputError) isInputError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("GitHub Username / Email") },
                    placeholder = { Text("e.g. devkumar or octocat", color = Color(0xFF64748B)) },
                    singleLine = true,
                    isError = isInputError,
                    supportingText = {
                        if (isInputError) {
                            Text("Please enter a username or handle", color = Color(0xFFEF4444))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF070B1E),
                        unfocusedContainerColor = Color(0xFF070B1E),
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF1E284D),
                        focusedLabelColor = Color(0xFF818CF8),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        cursorColor = Color(0xFF818CF8)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick presets
                val defaultHandle = if (role == UserRole.TEACHER) "teacher_coder" else "student_dev"
                SuggestionChip(
                    onClick = { username = defaultHandle },
                    label = { Text("Quick fill: @$defaultHandle", fontSize = 12.sp, color = Color(0xFF38BDF8)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFF1E284D).copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            val trimmed = username.trim().removePrefix("@")
                            if (trimmed.isBlank()) {
                                isInputError = true
                            } else {
                                val cleanHandle = trimmed.substringBefore("@").replace(" ", "").trim()
                                val email = if (trimmed.contains("@")) trimmed else "$cleanHandle@github.com"
                                val name = cleanHandle.replaceFirstChar { it.uppercase() }
                                val photoUrl = if (cleanHandle.isNotBlank()) "https://github.com/$cleanHandle.png" else null
                                onConfirm(name, email, photoUrl)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF2563EB))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
