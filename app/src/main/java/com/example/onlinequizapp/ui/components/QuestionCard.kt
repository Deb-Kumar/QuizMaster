package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.ui.theme.AccentAmber
import com.example.onlinequizapp.ui.theme.AccentAmberBg
import com.example.onlinequizapp.ui.theme.BrandPrimary
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.DangerRedBg
import com.example.onlinequizapp.ui.theme.SuccessGreen
import com.example.onlinequizapp.ui.theme.SuccessGreenBg

@Composable
fun QuestionCardHeader(
    difficulty: Difficulty,
    questionType: QuestionType,
    prompt: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Difficulty Badge
            val (diffBg, diffText, diffLabel) = when (difficulty) {
                Difficulty.EASY -> Triple(SuccessGreenBg, SuccessGreen, "Easy")
                Difficulty.MEDIUM -> Triple(AccentAmberBg, AccentAmber, "Medium")
                Difficulty.HARD -> Triple(DangerRedBg, DangerRed, "Hard")
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(diffBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = diffLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = diffText
                )
            }

            // Question Type Badge
            val typeLabel = when (questionType) {
                QuestionType.SINGLE -> "Single Choice"
                QuestionType.MULTIPLE -> "Multiple Choice"
                QuestionType.MIXED -> "Mixed Choice"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BrandPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = prompt,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                lineHeight = 27.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
