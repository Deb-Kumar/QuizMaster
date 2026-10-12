package com.example.onlinequizapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.Option
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.ui.theme.BrandPrimary
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.DangerRedBg
import com.example.onlinequizapp.ui.theme.Slate200
import com.example.onlinequizapp.ui.theme.Slate700
import com.example.onlinequizapp.ui.theme.SuccessGreen
import com.example.onlinequizapp.ui.theme.SuccessGreenBg

enum class OptionVisualState {
    NORMAL,
    SELECTED,
    CORRECT,
    INCORRECT
}

@Composable
fun OptionItem(
    option: Option,
    questionType: QuestionType,
    isSelected: Boolean,
    visualState: OptionVisualState,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when (visualState) {
            OptionVisualState.CORRECT -> SuccessGreen
            OptionVisualState.INCORRECT -> DangerRed
            OptionVisualState.SELECTED -> BrandPrimary
            OptionVisualState.NORMAL -> MaterialTheme.colorScheme.outline
        },
        label = "optionBorderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (visualState) {
            OptionVisualState.CORRECT -> SuccessGreenBg
            OptionVisualState.INCORRECT -> DangerRedBg
            OptionVisualState.SELECTED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            OptionVisualState.NORMAL -> MaterialTheme.colorScheme.surface
        },
        label = "optionBgColor"
    )

    val role = if (questionType == QuestionType.SINGLE) Role.RadioButton else Role.Checkbox

    val stateDesc = when (visualState) {
        OptionVisualState.CORRECT -> "Correct answer"
        OptionVisualState.INCORRECT -> "Incorrect answer"
        OptionVisualState.SELECTED -> "Selected"
        OptionVisualState.NORMAL -> "Not selected"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                border = BorderStroke(
                    width = if (visualState != OptionVisualState.NORMAL) 2.dp else 1.dp,
                    color = borderColor
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .selectable(
                selected = isSelected,
                enabled = enabled,
                role = role,
                onClick = onSelect
            )
            .semantics {
                contentDescription = "Option ${option.id}, ${option.text}. $stateDesc"
            },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Indicator: Radio, Checkbox, Check, or Cross icon
            when (visualState) {
                OptionVisualState.CORRECT -> {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Correct",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                OptionVisualState.INCORRECT -> {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(DangerRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Incorrect",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                else -> {
                    if (questionType == QuestionType.SINGLE) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = BrandPrimary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrandPrimary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Option Letter Prefix e.g. "A."
            Text(
                text = "${option.id}.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = when (visualState) {
                    OptionVisualState.CORRECT -> SuccessGreen
                    OptionVisualState.INCORRECT -> DangerRed
                    OptionVisualState.SELECTED -> BrandPrimary
                    OptionVisualState.NORMAL -> MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Option text
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
