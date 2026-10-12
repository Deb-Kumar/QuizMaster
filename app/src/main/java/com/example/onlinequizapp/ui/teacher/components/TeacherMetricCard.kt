package com.example.onlinequizapp.ui.teacher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite

// -------------------------------------------------------------
// Component: Metric Card
// -------------------------------------------------------------
@Composable
internal fun TeacherMetricCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    value: String,
    label: String,
    trend: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                color = TeacherTextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )

            Text(
                text = label,
                color = TeacherTextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (!trend.isNullOrBlank()) {
                Text(
                    text = trend,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}
