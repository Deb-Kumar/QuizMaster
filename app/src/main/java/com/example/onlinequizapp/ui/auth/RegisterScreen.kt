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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    onRegisterSuccess: (User) -> Unit,
    onNavigateBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
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
                    context.showToast("Account created with Google as ${user.name}!")
                    onRegisterSuccess(user)
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
                        context.showToast("Account created with Google as ${user.name}!")
                        onRegisterSuccess(user)
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

    // Show toast for any registration errors
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
            val totalHeight = maxHeight
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Navigation Bar ("<- Create Account") firmly pinned to the TOP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBackToLogin,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Scrollable Body Content centered in the remaining viewport height
                val topBarHeight = 48.dp
                val contentMinHeight = (totalHeight - topBarHeight).coerceAtLeast(0.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = contentMinHeight)
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Centered Glowing Header Badge with 3D Books and Pencil
                        AuthHeaderBadge(
                            isTeacher = uiState.role == UserRole.TEACHER,
                            isRegister = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                // "Join QuizMaster" Multi-Color Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Join ",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Quiz",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = AuthAccentPurple
                    )
                    Text(
                        text = "Master",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = AuthAccentCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "Choose your role to get started with tailored features",
                    fontSize = 14.sp,
                    color = AuthTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Role Selector Tab (Student / Teacher)
                AuthRoleSelector(
                    selectedRole = uiState.role,
                    onRoleSelected = { viewModel.onRoleSelected(it) },
                    height = 46.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Error banner if any
                if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRedBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
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
                                Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Input Fields
                AuthTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    placeholder = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    fieldHeight = 52.dp,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    placeholder = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    fieldHeight = 52.dp,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(
                    value = uiState.password,
                    onValueChange = { if (it.length <= 16) viewModel.onPasswordChanged(it) },
                    maxLength = 16,
                    placeholder = "Password (at least 6 chars)",
                    leadingIcon = Icons.Default.Lock,
                    fieldHeight = 52.dp,
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
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                AuthTextField(
                    value = uiState.confirmPassword,
                    onValueChange = { if (it.length <= 16) viewModel.onConfirmPasswordChanged(it) },
                    maxLength = 16,
                    placeholder = "Confirm Password",
                    leadingIcon = Icons.Default.Lock,
                    fieldHeight = 52.dp,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                            Icon(
                                imageVector = if (uiState.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (uiState.isConfirmPasswordVisible) "Hide confirm password" else "Show confirm password",
                                tint = AuthTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.register(onRegisterSuccess)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button ("Create Student Account →")
                AuthPrimaryButton(
                    text = if (uiState.role == UserRole.TEACHER) "Create Teacher Account" else "Create Student Account",
                    onClick = {
                        focusManager.clearFocus()
                        if (uiState.name.isBlank()) {
                            context.showToast("Please enter your name")
                        } else if (uiState.email.isBlank()) {
                            context.showToast("Please enter your email")
                        } else if (uiState.password.length < 6) {
                            context.showToast("Password must be at least 6 characters")
                        } else if (uiState.password != uiState.confirmPassword) {
                            context.showToast("Passwords do not match")
                        }
                        viewModel.register { user ->
                            context.showToast("Account created successfully! Welcome, ${user.name}!")
                            onRegisterSuccess(user)
                        }
                    },
                    isLoading = uiState.isLoading,
                    buttonHeight = 52.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Social Sign Up (Google & GitHub)
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
                                        context.showToast("Account created with GitHub as ${user.name}!")
                                        onRegisterSuccess(user)
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
                    isRegister = true,
                    buttonHeight = 50.dp,
                    spacing = 10.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Already have an account? Login
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            role = Role.Button,
                            onClick = onNavigateBackToLogin
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 14.sp,
                        color = AuthTextMuted
                    )
                    Text(
                        text = "Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthAccentPurple
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        GithubSignInDialog(
            isOpen = showGithubDialog,
            onDismiss = { showGithubDialog = false },
            onConfirm = { name, email, photoUrl ->
                viewModel.handleGithubSignInResult(name, email, photoUrl) { user ->
                    context.showToast("Account created with GitHub as ${user.name}!")
                    onRegisterSuccess(user)
                }
            },
            role = uiState.role
        )
    }
}
