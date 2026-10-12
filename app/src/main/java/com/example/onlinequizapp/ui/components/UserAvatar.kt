package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.onlinequizapp.ui.theme.BrandPrimary
import com.example.onlinequizapp.ui.theme.BrandPrimaryDark
import com.example.onlinequizapp.ui.theme.BrandPrimaryLight

@Composable
fun UserAvatar(
    avatarUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    borderWidth: Dp = 1.5.dp,
    borderColor: Color = BrandPrimary.copy(alpha = 0.5f),
    @DrawableRes defaultDrawableRes: Int? = null
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile picture of $name",
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
                            strokeWidth = 2.dp,
                            color = BrandPrimary,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                },
                error = {
                    if (defaultDrawableRes != null) {
                        Image(
                            painter = painterResource(id = defaultDrawableRes),
                            contentDescription = "Profile picture of $name",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        FallbackInitials(initial = initial, size = size)
                    }
                }
            )
        } else if (defaultDrawableRes != null) {
            Image(
                painter = painterResource(id = defaultDrawableRes),
                contentDescription = "Profile picture of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            FallbackInitials(initial = initial, size = size)
        }
    }
}

@Composable
private fun FallbackInitials(
    initial: String,
    size: Dp
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            BrandPrimaryDark,
            BrandPrimaryLight
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp
        )
    }
}
