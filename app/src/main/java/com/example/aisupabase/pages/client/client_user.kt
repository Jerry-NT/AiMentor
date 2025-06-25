package com.example.aisupabase.pages.client

import UserRepository
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.components.bottombar.BottomNavigationBar
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.UserRole
import com.example.aisupabase.models.Users
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.errorRed
import com.example.aisupabase.ui.theme.primaryBlue
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.get

// viewmodel
class ClientUserViewModel(private val userRepository: UserRepository):ViewModel(){

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ham update thong tin
    fun updateUser(user:Users)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = userRepository.updateUser(user)) {
                is UserResult.Success -> "Thành công"
                is UserResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun updatePassword( password: String)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = userRepository.changePassword(password)) {
                is UserResult.Success -> "Thành công"
                is UserResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
}

// viewmodel factory
class userViewModelFactory(private val supabase: SupabaseClient): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientUserViewModel::class.java)) {
            return ClientUserViewModel(UserRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SettingsItem(
    val title: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit = {}
)

@Composable
fun Client_User(navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "client") {
            authUser().clearUserSession(context)
            navController.navigate("login");
        }
    }

    val supabase = SupabaseClientProvider.client
    UserHomeView(navController,supabase)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: ClientUserViewModel = viewModel(factory = userViewModelFactory(supabase)))
    {
        val context = LocalContext.current
        val session = authUser().getUserSession(context)
        val indexImage = session["index_image"] as? Int
        val imageName = "avatar_" + (indexImage?.toString() ?: "")
        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        val imageResId = if (resId != 0) resId else R.drawable.background
        var showUpdateDialog by remember { mutableStateOf(false) }
        var changePassword by remember { mutableStateOf(false) }
        val settingsItems = listOf(
            SettingsItem(
                title = "Tùy chỉnh thông tin cá nhân",
                icon = Icons.Default.Edit,
                onClick = { showUpdateDialog = true }
            ),
            SettingsItem(
                title = "Tạo nhanh tài liệu",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("client_question") }
            ),
            SettingsItem(
                title = "Nâng cấp tai khoản",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("client_update_account") }
            ),
            SettingsItem(
                title = "Đặt lịch nhắc hẹn",
                icon = Icons.Default.Notifications,
                onClick = {

                    navController.navigate("client_noti")
                }
            ),
            SettingsItem(
                title = "Đổi mật khẩu",
                icon = Icons.Default.Edit,
                isDestructive = true,
                onClick = {changePassword = true }
            ),
            SettingsItem(
                title = "Đăng xuất",
                icon = Icons.Default.Close,
                isDestructive = true,
                onClick = {
                    authUser().clearUserSession(context)
                    navController.navigate("login") {
                        popUpTo("client_home") { inclusive = true }
                    }
                }
            )
        )
        val routeToIndex = mapOf(
            "client_home" to 0,
            "client_course" to 1,
            "client_search" to 2,
            "client_blog" to 3,
            "client_profile" to 4
        )
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        var selectedIndex by remember { mutableStateOf(routeToIndex[currentRoute] ?: 0) }

        LaunchedEffect(currentRoute) {
            selectedIndex = routeToIndex[currentRoute] ?: 0
        }

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index -> selectedIndex = index },
                    navController
                )
            }
        ){paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AsyncImage(
                    model = R.drawable.bg_5,
                    contentDescription = "Ảnh",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                )
                {
                    // thong tin user
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ){
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                // avatar
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                                {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                // User Name
                                Text(
                                    text = "${session["username"]}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                             //    Phone Number
                                Text(
                                    text = "(+84) ${session["phone"]}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Email
                                Text(
                                    text = "${session["email"]}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF2196F3),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = "${session["type_account"]}",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    // list action
                    items(settingsItems.size) { index ->
                        val item = settingsItems[index]
                        SettingsItemCard(
                            item = item,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        // ham cap nhap thong tin ca nhan
        if(showUpdateDialog)
        {
            val session = authUser().getUserSession(context)
            var fullName by remember { mutableStateOf(session["username"] as String) }
            var email by remember { mutableStateOf(session["email"] as String) }
            var phoneNumber by remember { mutableStateOf(session["phone"] as String) }

            // state cho lỗi
            var fullNameError by remember { mutableStateOf<String?>(null) }

            var phoneError by remember { mutableStateOf<String?>(null) }
            val errorRed = Color(0xFFD32F2F)
            val primaryBlue = Color(0xFF4361EE)

            Dialog(onDismissRequest = { showUpdateDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cập nhập Tài khoản", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showUpdateDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // truong fullname
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    var valid = true
                                    if (fullName.length < 8) {
                                        fullNameError = "Họ và tên tối thiểu 8 ký tự"
                                        valid = false
                                    }else if (!fullName.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$"))) {
                                        fullNameError = "Họ và tên không chứa số hoặc ký tự đặc biệt"
                                        valid = false
                                    }

                                    if (!phoneNumber.matches(Regex("^\\d{10,11}$"))) {
                                        phoneError = "Số điện thoại phải là 10-11 số"
                                        valid = false
                                    }

                                    if (valid) {

                                        val id = session["id"] as? Int ?: 0
                                        val indexImage = session["index_image"] as? Int ?: 0
                                        val idTypeAccount = session["id_type_account"] as? Int ?: 0
                                        val role = (session["role"] as? String)?.let { UserRole.valueOf(it) } ?: UserRole.client
                                        val typeAccount = session["type_account"] as? String ?: ""

                                        viewModel.updateUser(
                                            Users(id, fullName, email, phoneNumber, indexImage, idTypeAccount, role)
                                        )

                                        authUser().saveUserSession(
                                            context = context,
                                            username = fullName,
                                            email = email,
                                            id = id,
                                            indexImage = indexImage,
                                            role = role.toString(),
                                            typeAccount = typeAccount,
                                            phone = phoneNumber,
                                            id_type_account = idTypeAccount
                                        )
                                        showUpdateDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue)
                            ) { Text("Cập nhập", color = Color.White) }
                            OutlinedButton(
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.weight(1f)
                            ) { Text("Hủy") }
                        }
                    }
                }
            }
        }

        if(changePassword){
            var passwordError by remember { mutableStateOf<String?>(null) }
            var password by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }
            val error by viewModel.error.collectAsState()
            Dialog(onDismissRequest = { showUpdateDialog = false })
            {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cập nhập mật khẩu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { changePassword = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
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
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isLoading) {
                                        var valid = true
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
                                            viewModel.updatePassword(password)
                                            changePassword = false
                                            authUser().clearUserSession(context)
                                            navController.navigate("login") {
                                                popUpTo("client_home") { inclusive = true }
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
                                        text = "Cập nhập",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun SettingsItemCard(
    item: SettingsItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { item.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = if (item.isDestructive)
                    Color(0xFFFFEBEE)
                else
                    Color(0xFFF3F4F6),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(20.dp)
                        .wrapContentSize(Alignment.Center),
                    tint = if (item.isDestructive)
                        Color(0xFFE53E3E)
                    else
                        Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isDestructive)
                    Color(0xFFE53E3E)
                else
                    Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


