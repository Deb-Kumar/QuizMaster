package com.example.onlinequizapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.ui.components.*
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.DangerRedBg
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showGithubDialog by remember { mutableStateOf(false) }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val chosenEmail = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
        if (!chosenEmail.isNullOrBlank()) {
            val name = chosenEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
            val photoUrl = if (lastAccount?.email.equals(chosenEmail, ignoreCase = true)) {
                lastAccount?.photoUrl?.toString()
            } else {
                null
            }
            viewModel.handleGoogleSignInResult(
                name = name,
                email = chosenEmail,
                idToken = null,
                avatarUrl = photoUrl,
                onSuccess = { user ->
                    context.showToast("Signed in as ${user.name}")
                    onLoginSuccess(user)
                }
            )
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val email = account.email ?: "google.user@gmail.com"
                val name = account.displayName ?: email.substringBefore("@")
                val idToken = account.idToken
                val photoUrl = account.photoUrl?.toString()
                viewModel.handleGoogleSignInResult(
                    name = name,
                    email = email,
                    idToken = idToken,
                    avatarUrl = photoUrl,
                    onSuccess = { user ->
                        context.showToast("Signed in as ${user.name}")
                        onLoginSuccess(user)
                    }
                )
            }
        } catch (e: ApiException) {
            if (e.statusCode != 12501) { // 12501 is SIGN_IN_CANCELLED
                if (e.statusCode == 10) {
                    // DEVELOPER_ERROR (missing SHA-1 in Firebase Console)
                    // Prompt the Android OS Google Account Picker so user can choose their Gmail immediately!
                    try {
                        accountPickerLauncher.launch(SocialAuthHelper.createAccountPickerIntent())
                    } catch (_: Exception) {
                        viewModel.onAuthError(SocialAuthHelper.getGoogleErrorMessage(e.statusCode, e.localizedMessage))
                    }
                } else {
                    viewModel.onAuthError(SocialAuthHelper.getGoogleErrorMessage(e.statusCode, e.localizedMessage))
                }
            }
        }
    }

    // Show toast for any login or authentication errors
    androidx.compose.runtime.LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            context.showToast(msg)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuthBg)
    ) {
        // Futuristic Glowing Canvas Background
        AuthBackgroundCanvas()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val minHeight = maxHeight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing Floating Badge
                    AuthHeaderBadge(
                        isTeacher = uiState.role == UserRole.TEACHER,
                        isRegister = false
                    )

                    Spacer(modifier = Modifier.height(14.dp))

            // "Welcome Back" Multi-Colored Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = AuthAccentPurple
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = if (uiState.role == UserRole.TEACHER) {
                    "Sign in with Teacher credentials to manage quizzes & students"
                } else {
                    "Sign in to practice quizzes and track your progress"
                },
                fontSize = 14.sp,
                color = AuthTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Role Selector Tab (Student / Teacher)
            AuthRoleSelector(
                selectedRole = uiState.role,
                onRoleSelected = { viewModel.onRoleSelected(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error message if any
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerRedBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DangerRed,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center
                        )
                        if (uiState.errorMessage?.contains("GitHub", ignoreCase = true) == true ||
                            uiState.errorMessage?.contains("certificate", ignoreCase = true) == true ||
                            uiState.errorMessage?.contains("hash", ignoreCase = true) == true
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showGithubDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E284D)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                GithubIcon(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue with Direct GitHub Sign-In",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Field
            AuthTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            AuthTextField(
                value = uiState.password,
                onValueChange = { if (it.length <= 16) viewModel.onPasswordChanged(it) },
                maxLength = 16,
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.isPasswordVisible) "Hide password" else "Show password",
                            tint = AuthTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login(onLoginSuccess)
                    }
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Main Action Button ("Sign In as Student →")
            AuthPrimaryButton(
                text = if (uiState.role == UserRole.TEACHER) "Sign In as Teacher" else "Sign In as Student",
                onClick = {
                    focusManager.clearFocus()
                    if (uiState.email.isBlank() || uiState.password.isBlank()) {
                        context.showToast("Please enter both email and password")
                    }
                    viewModel.login { user ->
                        context.showToast("Welcome back, ${user.name}!")
                        onLoginSuccess(user)
                    }
                },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Social Logins (Google & GitHub)
            SocialAuthSection(
                onGoogleClick = {
                    focusManager.clearFocus()
                    SocialAuthHelper.launchGoogleSignIn(context, googleSignInLauncher)
                },
                onGithubClick = {
                    focusManager.clearFocus()
                    val activity = SocialAuthHelper.findActivity(context)
                    if (activity != null) {
                        SocialAuthHelper.launchGithubSignIn(
                            activity = activity,
                            onStart = { viewModel.setLoading(true) },
                            onSuccess = { name, email, photoUrl ->
                                viewModel.handleGithubSignInResult(name, email, photoUrl) { user ->
                                    context.showToast("Signed in with GitHub as ${user.name}")
                                    onLoginSuccess(user)
                                }
                            },
                            onFallbackPrompt = {
                                showGithubDialog = true
                            },
                            onError = { errorMsg ->
                                viewModel.onAuthError(errorMsg)
                            }
                        )
                    } else {
                        showGithubDialog = true
                    }
                },
                isRegister = false
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Register Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = onNavigateToRegister
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = AuthTextMuted
                )
                Text(
                    text = "Register",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthAccentPurple
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

        GithubSignInDialog(
            isOpen = showGithubDialog,
            onDismiss = { showGithubDialog = false },
            onConfirm = { name, email, photoUrl ->
                viewModel.handleGithubSignInResult(name, email, photoUrl) { user ->
                    context.showToast("Signed in with GitHub as ${user.name}")
                    onLoginSuccess(user)
                }
            },
            role = uiState.role
        )
    }
}
