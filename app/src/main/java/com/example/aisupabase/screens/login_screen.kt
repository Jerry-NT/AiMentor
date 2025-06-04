package com.example.aisupabase.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Users
import com.example.aisupabase.models.type_accounts
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username.toString() != "null" && role != null) {
            navController.navigate("${role}_home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LoginScreenContent(navController)
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreenContent(navController: NavController) {
    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Colors
    val errorRed = Color(0xFFD32F2F)
    val primaryBlue = Color(0xFF4361EE)

    // Main layout
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // Main content
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Header section
                Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                Text(
                    text = "Đăng nhập",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Chào mừng đã quay lại!",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                // Form section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        placeholder = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = if (emailError != null) errorRed else Color.LightGray,
                            focusedBorderColor = if (emailError != null) errorRed else primaryBlue
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        isError = emailError != null,
                        enabled = !isLoading
                    )

                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = errorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        placeholder = { Text("Mật khẩu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = if (passwordError != null) errorRed else Color.LightGray,
                            focusedBorderColor = if (passwordError != null) errorRed else primaryBlue
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = passwordError != null,
                        enabled = !isLoading
                    )

                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = errorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Login button
                    Button(
                        onClick = {
                            if (!isLoading) {
                                var valid = true

                                // Validate email
                                if (email.isBlank()) {
                                    emailError = "Email không được để trống"
                                    valid = false
                                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    emailError = "Email không hợp lệ"
                                    valid = false
                                }

                                // Validate password
                                if (password.isBlank()) {
                                    passwordError = "Mật khẩu không được để trống"
                                    valid = false
                                } else if (password.length < 8) {
                                    passwordError = "Mật khẩu tối thiểu 8 ký tự"
                                    valid = false
                                } else if (!password.any { it.isUpperCase() }) {
                                    passwordError = "Mật khẩu phải có ít nhất 1 chữ cái viết hoa"
                                    valid = false
                                }

                                if (valid) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        login(
                                            email = email,
                                            password = password,
                                            navController = navController,
                                            context = context,
                                            onLoadingChange = { isLoading = it }
                                        )
                                    }
                                }
                            }
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoading) Color.Gray else primaryBlue
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.height(20.dp)
                            )
                        } else {
                            Text(
                                text = "Đăng nhập",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Register link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chưa có tài khoản? ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Đăng ký",
                            color = primaryBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable(enabled = !isLoading) {
                                    navController.navigate("register")
                                }
                        )
                    }
                }
            }
        }
    }
}

suspend fun login(
    email: String,
    password: String,
    navController: NavController,
    context: Context,
    onLoadingChange: (Boolean) -> Unit
) {
    val supabase = SupabaseClientProvider.client

    try {
        // Authenticate user
        val result = supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        Log.d("Login", "Authentication successful: ${result.toString()}")

        // Get user information
        val userList = supabase.postgrest["users"]
            .select {
                filter { eq("email", email) }
            }
            .decodeList<Users>()

        val user = userList.firstOrNull()
            ?: throw Exception("Không tìm thấy người dùng với email: $email")

        Log.d("Login", "User found: ${user.username}")

        // Get account type information
        val typeList = supabase.postgrest["type_accounts"]
            .select {
                filter { eq("id", user.id_type_account) }
            }
            .decodeList<type_accounts>()

        val typeUser = typeList.firstOrNull()
            ?: throw Exception("Không tìm thấy loại tài khoản cho người dùng: ${user.username}")

        Log.d("Login", "Account type found: ${typeUser.type}")

        // Save user session
        authUser().saveUserSession(
            context = context,
            username = user.username,
            email = user.email,
            id = user.id,
            indexImage = user.index_image,
            role = user.role.toString(),
            typeAccount = typeUser.type
        )

        Log.d("Login", "User session saved successfully")

        // Navigate to main screen on Main thread
        withContext(Dispatchers.Main) {
            onLoadingChange(false)
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

            // Clear back stack and navigate to admin_home
            navController.navigate("${user.role}_home") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }

    } catch (e: Exception) {
        Log.e("Login", "Login error: ${e.message}", e)

        withContext(Dispatchers.Main) {
            onLoadingChange(false)
            val errorMessage = when {
                e.message?.contains("Invalid login credentials") == true ->
                    "Email hoặc mật khẩu không chính xác"
                e.message?.contains("network") == true ->
                    "Lỗi kết nối mạng, vui lòng kiểm tra internet"
                e.message?.contains("timeout") == true ->
                    "Kết nối bị timeout, vui lòng thử lại"
                else -> "Lỗi đăng nhập: ${e.message}"
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
}