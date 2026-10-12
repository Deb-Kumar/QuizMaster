package com.example.onlinequizapp.ui.home.profile

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.home.LocalQuizDark

@Composable
internal fun ChangePasswordDialog(
    isFirstTimeSetup: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (currentPassword: String?, newPassword: String, onResult: (Result<User>) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalQuizDark.current

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside { if (!isSaving) onDismiss() }
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row: Gradient Lock Icon + Title & Subtitle + Close Button
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
                                .background(
                                    Brush.linearGradient(
                                        if (isFirstTimeSetup) {
                                            listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                        } else {
                                            listOf(Color(0xFF059669), Color(0xFF10B981))
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFirstTimeSetup) Icons.Default.Key else Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isFirstTimeSetup) "Set Your Password" else "Change Password",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = if (isFirstTimeSetup) "Create a password for your account" else "Update your account password",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isSaving) onDismiss() },
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

                // Explanatory Banner for Social Users First Time
                if (isFirstTimeSetup) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Text(
                                text = "You logged in with Google or GitHub. Set your password now to enable direct email & password sign-in for your account.",
                                style = TextStyle(
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                // Error Message Banner (if any)
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = TextStyle(
                                color = Color(0xFFEF4444),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Input Fields
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Field 1: Current Password (Only when changing password)
                    if (!isFirstTimeSetup) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Current Password",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = {
                                    currentPassword = it
                                    errorMessage = null
                                },
                                placeholder = { Text("Enter your current password", color = textSecondary.copy(alpha = 0.6f), fontSize = 13.sp) },
                                singleLine = true,
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedBorderColor = Color(0xFF818CF8),
                                    unfocusedBorderColor = cardBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Field 2: Set Your Password / Set Your New Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isFirstTimeSetup) "Set Your Password" else "Set Your New Password",
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    text = if (isFirstTimeSetup) "Set Your password" else "Set Your new password",
                                    color = textSecondary.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = cardBg,
                                unfocusedContainerColor = cardBg,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = cardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 3: Confirm Your Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isFirstTimeSetup) "Confirm Your Password" else "Confirm Your New Password",
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    text = if (isFirstTimeSetup) "Confirm Your password" else "Confirm Your new password",
                                    color = textSecondary.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = cardBg,
                                unfocusedContainerColor = cardBg,
                                focusedBorderColor = Color(0xFF818CF8),
                                unfocusedBorderColor = cardBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = "Must be between 6 and 16 characters in length.",
                        style = TextStyle(
                            color = textSecondary,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Text(
                            text = "Cancel",
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val cur = currentPassword.trim()
                            val nPass = newPassword.trim()
                            val cPass = confirmPassword.trim()

                            if (!isFirstTimeSetup && cur.isBlank()) {
                                errorMessage = "Please enter your current password"
                                return@Button
                            }
                            if (nPass.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@Button
                            }
                            if (nPass.length > 16) {
                                errorMessage = "Password cannot exceed 16 characters"
                                return@Button
                            }
                            if (nPass != cPass) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }

                            isSaving = true
                            errorMessage = null
                            onSubmit(if (isFirstTimeSetup) null else cur, nPass) { result ->
                                isSaving = false
                                result.fold(
                                    onSuccess = {
                                        context.showToast(
                                            if (isFirstTimeSetup) "Password created successfully! 🔒"
                                            else "Password updated securely! 🔒"
                                        )
                                        onDismiss()
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "Failed to update password."
                                    }
                                )
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFirstTimeSetup) Color(0xFFEF4444) else Color(0xFF059669)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (isFirstTimeSetup) "Set Password" else "Update Password",
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
