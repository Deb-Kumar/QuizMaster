package com.example.onlinequizapp.ui.components

import android.content.Context
import android.widget.Toast

/**
 * Global helper to display Toast messages throughout the application.
 */
fun Context.showToast(message: String, isLong: Boolean = false) {
    if (message.isNotBlank()) {
        Toast.makeText(this, message, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }
}
