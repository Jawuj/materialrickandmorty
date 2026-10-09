package com.example.materialrickandmorty.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialrickandmorty.R
import com.example.materialrickandmorty.ui.theme.CardContainer
import com.example.materialrickandmorty.ui.theme.CardText
import com.example.materialrickandmorty.ui.theme.ChipBackground
import com.example.materialrickandmorty.ui.theme.DarkBackground
import com.example.materialrickandmorty.ui.theme.HeaderTitleText
import com.example.materialrickandmorty.ui.theme.IconPurple

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onSignUpClick: (String, String, String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    isFullBrandScreen: Boolean = true,
    onGuestClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = if (isFullBrandScreen) 16.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFullBrandScreen) {
                Spacer(modifier = Modifier.height(12.dp))

                // Brand Title "MATERIAL RICK"
                Text(
                    text = "MATERIAL\nRICK",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = HeaderTitleText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rick & Morty Image
                Image(
                    painter = painterResource(id = R.drawable.rickandmorty),
                    contentDescription = null,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (isFullBrandScreen) {
                        Text(
                            text = if (isSignUpMode) "Create account" else "Welcome back",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = IconPurple
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isSignUpMode) "Join the universe today." else "Your universe, all in one place.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CardText
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Continue with Google Button
                    Button(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1D192B)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFF1D192B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Or Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = CardText.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "Or",
                            style = MaterialTheme.typography.bodySmall,
                            color = CardText,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = CardText.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name Field (sign up mode)
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Name", color = IconPurple) },
                            placeholder = { Text("Enter your name", color = CardText) },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = IconPurple)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ChipBackground,
                                unfocusedContainerColor = ChipBackground,
                                focusedBorderColor = IconPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = IconPurple,
                                unfocusedTextColor = IconPurple
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email", color = IconPurple) },
                        placeholder = { Text("Enter your email", color = CardText) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = IconPurple)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChipBackground,
                            unfocusedContainerColor = ChipBackground,
                            focusedBorderColor = IconPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = IconPurple,
                            unfocusedTextColor = IconPurple
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password", color = IconPurple) },
                        placeholder = { Text("Enter your password", color = CardText) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = IconPurple)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = IconPurple
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChipBackground,
                            unfocusedContainerColor = ChipBackground,
                            focusedBorderColor = IconPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = IconPurple,
                            unfocusedTextColor = IconPurple
                        ),
                        singleLine = true
                    )

                    if (!isSignUpMode && isFullBrandScreen) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Forgot password?",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = IconPurple,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = IconPurple)
                        }
                    } else {
                        // Main Action Button
                        Button(
                            onClick = {
                                if (isSignUpMode) {
                                    onSignUpClick(email.trim(), password.trim(), name.trim())
                                } else {
                                    onLoginClick(email.trim(), password.trim())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.5.dp, IconPurple, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardContainer,
                                contentColor = IconPurple
                            )
                        ) {
                            Text(
                                text = if (isSignUpMode) "Sign up" else "Log in",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Mode Button
                        Button(
                            onClick = { isSignUpMode = !isSignUpMode },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.5.dp, IconPurple, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardContainer,
                                contentColor = IconPurple
                            )
                        ) {
                            Text(
                                text = if (isSignUpMode) "Already have an account? Log in" else "Create an account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (onGuestClick != null && isFullBrandScreen) {
                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onGuestClick) {
                    Text(
                        text = "Continue as guest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = HeaderTitleText
                    )
                }
            }

            if (isFullBrandScreen) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "By continuing, you agree to our\nTerms of service and Privacy policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
