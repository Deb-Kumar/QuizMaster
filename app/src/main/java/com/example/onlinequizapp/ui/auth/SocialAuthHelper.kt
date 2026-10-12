package com.example.onlinequizapp.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

object SocialAuthHelper {

    fun launchGoogleSignIn(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()

        // Check if default_web_client_id is available from google-services plugin
        val webClientIdResId = try {
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        } catch (_: Exception) {
            0
        }

        if (webClientIdResId != 0) {
            try {
                val webClientId = context.getString(webClientIdResId)
                if (webClientId.isNotBlank()) {
                    gsoBuilder.requestIdToken(webClientId)
                }
            } catch (_: Exception) {}
        }

        val gso = gsoBuilder.build()
        val client = GoogleSignIn.getClient(context, gso)

        // Sign out first so the Google Account Chooser is ALWAYS shown when tapped!
        client.signOut().addOnCompleteListener {
            launcher.launch(client.signInIntent)
        }
    }

    fun createAccountPickerIntent(): Intent {
        return android.accounts.AccountManager.newChooseAccountIntent(
            null,
            null,
            arrayOf("com.google"),
            null,
            null,
            null,
            null
        )
    }

    fun getGoogleErrorMessage(statusCode: Int, localizedMessage: String?): String {
        return when (statusCode) {
            10 -> "Google Sign-In Error (10): Debug SHA-1 fingerprint is missing in Firebase Console. Add SHA-1 to project settings."
            12500 -> "Google Sign-In configuration error (12500). Please verify SHA-1 in Firebase Console."
            7 -> "Network error. Please check your internet connection."
            else -> "Google Sign-In failed ($statusCode): ${localizedMessage ?: "Unknown error"}"
        }
    }

    fun isCertificateHashError(e: Exception): Boolean {
        val msg = e.localizedMessage ?: ""
        return msg.contains("certificate hash", ignoreCase = true) ||
               msg.contains("package certificate", ignoreCase = true) ||
               msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
               msg.contains("provider is not enabled", ignoreCase = true)
    }

    fun launchGithubSignIn(
        activity: Activity,
        onStart: () -> Unit,
        onSuccess: (name: String, email: String, photoUrl: String?) -> Unit,
        onFallbackPrompt: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val auth = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            onError("Firebase Auth is not available: ${e.localizedMessage}")
            onFallbackPrompt()
            return
        }

        val provider = OAuthProvider.newBuilder("github.com")
        provider.scopes = listOf("user:email", "read:user")

        onStart()

        val handleAuthSuccess = { authResult: com.google.firebase.auth.AuthResult ->
            val user = authResult.user
            val additionalInfo = authResult.additionalUserInfo
            val profileMap = additionalInfo?.profile
            val githubUsername = additionalInfo?.username
                ?: (profileMap?.get("login") as? String)
                ?: (profileMap?.get("login_name") as? String)
            val email = user?.email
                ?: (profileMap?.get("email") as? String)
                ?: (if (!githubUsername.isNullOrBlank()) "$githubUsername@github.com" else "${user?.uid ?: "user"}@github.com")
            val name = user?.displayName
                ?: (profileMap?.get("name") as? String)
                ?: githubUsername
                ?: email.substringBefore("@")
            val cleanHandle = (githubUsername ?: name.substringBefore("@")).replace(" ", "").trim()
            val photoUrl = (profileMap?.get("avatar_url") as? String)?.takeIf { it.isNotBlank() }
                ?: user?.photoUrl?.toString()?.takeIf { it.isNotBlank() }
                ?: user?.providerData?.firstOrNull { it.providerId == "github.com" }?.photoUrl?.toString()
                ?: if (cleanHandle.isNotBlank()) "https://github.com/$cleanHandle.png" else null
            onSuccess(name, email, photoUrl)
        }

        val pendingResult = auth.pendingAuthResult
        if (pendingResult != null) {
            pendingResult.addOnSuccessListener { authResult ->
                handleAuthSuccess(authResult)
            }.addOnFailureListener { e ->
                if (isCertificateHashError(e)) {
                    onFallbackPrompt()
                }
                onError(formatSocialAuthError("GitHub", e))
            }
        } else {
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { authResult ->
                    handleAuthSuccess(authResult)
                }
                .addOnFailureListener { e ->
                    if (isCertificateHashError(e)) {
                        onFallbackPrompt()
                    }
                    onError(formatSocialAuthError("GitHub", e))
                }
        }
    }

    fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    private fun formatSocialAuthError(providerName: String, e: Exception): String {
        val msg = e.localizedMessage ?: "Unknown error"
        return when {
            msg.contains("certificate hash", ignoreCase = true) ||
            msg.contains("package certificate", ignoreCase = true) -> {
                "$providerName sign-in error: SHA-1 / SHA-256 certificate hash is not registered in Firebase Console. Add SHA-1 to Firebase Project Settings."
            }
            msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
            msg.contains("provider is not enabled", ignoreCase = true) -> {
                "$providerName sign-in is not enabled in Firebase Console. Please enable $providerName in Firebase Console > Authentication > Sign-in method."
            }
            msg.contains("canceled", ignoreCase = true) ||
            msg.contains("cancelled", ignoreCase = true) ||
            msg.contains("WEB_CONTEXT_CANCELED", ignoreCase = true) -> {
                "$providerName sign-in was cancelled."
            }
            else -> "$providerName sign-in failed: $msg"
        }
    }
}
