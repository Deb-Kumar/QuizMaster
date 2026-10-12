package com.example.onlinequizapp.ui.home.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.UserAvatar
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.home.LocalQuizDark
import java.io.File
import java.io.FileOutputStream

/**
 * Saves a captured Bitmap into app-internal storage and returns a file URI string.
 */
fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, userId: String): String? {
    return try {
        val dir = File(context.filesDir, "profile_avatars")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "avatar_${userId.ifBlank { "guest" }}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        null
    }
}

/**
 * Copies a selected gallery content URI to app-internal storage and returns a permanent file URI string.
 */
fun copyUriToInternalStorage(context: Context, uri: Uri, userId: String): String? {
    return try {
        val dir = File(context.filesDir, "profile_avatars")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "avatar_${userId.ifBlank { "guest" }}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        null
    }
}

/**
 * Modern modal showing options:
 * 1. See profile picture
 * 2. Upload profile picture (Capture from Camera & Upload from Gallery)
 */
@Composable
internal fun ProfilePictureOptionsDialog(
    user: User?,
    defaultDrawableRes: Int = R.drawable.img_student_hero,
    onDismiss: () -> Unit,
    onSeePictureClick: () -> Unit,
    onCaptureFromCameraClick: () -> Unit,
    onUploadFromGalleryClick: () -> Unit
) {
    val isDark = LocalQuizDark.current
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color.White
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 20.dp, vertical = 24.dp),
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
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(
                            avatarUrl = user?.avatarUrl,
                            name = user?.name ?: "User",
                            size = 46.dp,
                            defaultDrawableRes = defaultDrawableRes
                        )

                        Column {
                            Text(
                                text = "Profile Picture",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = user?.name ?: "Manage your photo",
                                style = TextStyle(
                                    color = textMuted,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                    thickness = 1.dp
                )

                // Option 1: See profile picture
                Text(
                    text = "VIEW OPTION",
                    style = TextStyle(
                        color = textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )

                ProfileOptionItem(
                    icon = Icons.Default.Visibility,
                    iconGradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                    title = "See profile picture",
                    description = "View your full-size profile photo",
                    isDark = isDark,
                    onClick = onSeePictureClick
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Option 2: Upload profile picture header
                Text(
                    text = "UPLOAD PROFILE PICTURE",
                    style = TextStyle(
                        color = textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )

                // Sub-option A: Capture from Camera
                ProfileOptionItem(
                    icon = Icons.Default.CameraAlt,
                    iconGradient = listOf(Color(0xFF2563EB), Color(0xFF06B6D4)),
                    title = "Capture from Camera",
                    description = "Take a new photo with device camera",
                    isDark = isDark,
                    onClick = onCaptureFromCameraClick
                )

                // Sub-option B: Upload from Gallery
                ProfileOptionItem(
                    icon = Icons.Default.Image,
                    iconGradient = listOf(Color(0xFF059669), Color(0xFF10B981)),
                    title = "Upload from Gallery",
                    description = "Choose an existing image from photos",
                    isDark = isDark,
                    onClick = onUploadFromGalleryClick
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textMuted
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    iconGradient: List<Color>,
    title: String,
    description: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF131B38) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF1E294B) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(iconGradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    color = textMuted,
                    fontSize = 12.sp
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = textMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Full-size Profile Picture viewer dialog.
 */
@Composable
internal fun ViewProfilePictureDialog(
    user: User?,
    defaultDrawableRes: Int = R.drawable.img_student_hero,
    onDismiss: () -> Unit,
    onChangePhotoClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalQuizDark.current
    val dialogBg = if (isDark) Color(0xFF0A0F1D) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 20.dp, vertical = 24.dp),
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
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile Picture",
                        style = TextStyle(
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Large Picture Display
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF9333EA),
                                    Color(0xFF06B6D4),
                                    Color(0xFF6366F1)
                                )
                            )
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF131B38) else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.avatarUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(user?.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Full Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF6366F1),
                                        modifier = Modifier.size(36.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                            },
                            error = {
                                Image(
                                    painter = painterResource(id = defaultDrawableRes),
                                    contentDescription = "Default avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                    } else {
                        Image(
                            painter = painterResource(id = defaultDrawableRes),
                            contentDescription = "Default avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // User details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = user?.name ?: "Student",
                        style = TextStyle(
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    val subtitle = buildString {
                        if (!user?.displayDepartment.isNullOrBlank()) {
                            append(user?.displayDepartment)
                        }
                        if (!user?.displayUniversity.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append(user?.displayUniversity)
                        }
                    }

                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = TextStyle(
                                color = textMuted,
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textMuted)
                    ) {
                        Text(
                            text = "Close",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onChangePhotoClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Change Photo",
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
