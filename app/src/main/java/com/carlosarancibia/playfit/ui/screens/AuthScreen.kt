package com.carlosarancibia.playfit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import com.carlosarancibia.playfit.data.auth.AuthResult
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import kotlinx.coroutines.launch

enum class AuthView { Options, SignIn, SignUp }

@Composable
fun AuthScreen(
    onDismiss: () -> Unit,
    onGoogleSignIn: suspend () -> AuthResult,
    onEmailSignIn: suspend (email: String, password: String) -> AuthResult,
    onEmailSignUp: suspend (email: String, password: String) -> AuthResult,
    onGuestSignIn: suspend () -> AuthResult,
    onResetPassword: suspend (email: String) -> AuthResult = { AuthResult.Error("Not available") },
) {
    var view by rememberSaveable { mutableStateOf(AuthView.Options) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    suspend fun submitEmail() {
        focusManager.clearFocus()
        if (busy) return
        error = null
        success = null
        busy = true
        val result = if (view == AuthView.SignUp) {
            onEmailSignUp(email.trim(), password)
        } else {
            onEmailSignIn(email.trim(), password)
        }
        when (result) {
            is AuthResult.Success -> onDismiss()
            is AuthResult.Pending -> success = result.message
            is AuthResult.Error -> error = result.message
        }
        busy = false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        GlowBackground()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(PlayfitSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(PlayfitSpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (view != AuthView.Options) {
                    IconButton(
                        onClick = {
                            view = AuthView.Options
                            error = null
                            success = null
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(Modifier.size(32.dp))
                }
                Image(
                    painter = painterResource(com.carlosarancibia.playfit.R.drawable.playfit_logo),
                    contentDescription = "Playfit",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp),
                ) {
                    Text(
                        text = "X",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(PlayfitSpacing.lg))

            Image(
                painter = painterResource(com.carlosarancibia.playfit.R.drawable.playfit_logo),
                contentDescription = "Playfit",
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit,
            )

            Text(
                text = "PLAYFIT DECISIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = PlayfitExtendedTheme.colors.playfitAccent,
            )

            Spacer(Modifier.height(PlayfitSpacing.sm))

            val (heading, subtitle) = when (view) {
                AuthView.SignIn -> "Sign In" to "Enter your email credentials to access your library."
                AuthView.SignUp -> "Create Account" to "Create an account to backup recommendations in the cloud."
                AuthView.Options -> "Welcome to Playfit" to "Choose how you want to sync your library across devices."
            }
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = PlayfitSpacing.xs),
            )

            Spacer(Modifier.height(PlayfitSpacing.lg))

            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                error?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PlayfitSpacing.sm),
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = PlayfitExtendedTheme.colors.playfitNegative,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PlayfitSpacing.sm),
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = success != null,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                success?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = PlayfitSpacing.sm),
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = PlayfitExtendedTheme.colors.playfitPositive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PlayfitSpacing.sm),
                        )
                    }
                }
            }

            when (view) {
                AuthView.Options -> {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                focusManager.clearFocus()
                                if (busy) return@launch
                                error = null
                                success = null
                                busy = true
                                val result = onGoogleSignIn()
                                when (result) {
                                    is AuthResult.Success -> onDismiss()
                                    is AuthResult.Pending -> success = result.message
                                    is AuthResult.Error -> error = result.message
                                }
                                busy = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        enabled = !busy,
                    ) {
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                        )
                    }

                    Spacer(Modifier.height(PlayfitSpacing.sm))

                    OutlinedButton(
                        onClick = {
                            view = AuthView.SignIn
                            error = null
                            success = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        enabled = !busy,
                    ) {
                        Text(
                            text = "Continue with Email",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                        )
                    }

                    Spacer(Modifier.height(PlayfitSpacing.sm))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                focusManager.clearFocus()
                                if (busy) return@launch
                                error = null
                                success = null
                                busy = true
                                val result = onGuestSignIn()
                                when (result) {
                                    is AuthResult.Success -> onDismiss()
                                    is AuthResult.Pending -> success = result.message
                                    is AuthResult.Error -> error = result.message
                                }
                                busy = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                        ),
                        enabled = !busy,
                    ) {
                        Text(
                            text = "Continue as Guest",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                        )
                    }

                    Spacer(Modifier.height(PlayfitSpacing.sm))

                    Text(
                        text = "New to Playfit? Create account",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = PlayfitSpacing.xs),
                    )

                    Spacer(Modifier.height(PlayfitSpacing.md))

                    Text(
                        text = "Guest profiles save choices on this device. Creating an account lets you back up and sync recommendations.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = PlayfitSpacing.sm),
                    )
                }

                AuthView.SignIn, AuthView.SignUp -> {
                    Column(verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; error = null },
                            label = { Text("Email Address") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(),
                            enabled = !busy,
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; error = null },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = {
                                    scope.launch { submitEmail() }
                                },
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(),
                            enabled = !busy,
                        )

                        Spacer(Modifier.height(PlayfitSpacing.xs))

                        Button(
                            onClick = { scope.launch { submitEmail() } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PlayfitExtendedTheme.colors.playfitAccent,
                            ),
                            enabled = !busy,
                        ) {
                            Text(
                                text = if (view == AuthView.SignUp) {
                                    if (busy) "Creating account\u2026" else "Create Account"
                                } else {
                                    if (busy) "Signing in\u2026" else "Sign In"
                                },
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }

                        if (view == AuthView.SignIn) {
                            TextButton(
                                onClick = {
                                    if (email.isBlank()) {
                                        error = "Enter your email address first."
                                        return@TextButton
                                    }
                                    scope.launch {
                                        val result = onResetPassword(email)
                                        when (result) {
                                            is AuthResult.Success -> {
                                                success = "If that email is registered, you'll receive a reset link shortly."
                                            }
                                            is AuthResult.Pending -> success = result.message
                                            is AuthResult.Error -> error = result.message
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                view = if (view == AuthView.SignIn) AuthView.SignUp else AuthView.SignIn
                                error = null
                                success = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = if (view == AuthView.SignIn) "No account yet? Create one" else "Already have an account? Sign in",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(PlayfitSpacing.xxl))
        }
    }
}
