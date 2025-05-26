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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.aisupabase.auth.AuthViewModel
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Users
import com.example.aisupabase.models.type_accounts
import io.github.jan.supabase.auth.auth

import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    LoginScreen(navController)
}
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreen(navController: NavController) {
    // Khai báo các biến state để lưu trữ giá trị nhập vào
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    //state để kiểm tra trạng thái đăng nhập
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val errorRed = Color(0xFFD32F2F)
    val context = LocalContext.current

    // Màu xanh dương cho nút đăng ký và link đăng nhập
    val primaryBlue = Color(0xFF4361EE)

    // Sử dụng Box để đặt hình nền và nội dung chồng lên nhau
    Box(modifier = Modifier.fillMaxSize()) {
        // Hình nền - sử dụng alpha để làm mờ hình nền nếu cần
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        // Bố cục chính - sử dụng BoxWithConstraints để tính toán vị trí chính xác
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Phần tiêu đề - nằm ở trên cùng với padding từ trên xuống
                Spacer(modifier = Modifier.height(screenHeight * 0.1f)) // 10% chiều cao màn hình

                // Tiêu đề "Đăng ký"
                Text(
                    text = "Đăng nhập",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Phụ đề "Tạo tài khoản để tiếp tục!"
                Text(
                    text = "Chào mừng đã quay lại!",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Spacer để đẩy các trường nhập liệu xuống giữa màn hình
                Spacer(modifier = Modifier.height(screenHeight * 0.1f)) // 10% chiều cao màn hình

                // Phần form nhập liệu - nằm ở giữa màn hình
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Trường nhập email
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
                        isError = emailError != null
                    )
                    if (emailError != null) {
                        Text(emailError!!, color = errorRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Trường nhập mật khẩu
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
                        isError = passwordError != null
                    )
                    if (passwordError != null) {
                        Text(passwordError!!, color = errorRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Nút đăng ký
                    Button(
                        onClick = {
                            var valid = true
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Email không hợp lệ"
                                valid = false
                            }
                            if (password.length < 8) {
                                passwordError = "Mật khẩu tối thiểu 8 ký tự"
                                valid = false
                            } else if (!password.any { it.isUpperCase() }) {
                                passwordError = "Mật khẩu phải có ít nhất 1 chữ cái viết hoa"
                                valid = false
                            }
                            if (valid) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    login(email, password, navController, context)
                                }
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue
                        )
                    ) {
                        Text(
                            text = "Đăng nhập",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Phần văn bản và liên kết đăng ky
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
                                modifier = Modifier.padding(start = 4.dp)
                                    .clickable {
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
    context: Context
) {
    val supabase = SupabaseClientProvider.client
    try {
        val result = supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        // Lấy thông tin người dùng từ Supabase
        val userList = supabase.postgrest["users"]
            .select {
                filter { eq("email", email) }
            }
            .decodeList<Users>()

        val user = userList.firstOrNull() ?: throw Exception("Không tìm thấy người dùng với email: $email")

        // lay thông tin loại tài khoản từ Supabase
        val typeList = supabase.postgrest["type_account"]
            .select {
                filter { eq("id", user.id_type_account) }
            }
            .decodeList<type_accounts>()

        val typeuser = typeList.firstOrNull() ?: throw Exception("Không tìm thấy loại tài khoản cho người dùng: ${user.username}")

        // Lưu thông tin người dùng vào SharedPreferences
        authUser().saveUserSession(context, user.username, user.email, user.id, user.index_image, user.role.toString(), typeuser.type)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            }

    } catch (e: Exception) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("Login", "Session: ${e.toString()}")
            Toast.makeText(context, "Lỗi đăng nhập, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
        }

    }
}