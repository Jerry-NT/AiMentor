package com.example.aisupabase.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.config.SupabaseClientProvider.client
import com.example.aisupabase.controllers.*
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.supabaseJson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lessons

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
    LessonManagementApp(supabase = supabase)
}

class LessonsViewModel(private val repository: LessonRepository) : ViewModel() {
    private val _lessonsList = MutableStateFlow<List<lessons>>(emptyList())
    val lessonsList: StateFlow<List<lessons>> = _lessonsList

    private val _coursesList = MutableStateFlow<List<courses>>(emptyList())
    val coursesList: StateFlow<List<courses>> = _coursesList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getLessons()
    }

    fun getLessons() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getLessons()) {
                is LessonResult.Success -> _lessonsList.value = result.data ?: emptyList()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }


    suspend fun getCourses(): List<courses> {
        val response = client.from("courses").select().decodeList<courses>()
        return response
    }



    fun deleteLesson(lesson: lessons) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteLesson(lesson.id ?: 0)) {
                is LessonResult.Success -> getLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun updateLesson(lesson: lessons, id_course: Int, title_lession: String, content_lession: String, duration: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateLesson(
                lesson.id ?: 0,
                id_course,
                title_lession,
                content_lession,
                duration
            )) {
                is LessonResult.Success -> getLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun addLesson(id_course: Int, title_lession: String, content_lession: String, duration: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addLesson(id_course, title_lession, content_lession, duration)) {
                is LessonResult.Success -> getLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonManagementApp(
    supabase: SupabaseClient,
    viewModel: LessonsViewModel = viewModel(factory = LessonsViewModelFactory(supabase))
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val lessonsList by viewModel.lessonsList.collectAsState()
    val coursesList by viewModel.coursesList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<lessons?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Bài học") },
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
                        Button(onClick = { viewModel.getLessons() }) {
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
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("STT: ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("ID Khóa học: ${lesson.id_course}", fontSize = 14.sp)
                                    Text("Tiêu đề bài học: ${lesson.title_lession}", fontSize = 14.sp)
                                    Text("Nội dung bài học: ${lesson.content_lession}", fontSize = 14.sp)
                                    Text("Thời lượng: ${lesson.duration}", fontSize = 14.sp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selected = lesson
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }
                                        Button(
                                            onClick = {
                                                selected = lesson
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

            if (showAddDialog) {
                LessonDialog(
                    title = "Thêm Bài học",
                    courses = coursesList,
                    onConfirm = { id, title, content, duration ->
                        viewModel.addLesson(id, title, content, duration)
                        showAddDialog = false
                    },
                    onDismiss = { showAddDialog = false }
                )
            }

            if (showUpdateDialog && selected != null) {
                LessonDialog(
                    title = "Cập nhật Bài học",
                    courses = coursesList,
                    initCourseId = selected!!.id_course,
                    initTitle = selected!!.title_lession,
                    initContent = selected!!.content_lession,
                    initDuration = selected!!.duration,
                    onConfirm = { id, title, content, duration ->
                        viewModel.updateLesson(selected!!, id, title, content, duration)
                        showUpdateDialog = false
                    },
                    onDismiss = { showUpdateDialog = false }
                )
            }

            if (showDeleteDialog && selected != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn có chắc chắn muốn xóa bài học này không?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteLesson(selected!!)
                            showDeleteDialog = false
                        }) {
                            Text("Xóa", color = Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDialog(
    title: String,
    courses: List<courses>,
    initCourseId: Int = courses.firstOrNull()?.id ?: 0,
    initTitle: String = "",
    initContent: String = "",
    initDuration: String = "",
    onConfirm: (Int, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCourseId: Int? by remember { mutableStateOf(initCourseId) }
    var expanded by remember { mutableStateOf(false) }
    var titleText by remember { mutableStateOf(initTitle) }
    var contentText by remember { mutableStateOf(initContent) }
    var durationText by remember { mutableStateOf(initDuration) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = courses.find { it.id == selectedCourseId }?.title_course ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Khóa học") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.title_course) },
                                onClick = {
                                    selectedCourseId = course.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCourseId != 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = titleText, onValueChange = { titleText = it }, label = { Text("Tiêu đề") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = contentText, onValueChange = { contentText = it }, label = { Text("Nội dung") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = durationText, onValueChange = { durationText = it }, label = { Text("Thời lượng") })
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vui lòng chọn khóa học trước", color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if ((selectedCourseId ?: 0) != 0) {
                    onConfirm(selectedCourseId ?: 0, titleText, contentText, durationText)
                }
            }, enabled = (selectedCourseId ?: 0) != 0) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
