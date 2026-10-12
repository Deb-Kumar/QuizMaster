package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.onlinequizapp.R
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.UserRole

// Cyber-Dark Auth Palette Matching Mockups
val AuthBg = Color(0xFF070B1E)
val AuthSurface = Color(0xFF0F1532)
val AuthBorder = Color(0xFF1E284D)
val AuthBorderFocused = Color(0xFF6366F1)
val AuthPrimaryGradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF2563EB)))
val AuthTabGradient = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6)))
val AuthAccentCyan = Color(0xFF38BDF8)
val AuthAccentPurple = Color(0xFF818CF8)
val AuthTextMuted = Color(0xFF94A3B8)
val AuthTextDim = Color(0xFF64748B)

@Composable
fun AuthBackgroundCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Solid deep midnight background
        drawRect(color = AuthBg)

        // Top Left Purple Ambient Glow Orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.08f, h * 0.12f),
                radius = 220f
            ),
            radius = 220f,
            center = Offset(w * 0.08f, h * 0.12f)
        )

        // Top Left Purple Organic Curve
        val topLeftPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.44f, 0f)
            cubicTo(w * 0.38f, h * 0.08f, w * 0.16f, h * 0.13f, 0f, h * 0.11f)
            close()
        }
        drawPath(
            path = topLeftPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF7C3AED), Color(0xFF6366F1)),
                start = Offset(0f, 0f),
                end = Offset(w * 0.44f, h * 0.11f)
            )
        )

        // Bottom Left Purple Curve
        val bottomLeftPath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.93f)
            cubicTo(w * 0.18f, h * 0.92f, w * 0.34f, h * 0.97f, w * 0.45f, h)
            close()
        }
        drawPath(
            path = bottomLeftPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                start = Offset(0f, h * 0.93f),
                end = Offset(w * 0.45f, h)
            )
        )

        // Bottom Right Cyan/Blue Curve
        val bottomRightPath = Path().apply {
            moveTo(w, h)
            lineTo(w, h * 0.93f)
            cubicTo(w * 0.82f, h * 0.92f, w * 0.66f, h * 0.97f, w * 0.55f, h)
            close()
        }
        drawPath(
            path = bottomRightPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8)),
                start = Offset(w, h * 0.93f),
                end = Offset(w * 0.55f, h)
            )
        )
    }
}

@Composable
fun AuthHeaderBadge(
    isTeacher: Boolean = false,
    isRegister: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isRegister) 130.dp else 115.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Top-Left Cyan Ring
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-80).dp, y = 14.dp)
                .size(15.dp)
                .border(width = 3.dp, color = Color(0xFF38BDF8), shape = CircleShape)
        )

        // Floating Yellow 4-point Sparkle Star (Left)
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-68).dp, y = (-24).dp)
                .size(20.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val s = size.width / 2f
            val path = Path().apply {
                moveTo(cx, cy - s)
                cubicTo(cx, cy - s * 0.22f, cx + s * 0.22f, cy, cx + s, cy)
                cubicTo(cx + s * 0.22f, cy, cx, cy + s * 0.22f, cx, cy + s)
                cubicTo(cx, cy - s * 0.22f, cx - s * 0.22f, cy, cx - s, cy)
                cubicTo(cx - s * 0.22f, cy, cx, cy - s * 0.22f, cx, cy - s)
                close()
            }
            drawPath(path = path, color = Color(0xFFFBBF24))
        }

        // Floating Cyan Top-Left Dash
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-46).dp, y = (-38).dp)
                .size(width = 12.dp, height = 4.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF38BDF8))
        )

        if (!isRegister) {
            // Floating Yellow 4-point Sparkle Star (Top Right)
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 68.dp, y = (-26).dp)
                    .size(22.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val s = size.width / 2f
                val path = Path().apply {
                    moveTo(cx, cy - s)
                    cubicTo(cx, cy - s * 0.22f, cx + s * 0.22f, cy, cx + s, cy)
                    cubicTo(cx + s * 0.22f, cy, cx, cy + s * 0.22f, cx, cy + s)
                    cubicTo(cx, cy - s * 0.22f, cx - s * 0.22f, cy, cx - s, cy)
                    cubicTo(cx - s * 0.22f, cy, cx, cy - s * 0.22f, cx, cy - s)
                    close()
                }
                drawPath(path = path, color = Color(0xFFFBBF24))
            }

            // Decorative sprinkle pill dashes
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 76.dp, y = (-8).dp)
                    .size(width = 14.dp, height = 4.dp)
                    .rotate(-35f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF38BDF8))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 72.dp, y = 16.dp)
                    .size(width = 12.dp, height = 4.dp)
                    .rotate(35f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF818CF8))
            )
        } else {
            // Floating Cyan Glowing Orb (Far Top Right) matching Reference Mockup
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 138.dp, y = (-42).dp)
                    .size(18.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0xFF38BDF8), ambientColor = Color(0xFF0284C7))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF67E8F9),
                                Color(0xFF0284C7),
                                Color(0xFF0369A1)
                            )
                        )
                    )
            )
        }

        // Central Glowing Graduation Cap Badge
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(86.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF6366F1),
                    ambientColor = Color(0xFF3B82F6)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFF6366F1),
                            Color(0xFF3B82F6)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isTeacher) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.School,
                contentDescription = "Role Logo",
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }

        if (isRegister) {
            // Floating 3D Stacked Books with Pencil Graphic
            Image(
                painter = painterResource(id = R.drawable.img_3d_books_pencil),
                contentDescription = "3D Books and Pencil",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 88.dp, y = (-14).dp)
                    .size(width = 96.dp, height = 68.dp)
            )
        }
    }
}

@Composable
fun AuthRoleSelector(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AuthSurface),
        border = BorderStroke(1.dp, AuthBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isStudent = selectedRole == UserRole.STUDENT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isStudent) {
                            Modifier
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF6366F1))
                                .background(AuthTabGradient)
                        } else {
                            Modifier.background(Color.Transparent)
                        }
                    )
                    .clickable(role = Role.Tab) {
                        onRoleSelected(UserRole.STUDENT)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (isStudent) Color.White else AuthTextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Student",
                        fontSize = 14.sp,
                        fontWeight = if (isStudent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isStudent) Color.White else AuthTextMuted
                    )
                }
            }

            val isTeacher = selectedRole == UserRole.TEACHER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isTeacher) {
                            Modifier
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF6366F1))
                                .background(AuthTabGradient)
                        } else {
                            Modifier.background(Color.Transparent)
                        }
                    )
                    .clickable(role = Role.Tab) {
                        onRoleSelected(UserRole.TEACHER)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = if (isTeacher) Color.White else AuthTextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Teacher",
                        fontSize = 14.sp,
                        fontWeight = if (isTeacher) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTeacher) Color.White else AuthTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    fieldHeight: Dp = 52.dp,
    maxLength: Int? = null,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (!readOnly && (maxLength == null || input.length <= maxLength)) {
                onValueChange(input)
            }
        },
        readOnly = readOnly,
        placeholder = {
            Text(text = placeholder, color = AuthTextDim, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = AuthTextMuted,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = AuthSurface,
            unfocusedContainerColor = AuthSurface,
            focusedBorderColor = AuthBorderFocused,
            unfocusedBorderColor = AuthBorder,
            cursorColor = AuthAccentPurple
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(fieldHeight)
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonHeight: Dp = 52.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF6366F1),
                ambientColor = Color(0xFF3B82F6)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0xFF1E284D)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AuthPrimaryGradient),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStepHeader(
    stepText: String,
    titleMain: String,
    titleAccent: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step pill with glowing pulse dot
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AuthAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .background(Color(0xFF0F1532))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AuthAccentCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stepText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthAccentCyan,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compact glowing icon (54dp)
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF6366F1),
                    ambientColor = Color(0xFF3B82F6)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFF6366F1),
                            Color(0xFF3B82F6)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$titleMain ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = titleAccent,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = AuthAccentPurple
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = AuthTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun CyberSelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AuthAccentPurple else AuthBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                if (isSelected) {
                    AuthTabGradient
                } else {
                    Brush.linearGradient(listOf(Color(0xFF0F1532), Color(0xFF0F1532)))
                }
            )
            .clickable(
                role = Role.RadioButton,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(13.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else AuthTextMuted
            )
        }
    }
}

@Composable
fun OnboardingProgressBar(
    currentStep: Int,
    totalSteps: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP $currentStep OF $totalSteps",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuthAccentCyan,
                letterSpacing = 1.sp
            )
            Text(
                text = "${(currentStep * 100) / totalSteps}% Complete",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AuthTextMuted
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isCompleted) {
                                AuthTabGradient
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF1E284D), Color(0xFF1E284D)))
                            }
                        )
                )
            }
        }
    }
}


