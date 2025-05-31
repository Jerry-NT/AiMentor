package com.example.aisupabase.pages

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.*
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Users
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.firstOrNull

// ViewModel
class CoursesViewModel(private val repository: CourseRepository) : ViewModel() {
    private val _coursesList = MutableStateFlow<List<courses>>(emptyList())
    val coursesList: StateFlow<List<courses>> = _coursesList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getCourses()
    }

    fun getCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getCourses()) {
                is CourseResult.Success -> _coursesList.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun deleteCourse(course: courses) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteCourse(course.id ?: 0)) {
                is CourseResult.Success -> getCourses()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun updateCourse(course: courses, title: String, desc: String, total: Int, publicId: String, url: String, isPrivate: Boolean, userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateCourse(course.id ?: 0, title, desc, total, publicId, url, isPrivate, userId)) {
                is CourseResult.Success -> getCourses()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun addCourse(title: String, desc: String, total: Int, publicId: String, url: String, isPrivate: Boolean, userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addCourse(title, desc, total, publicId, url, isPrivate, userId)) {
                is CourseResult.Success -> getCourses()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
}

// ViewModel Factory
class CoursesViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoursesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoursesViewModel(CourseRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Trang chính
@Composable
fun Admin_Courses(navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "admin") {
            authUser().clearUserSession(context)
            navController.navigate("login")
        }
    }

    val supabase = SupabaseClientProvider.client
    CourseManagementApp(supabase = supabase)
}

// Giao diện chính
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManagementApp(
    supabase: SupabaseClient,
    viewModel: CoursesViewModel = viewModel(factory = CoursesViewModelFactory(supabase))
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coursesList by viewModel.coursesList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<courses?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Khóa học") },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm", color = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )

            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "Lỗi không xác định",
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.getCourses() }) {
                            Text("Thử lại")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // fillter course public -> tu viet
                        itemsIndexed(coursesList) { index, course ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Số thứ tự: ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Tiêu đề: ${course.title_course}", fontSize = 14.sp)
                                    Text("Mô tả: ${course.des_course}", fontSize = 14.sp)
                                    UsersText(supabase, course.user_create)
                                    // hien thi lo trinh id_roadmaps
                                    AsyncImage(
                                        model = course.url_image,
                                        contentDescription = "Ảnh khóa học",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    course.created_at?.let {
                                        Text("Ngày tạo: $it", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selected = course
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                selected = course
                                                showDeleteDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Red),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Xóa", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }
}
