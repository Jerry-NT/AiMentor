package com.example.aisupabase.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.LessonRepository
import com.example.aisupabase.controllers.LessonResult
import com.example.aisupabase.controllers.authUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lessons
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.OptIn
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.emptyList
import kotlin.collections.find
import kotlin.collections.first
import kotlin.collections.listOf
import kotlin.collections.plus
import kotlin.let
import kotlin.plus
import kotlin.sequences.plus
import kotlin.toString

// Thêm data class Course để demo (bạn có thể lấy từ DB nếu có)
data class Course(val id: String, val title: String)

class LessonsViewModel(private val repository: LessonRepository) : ViewModel() {
    private val _lessonsList = MutableStateFlow<List<lessons>>(emptyList())
    val lessonsList: StateFlow<List<lessons>> = _lessonsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Hàm lấy lesson theo courseId
    fun getLessonsByCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getLessons(courseId)) {
                is LessonResult.Success -> _lessonsList.value = result.data ?: emptyList()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    // Placeholder cho hàm thêm, sửa, xóa
    fun addLesson() { /* logic thêm */ }
    fun editLesson(lesson: lessons) { /* logic sửa */ }
    fun deleteLesson(lesson: lessons) { /* logic xóa */ }
}

class LessonsViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LessonsViewModel(LessonRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Admin_Lessons(navController: NavController) {
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
    LessonsManagementApp(supabase = supabase)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsManagementApp(
    supabase: SupabaseClient,
    viewModel: LessonsViewModel = viewModel(factory = LessonsViewModelFactory(supabase))
) {
    // Giả sử có danh sách khóa học tĩnh (bạn có thể lấy từ DB nếu có)
    val courses = listOf(
        Course("course1", "Khóa học 1"),
        Course("course2", "Khóa học 2"),
        Course("course3", "Khóa học 3"),
    )

    var selectedCourseId by remember { mutableStateOf(courses.first().id) }

    LaunchedEffect(selectedCourseId) {
        viewModel.getLessonsByCourse(selectedCourseId)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val lessonsList by viewModel.lessonsList.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Quản lý Bài học") })
                // Dropdown chọn course
                CourseDropdown(
                    courses = courses,
                    selectedCourseId = selectedCourseId,
                    onCourseSelected = { selectedCourseId = it.toString() }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.addLesson() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Thêm bài học") },
                text = { Text("Thêm") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
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
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.getLessonsByCourse(selectedCourseId) }) {
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
                        itemsIndexed(lessonsList) { index, lesson ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Số thứ tự: ${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "ID khóa học: ${lesson.id_course}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Tiêu đề bài học: ${lesson.title_lession}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Nội dung bài học: ${lesson.content_lession}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Thời lượng: ${lesson.duration}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { viewModel.editLesson(lesson) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa")
                                        }
                                        Button(
                                            onClick = { viewModel.deleteLesson(lesson) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Xóa")
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

@Composable
fun CourseDropdown(
    courses: List<Course>,
    selectedCourseId: String,
    onCourseSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCourse = courses.find { it.id == selectedCourseId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedCourse?.id ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Chọn khóa học") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            courses.forEach { course ->
                DropdownMenuItem(
                    text = { Text(course.id) },
                    onClick = {
                        course.id?.let { onCourseSelected(it) }
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun ColumnScope.onCourseSelected(string: String) {}





