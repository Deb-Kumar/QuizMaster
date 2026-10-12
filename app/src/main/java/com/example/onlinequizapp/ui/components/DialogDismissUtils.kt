package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Modifier placed on the full-screen outer Box of a Dialog with usePlatformDefaultWidth = false
 * so that tapping anywhere outside the modal content card closes the dialog.
 */
@Composable
fun Modifier.dismissOnClickOutside(onDismiss: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onDismiss
    )
}

/**
 * Modifier placed on the inner modal Card/Column
 * so that clicks inside the modal card are consumed and do not dismiss the dialog.
 */
@Composable
fun Modifier.consumeClicks(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {}
    )
}
