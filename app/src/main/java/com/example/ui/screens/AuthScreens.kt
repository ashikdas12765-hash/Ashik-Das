package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.components.FriendZoneLogoBadge
import com.example.ui.components.FriendZoneWordmark
import com.example.ui.components.GradientButton
import com.example.ui.components.LanguageToggleButton
import com.example.ui.components.LocalPreviewPill
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FzAmber
import com.example.ui.theme.FzPink
import com.example.ui.theme.FzPurple
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.AuthMode

@Composable
fun AuthFlowScreen(
    authMode: AuthMode,
    language: AppLanguage,
    onToggleLanguage: () -> Unit,
    isValidEmail: (String) -> Boolean,
    isValidUsername: (String) -> Boolean,
    onSwitchMode: (AuthMode) -> Unit,
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (fullName: String, username: String, email: String, password: String) -> Boolean,
    onDemoSignIn: () -> Unit
) {
    if (authMode == AuthMode.REGISTER) {
        BackHandler { onSwitchMode(AuthMode.LOGIN) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Soft atmospheric purple-pink halo at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            FzPurple.copy(alpha = 0.14f),
                            FzPink.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Language Toggle Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                horizontalArrangement = Arrangement.End
            ) {
                LanguageToggleButton(
                    currentLanguage = language,
                    onToggle = onToggleLanguage,
                    tag = "auth_language_toggle_button"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand Header
            FriendZoneLogoBadge(size = 72.dp)
            Spacer(modifier = Modifier.height(14.dp))
            FriendZoneWordmark(fontSize = 34.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LocalAppStrings.current.tagline,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LocalPreviewPill(
                subtitle = LocalAppStrings.current.localPreviewSubtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = authMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "auth_mode_switch"
            ) { mode ->
                when (mode) {
                    AuthMode.LOGIN -> LoginCardContent(
                        isValidEmail = isValidEmail,
                        onLogin = onLogin,
                        onDemoSignIn = onDemoSignIn,
                        onNavigateRegister = { onSwitchMode(AuthMode.REGISTER) }
                    )
                    AuthMode.REGISTER -> RegisterCardContent(
                        isValidEmail = isValidEmail,
                        isValidUsername = isValidUsername,
                        onRegister = onRegister,
                        onNavigateLogin = { onSwitchMode(AuthMode.LOGIN) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun LoginCardContent(
    isValidEmail: (String) -> Boolean,
    onLogin: (String, String) -> Boolean,
    onDemoSignIn: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    val strings = LocalAppStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = strings.welcomeBack,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.loginSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    generalError = null
                },
                label = { Text(strings.emailLabel) },
                placeholder = { Text(strings.emailPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = emailError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    generalError = null
                },
                label = { Text(strings.passwordLabel) },
                placeholder = { Text(strings.passwordPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showForgotDialog = true },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = strings.forgotPassword,
                        color = FzPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (generalError != null) {
                Text(
                    text = generalError!!,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            GradientButton(
                text = strings.loginButton,
                onClick = {
                    val validEmail = isValidEmail(email)
                    val validPass = password.length >= 6
                    emailError = if (!validEmail) strings.errInvalidEmail else null
                    passwordError = if (!validPass) strings.errShortPassword else null
                    if (validEmail && validPass) {
                        val ok = onLogin(email, password)
                        if (!ok) {
                            generalError = strings.errInvalidLogin
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_submit_button")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDemoSignIn,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("demo_login_button")
            ) {
                Text(
                    text = strings.continueDemoButton,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.noAccountYet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateRegister,
                    modifier = Modifier.testTag("go_to_register_button")
                ) {
                    Text(
                        text = strings.createNewAccount,
                        color = FzPink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showForgotDialog) {
        ForgotPasswordPreviewDialog(
            initialEmail = email,
            isValidEmail = isValidEmail,
            onDismiss = { showForgotDialog = false }
        )
    }
}

@Composable
private fun ForgotPasswordPreviewDialog(
    initialEmail: String,
    isValidEmail: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var email by remember { mutableStateOf(initialEmail) }
    var error by remember { mutableStateOf<String?>(null) }
    var checked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.forgotPasswordTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = strings.forgotPasswordDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        error = null
                    },
                    label = { Text(strings.emailLabel) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (checked) {
                    Text(
                        text = strings.resetPreviewNotice,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FzPurple
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isValidEmail(email)) {
                        error = strings.errInvalidEmail
                    } else {
                        checked = true
                    }
                }
            ) {
                Text(strings.sendResetPreview, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}

@Composable
private fun RegisterCardContent(
    isValidEmail: (String) -> Boolean,
    isValidUsername: (String) -> Boolean,
    onRegister: (fullName: String, username: String, email: String, password: String) -> Boolean,
    onNavigateLogin: () -> Unit
) {
    val strings = LocalAppStrings.current
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    val strengthProgress = remember(password) {
        when {
            password.isEmpty() -> 0f
            password.length < 6 -> 0.33f
            password.length < 10 || !password.any { it.isDigit() } -> 0.66f
            else -> 1f
        }
    }
    val strengthColor = when {
        strengthProgress <= 0.34f -> ErrorRed
        strengthProgress <= 0.67f -> FzAmber
        else -> SuccessGreen
    }
    val strengthLabel = when {
        strengthProgress <= 0.34f -> strings.passwordWeak
        strengthProgress <= 0.67f -> strings.passwordGood
        else -> strings.passwordStrong
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = strings.registerTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.registerSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; nameError = null },
                label = { Text(strings.fullNameLabel) },
                placeholder = { Text(strings.fullNamePlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_fullname_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().replace(" ", ""); usernameError = null },
                label = { Text(strings.usernameLabel) },
                placeholder = { Text(strings.usernamePlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = null) },
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_username_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = { Text(strings.emailLabel) },
                placeholder = { Text(strings.emailPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text(strings.passwordLabel) },
                placeholder = { Text(strings.passwordPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input")
            )

            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { strengthProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = strengthColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = strengthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = strengthColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmError = null },
                label = { Text(strings.confirmPasswordLabel) },
                placeholder = { Text(strings.confirmPasswordPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmError != null,
                supportingText = confirmError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_confirm_password_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            GradientButton(
                text = strings.createAccountButton,
                onClick = {
                    val okName = fullName.trim().length >= 2
                    val okUser = isValidUsername(username)
                    val okEmail = isValidEmail(email)
                    val okPass = password.length >= 6
                    val okMatch = confirmPassword == password && confirmPassword.isNotEmpty()

                    nameError = if (!okName) strings.errEmptyName else null
                    usernameError = if (!okUser) strings.errInvalidUsername else null
                    emailError = if (!okEmail) strings.errInvalidEmail else null
                    passwordError = if (!okPass) strings.errShortPassword else null
                    confirmError = if (!okMatch) strings.errPasswordMismatch else null

                    if (okName && okUser && okEmail && okPass && okMatch) {
                        onRegister(fullName, username, email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_submit_button")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.alreadyHaveAccount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateLogin,
                    modifier = Modifier.testTag("go_to_login_button")
                ) {
                    Text(
                        text = strings.signInLink,
                        color = FzPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
