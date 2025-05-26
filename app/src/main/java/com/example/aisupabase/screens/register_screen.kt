package com.example.aisupabase.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.aisupabase.models.UserRole
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.aisupabase.models.Users
// Main activity
@Composable
fun RegisterScreen(navController: NavController) {

        RegistrationScreen(navController)

}

// view and function register

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RegistrationScreen(navController: NavController) {
    // Khai báo các biến state để lưu trữ giá trị nhập vào
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // state cho lỗi
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
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
                    text = "Đăng ký",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Phụ đề "Tạo tài khoản để tiếp tục!"
                Text(
                    text = "Tạo tài khoản để tiếp tục!",
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
                    // Trường nhập họ và tên
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                        },
                        placeholder = { Text("Họ và tên") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = if (fullNameError != null) errorRed else Color.LightGray,
                            focusedBorderColor = if (fullNameError != null) errorRed else primaryBlue
                        ),
                        singleLine = true,
                        isError = fullNameError != null
                    )
                    if (fullNameError != null) {
                        Text(fullNameError!!, color = errorRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

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

                    // Trường nhập số điện thoại
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneError = null
                        },
                        placeholder = { Text("Số điện thoại") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = if (phoneError != null) errorRed else Color.LightGray,
                            focusedBorderColor = if (phoneError != null) errorRed else primaryBlue
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = phoneError != null
                    )
                    if (phoneError != null) {
                        Text(phoneError!!, color = errorRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
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
                            // Validate
                            var valid = true
                            if (fullName.length < 8) {
                                fullNameError = "Họ và tên tối thiểu 8 ký tự"
                                valid = false
                            }else if (!fullName.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$"))) {
                                fullNameError = "Họ và tên không chứa số hoặc ký tự đặc biệt"
                                valid = false
                            }

                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Email không hợp lệ"
                                valid = false
                            }
                            if (!phoneNumber.matches(Regex("^\\d{10,11}$"))) {
                                phoneError = "Số điện thoại phải là 10-11 số"
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
                                Register(context,email, fullName, password, phoneNumber)
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
                            text = "Đăng ký",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // chuyển hướng đăng nhập
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = "Đã có tài khoản? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Đăng nhập",
                        color = primaryBlue,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                            .clickable {
                                navController.navigate("login")
                            }
                    )

                }
            }
        }
    }
}

fun Register(
    context: Context,
    email: String,
    fullName: String,
    password: String,
    phoneNumber: String
): String {
    val supabase = SupabaseClientProvider.client
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            if (result != null) {
                val newUser = Users(
                    username = fullName,
                    email = email,
                    phone = phoneNumber,
                    index_image = 1,
                    role = UserRole.client,
                    id_type_account = 1,
                )
                supabase.postgrest["users"].insert(newUser)
                // Đăng ký thành công, có thể show toast nếu muốn
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Email đã tồn tại hoặc không hợp lệ!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Email đã tồn tại hoặc không hợp lệ!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    return ("Provide the return value")
}

