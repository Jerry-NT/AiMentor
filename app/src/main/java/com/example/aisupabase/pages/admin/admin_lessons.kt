package com.example.aisupabase.pages.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.cloudinary.CloudinaryService
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.config.function_handle_public.getPublicIdFromUrl
import com.example.aisupabase.config.function_handle_public.isValidTitle
import com.example.aisupabase.config.function_handle_public.uriToFile
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LessonRepository
import com.example.aisupabase.controllers.LessonResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import courses
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lessons
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


data class ContentSection(
    val content_title: String,
    val content_description: String,
    val example: ExampleContent?
)

data class ExampleContent(
    val example_description: String,
    val code_example: String? = null
)

data class PracticeQuestion(
    val question: String,
    val type: String = "essay"
)

// viewmodel
class AdminLessonsViewModel(
    private val repository: LessonRepository,
    private val course_repository: CourseRepository) : ViewModel()
{
    private val _lessonsList = MutableStateFlow<List<lessons>>(emptyList())
    val lessonsList: StateFlow<List<lessons>> = _lessonsList

    private val _coursesList = MutableStateFlow<List<courses>>(emptyList())
    val coursesList: StateFlow<List<courses>> = _coursesList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchLessons()
        fetchCourses()
    }

    fun fetchLessons() {
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

    fun fetchCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = course_repository.getCourses()) {
                is CourseResult.Success -> _coursesList.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun addLesson(lesson: lessons) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addLesson(
                lesson.id_course,
                lesson.title_lesson,
                lesson.content_lesson,
                lesson.duration,
                lesson.public_id_image,
                lesson.url_image,
                lesson.practice_questions
            )) {
                is LessonResult.Success -> fetchLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun deleteLesson(lessonId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteLesson(lessonId)) {
                is LessonResult.Success -> fetchLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun updateLesson(lesson: lessons) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateLesson(
                lesson.id ?: 0,
                lesson.id_course,
                lesson.title_lesson,
                lesson.content_lesson,
                lesson.duration,
                lesson.public_id_image,
                lesson.url_image,
                lesson.practice_questions
            )) {
                is LessonResult.Success -> fetchLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun checkLessonExists(
        title_lesson: String,
        case: String = "add",
        id: Int? = null,
        id_course: Int,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val exists = repository.checkLessonExists(title_lesson, case, id, id_course)
            onResult(exists)
        }
    }
}

// view factory
class AdminLessonsViewModelFactory(private val supabase: SupabaseClient) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminLessonsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminLessonsViewModel(
                LessonRepository(supabase),
                CourseRepository(supabase)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//  Main Activity
@Composable
fun Admin_Lessons(navController: NavController) {
    // xử lý logic xác thực người dùng, kiểm tra quyền truy cập, v.v.
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "admin") {
            authUser().clearUserSession(context)
            navController.navigate("login");
        }
    }
    val supabase = SupabaseClientProvider.client
    LessionManagermentApp(supabase = supabase, navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessionManagermentApp(
    supabase: SupabaseClient,
    viewModel: AdminLessonsViewModel = viewModel(factory = AdminLessonsViewModelFactory(supabase)),
    navController: NavController) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val lessonsList by viewModel.lessonsList.collectAsState()
    val coursesList by viewModel.coursesList.collectAsState()

    var selectedCourse by remember { mutableStateOf<courses?>(null) }
    var selected by remember { mutableStateOf<lessons?>(null) }

    var expanded by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý bài hoc") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            text = error ?: "Unknown error",
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.fetchCourses() }) { Text("Retry") }
                    }
                }

                else -> {
                    // dropdown menu to select course
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Chọn khóa học:",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box {
                            OutlinedTextField(
                                value = selectedCourse?.title_course ?: "",
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true },
                                enabled = false,
                                placeholder = { Text("Chọn khóa học") }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                coursesList.forEach { course ->
                                    DropdownMenuItem(
                                        text = { Text(course.title_course) },
                                        onClick = {
                                            selectedCourse = course
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.padding(8.dp))
                        // list of lessons
                        if (selectedCourse != null) {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Thêm bài học",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm bài học", color = Color.White)
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // filter lesson by selected course <=> lessons.id_course == selectedCourse?.id
                                itemsIndexed(lessonsList.filter { it.id_course == selectedCourse?.id }) { index, lesson ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                                text = "Tiêu đề: ${lesson.title_lesson}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            val contentArray = JSONArray(lesson.content_lesson)
                                            for (i in 1 until contentArray.length()) {
                                                val item = contentArray.getJSONObject(i)
                                                val contentTitle = item.getString("content_title")
                                                val contentDescription = item.getString("content_description")
                                                val example = item.optJSONObject("example")
                                                val exampleDescription = example?.optString("example_description") ?: ""
                                                val codeExample = example?.optString("code_example") ?: ""

                                                Text(
                                                    text = "${i}.${contentTitle}",
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4A5568),
                                                    lineHeight = 28.sp,
                                                    textAlign = TextAlign.Justify,
                                                    modifier = Modifier.padding(bottom = 24.dp)
                                                )
                                                Text(
                                                    text = contentDescription,
                                                    fontSize = 16.sp,
                                                    color = Color(0xFF4A5568),
                                                    lineHeight = 28.sp,
                                                    textAlign = TextAlign.Justify,
                                                    modifier = Modifier.padding(bottom = 24.dp)
                                                )
                                                if (exampleDescription.isNotEmpty()) {
                                                    Text(
                                                        text ="Ví dụ: $exampleDescription",

                                                        fontSize = 16.sp,
                                                        lineHeight = 28.sp,
                                                        textAlign = TextAlign.Justify,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(bottom = 24.dp)
                                                    )
                                                }
                                                if (codeExample.isNotEmpty()) {
                                                    Surface(
                                                        color = Color(0xFFF5F5F5),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 24.dp)
                                                    ) {
                                                        Text(
                                                            text = codeExample,
                                                            fontSize = 16.sp,
                                                            color = Color(0xFF2D3748),
                                                            lineHeight = 24.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier.padding(16.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }

                                            val practiceQuestions = lesson.practice_questions
                                            if (practiceQuestions.isNotEmpty()) {
                                                Text(
                                                    text = "Câu hỏi thực hành",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2D3748),
                                                    lineHeight = 28.sp,
                                                    modifier = Modifier.padding(bottom = 16.dp)
                                                )
                                                val questionsArray = JSONArray(practiceQuestions)
                                                for (i in 0 until questionsArray.length()) {
                                                    val question = questionsArray.getJSONObject(i)
                                                    val questionText = question.getString("question")
                                                    Text(
                                                        text = "${i + 1}. $questionText",
                                                        fontSize = 16.sp,
                                                        color = Color(0xFF4A5568),
                                                        lineHeight = 28.sp,
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Thời gian: ${lesson.duration} phút",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            // Action buttons remain the same
                                            Spacer(modifier = Modifier.height(24.dp))
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

                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("") }

        // Content sections state - bắt đầu với 1 section trống
        var contentSections by remember {
            mutableStateOf(listOf(ContentSection("", "", ExampleContent("", ""))))
        }

        // Practice questions state - bắt đầu với 3 câu hỏi trống
        var practiceQuestions by remember {
            mutableStateOf(listOf(
                PracticeQuestion("", "essay"),
                PracticeQuestion("", "essay"),
                PracticeQuestion("", "essay")
            ))
        }

        var errorMsg by remember { mutableStateOf<String?>(null) }
        var errorDuration by remember { mutableStateOf<String?>(null) }
        var errorContent by remember { mutableStateOf<String?>(null) }

        val context = LocalContext.current

        // Image states
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imageUrl by remember { mutableStateOf<String?>(null) }
        var imagePublicId by remember { mutableStateOf<String?>(null) }
        var isUploading by remember { mutableStateOf(false) }
        var uploadError by remember { mutableStateOf<String?>(null) }
        var imageFileToUpload by remember { mutableStateOf<File?>(null) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
            uploadError = null
            if (uri != null) {
                val file = uriToFile(context, uri)
                if (file != null) {
                    imageFileToUpload = file
                } else {
                    uploadError = "Không thể đọc file ảnh!"
                    isUploading = false
                }
            }
        }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f) // Giới hạn chiều cao để có thể scroll
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Thêm Bài Học", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showAddDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    // Title field
                    item {
                        Text("Tiêu đề bài học", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nhập tiêu đề bài học") },
                            singleLine = true,
                            isError = errorMsg != null
                        )
                        if (errorMsg != null) {
                            Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Duration field
                    item {
                        Text("Thời gian học (phút)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it; errorDuration = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nhập thời gian học (1-719 phút)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errorDuration != null
                        )
                        if (errorDuration != null) {
                            Text(errorDuration!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Content Sections Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nội dung bài học",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                            OutlinedButton(
                                onClick = {
                                    contentSections = contentSections + ContentSection("", "", ExampleContent("", ""))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm phần", fontSize = 12.sp)
                            }
                        }
                        if (errorContent != null) {
                            Text(errorContent!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Content Sections
                    itemsIndexed(contentSections) { index, section ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Phần ${index + 1}",
                                        fontWeight = FontWeight.Medium,
                                        color = Blue
                                    )
                                    if (contentSections.size > 1) {
                                        IconButton(
                                            onClick = {
                                                contentSections = contentSections.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa phần",
                                                tint = Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Content Title
                                OutlinedTextField(
                                    value = section.content_title,
                                    onValueChange = { newTitle ->
                                        contentSections = contentSections.toMutableList().apply {
                                            this[index] = section.copy(content_title = newTitle)
                                        }
                                        errorContent = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Tiêu đề phần nội dung") },
                                    placeholder = { Text("Nhập tiêu đề phần nội dung") },
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Content Description
                                OutlinedTextField(
                                    value = section.content_description,
                                    onValueChange = { newDesc ->
                                        contentSections = contentSections.toMutableList().apply {
                                            this[index] = section.copy(content_description = newDesc)
                                        }
                                        errorContent = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nội dung chi tiết") },
                                    placeholder = { Text("Nhập nội dung chi tiết (250-1500 ký tự)") },
                                    minLines = 3,
                                    maxLines = 5
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Example section
                                Text(
                                    "Ví dụ minh họa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6B7280)
                                )

                                OutlinedTextField(
                                    value = section.example?.example_description ?: "",
                                    onValueChange = { newExampleDesc ->
                                        contentSections = contentSections.toMutableList().apply {
                                            val currentExample = section.example ?: ExampleContent("", "")
                                            this[index] = section.copy(
                                                example = currentExample.copy(example_description = newExampleDesc)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Mô tả ví dụ") },
                                    placeholder = { Text("Mô tả ví dụ ngắn gọn (~150 ký tự)") },
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = section.example?.code_example ?: "",
                                    onValueChange = { newCode ->
                                        contentSections = contentSections.toMutableList().apply {
                                            val currentExample = section.example ?: ExampleContent("", "")
                                            this[index] = section.copy(
                                                example = currentExample.copy(code_example = newCode)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Code ví dụ (tùy chọn)") },
                                    placeholder = { Text("Mã code minh họa (~150 ký tự)") },
                                    minLines = 2,
                                    maxLines = 4
                                )
                            }
                        }
                    }

                    // Practice Questions Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Câu hỏi thực hành",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                            OutlinedButton(
                                onClick = {
                                    practiceQuestions = practiceQuestions + PracticeQuestion("", "essay")
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm câu hỏi", fontSize = 12.sp)
                            }
                        }
                    }

                    // Practice Questions
                    itemsIndexed(practiceQuestions) { index, question ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Câu hỏi ${index + 1}",
                                        fontWeight = FontWeight.Medium,
                                        color = Blue
                                    )
                                    if (practiceQuestions.size > 1) {
                                        IconButton(
                                            onClick = {
                                                practiceQuestions = practiceQuestions.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa câu hỏi",
                                                tint = Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = question.question,
                                    onValueChange = { newQuestion ->
                                        practiceQuestions = practiceQuestions.toMutableList().apply {
                                            this[index] = question.copy(question = newQuestion)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Câu hỏi thực hành") },
                                    placeholder = { Text("Nhập câu hỏi (15-248 ký tự)") },
                                    minLines = 2,
                                    maxLines = 3
                                )
                            }
                        }
                    }

                    // Image selection
                    item {
                        Text("Ảnh bài học", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = !isUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = Blue)
                            ) {
                                Text(
                                    if (isUploading) "Đang tải..." else "Chọn ảnh",
                                    color = Color.White
                                )
                            }
                            if (uploadError != null) {
                                Text(
                                    uploadError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Ảnh đã chọn",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(top = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Action buttons
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val coroutineScope = rememberCoroutineScope()
                            Button(
                                onClick = {
                                    var isValid = true

                                    // Validate title
                                    if (!isValidTitle(title)) {
                                        errorMsg = "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                        isValid = false
                                    }

                                    // Validate duration
                                    if (duration.isEmpty() || !duration.all { it.isDigit() } ||
                                        duration.toInt() <= 0 || duration.toInt() >= 720) {
                                        errorDuration = "Thời gian học không hợp lệ (phải là số nguyên dương và nhỏ hơn 720)"
                                        isValid = false
                                    }

                                    // Validate content sections
                                    val validContentSections = contentSections.filter {
                                        it.content_title.isNotBlank() &&
                                                it.content_description.isNotBlank() &&
                                                it.content_description.length >= 250 &&
                                                it.content_description.length <= 1500
                                    }

                                    if (validContentSections.isEmpty()) {
                                        errorContent = "Cần ít nhất 1 phần nội dung hợp lệ (tiêu đề không rỗng, nội dung 250-1500 ký tự)"
                                        isValid = false
                                    }

                                    // Validate practice questions
                                    val validPracticeQuestions = practiceQuestions.filter {
                                        it.question.isNotBlank() &&
                                                it.question.length >= 15 &&
                                                it.question.length <= 248
                                    }

                                    if (validPracticeQuestions.size < 0) {
                                        errorContent = "Cần ít nhất 1 câu hỏi thực hành hợp lệ (15-248 ký tự)"
                                        isValid = false
                                    }

                                    if (isValid) {
                                        viewModel.checkLessonExists(
                                            title,
                                            "add",
                                            null,
                                            selectedCourse?.id ?: 0
                                        ) { exists ->
                                            if (exists) {
                                                errorMsg = "Tiêu đề bài học đã tồn tại"
                                            } else {
                                                isUploading = true
                                                uploadError = null

                                                // Create new JSON structure
                                                val contentLessonArray = JSONArray()
                                                contentLessonArray.put(JSONObject())

                                                validContentSections.forEach { section ->
                                                    val sectionObj = JSONObject().apply {
                                                        put("content_title", section.content_title)
                                                        put("content_description", section.content_description)

                                                        if (section.example != null && section.example.example_description.isNotBlank()) {
                                                            val exampleObj = JSONObject().apply {
                                                                put("example_description", section.example.example_description)
                                                                if (!section.example.code_example.isNullOrBlank()) {
                                                                    put("code_example", section.example.code_example)
                                                                }
                                                            }
                                                            put("example", exampleObj)
                                                        }
                                                    }
                                                    contentLessonArray.put(sectionObj)
                                                }

                                                val practiceQuestionsArray = JSONArray()
                                                validPracticeQuestions.forEach { question ->
                                                    val questionObj = JSONObject().apply {
                                                        put("question", question.question)
                                                        put("type", question.type)
                                                    }
                                                    practiceQuestionsArray.put(questionObj)

                                                }


                                                coroutineScope.launch {
                                                    val file = imageFileToUpload
                                                    if (file != null) {
                                                        val url = CloudinaryService.uploadImage(file)
                                                        if (url != null) {
                                                            imageUrl = url
                                                            imagePublicId = getPublicIdFromUrl(url)

                                                            viewModel.addLesson(
                                                                lessons(
                                                                    null,
                                                                    id_course = selectedCourse?.id ?: 0,
                                                                    title_lesson = title.trim(),
                                                                    content_lesson =  contentLessonArray.toString(),
                                                                    duration = duration.toInt(),
                                                                    public_id_image = imagePublicId ?: "",
                                                                    url_image = imageUrl ?: "",
                                                                    practice_questions = practiceQuestionsArray.toString()
                                                                )
                                                            )
                                                            showAddDialog = false
                                                            isUploading = false
                                                        } else {
                                                            uploadError = "Upload ảnh thất bại!"
                                                            isUploading = false
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                enabled = !isUploading
                            ) {
                                Text(
                                    if (isUploading) "Đang xử lý..." else "Thêm bài học",
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { showAddDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Hủy")
                            }
                        }
                    }
                }
            }
        }
    }

    // Update Dialog
    if (showUpdateDialog && selected != null) {
        var title by remember { mutableStateOf(selected!!.title_lesson) }
        var duration by remember { mutableStateOf(selected!!.duration.toString()) }

        // Parse existing content_lesson JSON to ContentSection list
        var contentSections by remember {
            mutableStateOf(
                try {
                    val contentArray = JSONArray(selected!!.content_lesson)
                    val sections = mutableListOf<ContentSection>()

                    // Bắt đầu từ index 1 nếu index 0 là object rỗng
                    val startIndex = if (contentArray.length() > 0 &&
                        contentArray.getJSONObject(0).length() == 0) 1 else 0

                    for (i in startIndex until contentArray.length()) {
                        val item = contentArray.getJSONObject(i)
                        val contentTitle = item.optString("content_title", "")
                        val contentDescription = item.optString("content_description", "")
                        val example = item.optJSONObject("example")
                        val exampleDescription = example?.optString("example_description", "") ?: ""
                        val codeExample = example?.optString("code_example", "") ?: ""

                        if (contentTitle.isNotEmpty() || contentDescription.isNotEmpty()) {
                            sections.add(
                                ContentSection(
                                    content_title = contentTitle,
                                    content_description = contentDescription,
                                    example = ExampleContent(
                                        example_description = exampleDescription,
                                        code_example = codeExample
                                    )
                                )
                            )
                        }
                    }

                    // Nếu không có section nào, tạo một section trống
                    if (sections.isEmpty()) {
                        sections.add(ContentSection("", "", ExampleContent("", "")))
                    }

                    sections
                } catch (e: Exception) {
                    listOf(ContentSection("", "", ExampleContent("", "")))
                }
            )
        }

        // Parse existing practice_questions JSON to PracticeQuestion list
        var practiceQuestions by remember {
            mutableStateOf(
                try {
                    val questionsArray = JSONArray(selected!!.practice_questions)
                    val questions = mutableListOf<PracticeQuestion>()

                    for (i in 0 until questionsArray.length()) {
                        val question = questionsArray.getJSONObject(i)
                        val questionText = question.optString("question", "")
                        val questionType = question.optString("type", "essay")

                        if (questionText.isNotEmpty()) {
                            questions.add(PracticeQuestion(questionText, questionType))
                        }
                    }

                    // Nếu không có câu hỏi nào, tạo 3 câu hỏi trống
                    if (questions.isEmpty()) {
                        repeat(3) {
                            questions.add(PracticeQuestion("", "essay"))
                        }
                    }

                    questions
                } catch (e: Exception) {
                    listOf(
                        PracticeQuestion("", "essay"),
                        PracticeQuestion("", "essay"),
                        PracticeQuestion("", "essay")
                    )
                }
            )
        }

        var errorMsg by remember { mutableStateOf<String?>(null) }
        var errorDuration by remember { mutableStateOf<String?>(null) }
        var errorContent by remember { mutableStateOf<String?>(null) }

        val context = LocalContext.current

        // Image states
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imageUrl by remember { mutableStateOf<String?>(null) }
        var imagePublicId by remember { mutableStateOf<String?>(null) }
        var isUploading by remember { mutableStateOf(false) }
        var uploadError by remember { mutableStateOf<String?>(null) }
        var imageFileToUpload by remember { mutableStateOf<File?>(null) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
            uploadError = null
            if (uri != null) {
                val file = uriToFile(context, uri)
                if (file != null) {
                    imageFileToUpload = file
                } else {
                    uploadError = "Không thể đọc file ảnh!"
                    isUploading = false
                }
            }
        }

        // Set initial image
        LaunchedEffect(showUpdateDialog, selected) {
            if (showUpdateDialog && selected?.url_image != null) {
                imageUri = Uri.parse(selected?.url_image)
            }
        }

        Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cập Nhật Bài Học", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showUpdateDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    // Title field
                    item {
                        Text("Tiêu đề bài học", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; errorMsg = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nhập tiêu đề bài học") },
                            singleLine = true,
                            isError = errorMsg != null
                        )
                        if (errorMsg != null) {
                            Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Duration field
                    item {
                        Text("Thời gian học (phút)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it; errorDuration = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nhập thời gian học (1-719 phút)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = errorDuration != null
                        )
                        if (errorDuration != null) {
                            Text(errorDuration!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Content Sections Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Nội dung bài học",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                            OutlinedButton(
                                onClick = {
                                    contentSections = contentSections + ContentSection("", "", ExampleContent("", ""))
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm phần", fontSize = 12.sp)
                            }
                        }
                        if (errorContent != null) {
                            Text(errorContent!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    // Content Sections
                    itemsIndexed(contentSections) { index, section ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Phần ${index + 1}",
                                        fontWeight = FontWeight.Medium,
                                        color = Blue
                                    )
                                    if (contentSections.size > 1) {
                                        IconButton(
                                            onClick = {
                                                contentSections = contentSections.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa phần",
                                                tint = Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Content Title
                                OutlinedTextField(
                                    value = section.content_title,
                                    onValueChange = { newTitle ->
                                        contentSections = contentSections.toMutableList().apply {
                                            this[index] = section.copy(content_title = newTitle)
                                        }
                                        errorContent = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Tiêu đề phần nội dung") },
                                    placeholder = { Text("Nhập tiêu đề phần nội dung") },
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Content Description
                                OutlinedTextField(
                                    value = section.content_description,
                                    onValueChange = { newDesc ->
                                        contentSections = contentSections.toMutableList().apply {
                                            this[index] = section.copy(content_description = newDesc)
                                        }
                                        errorContent = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nội dung chi tiết") },
                                    placeholder = { Text("Nhập nội dung chi tiết (250-1500 ký tự)") },
                                    minLines = 3,
                                    maxLines = 5
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Example section
                                Text(
                                    "Ví dụ minh họa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6B7280)
                                )

                                OutlinedTextField(
                                    value = section.example?.example_description ?: "",
                                    onValueChange = { newExampleDesc ->
                                        contentSections = contentSections.toMutableList().apply {
                                            val currentExample = section.example ?: ExampleContent("", "")
                                            this[index] = section.copy(
                                                example = currentExample.copy(example_description = newExampleDesc)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Mô tả ví dụ") },
                                    placeholder = { Text("Mô tả ví dụ ngắn gọn (~150 ký tự)") },
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = section.example?.code_example ?: "",
                                    onValueChange = { newCode ->
                                        contentSections = contentSections.toMutableList().apply {
                                            val currentExample = section.example ?: ExampleContent("", "")
                                            this[index] = section.copy(
                                                example = currentExample.copy(code_example = newCode)
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Code ví dụ (tùy chọn)") },
                                    placeholder = { Text("Mã code minh họa (~150 ký tự)") },
                                    minLines = 2,
                                    maxLines = 4
                                )
                            }
                        }
                    }

                    // Practice Questions Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Câu hỏi thực hành",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue
                            )
                            OutlinedButton(
                                onClick = {
                                    practiceQuestions = practiceQuestions + PracticeQuestion("", "essay")
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm câu hỏi", fontSize = 12.sp)
                            }
                        }
                    }

                    // Practice Questions
                    itemsIndexed(practiceQuestions) { index, question ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Câu hỏi ${index + 1}",
                                        fontWeight = FontWeight.Medium,
                                        color = Blue
                                    )
                                    if (practiceQuestions.size > 1) {
                                        IconButton(
                                            onClick = {
                                                practiceQuestions = practiceQuestions.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa câu hỏi",
                                                tint = Red,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = question.question,
                                    onValueChange = { newQuestion ->
                                        practiceQuestions = practiceQuestions.toMutableList().apply {
                                            this[index] = question.copy(question = newQuestion)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Câu hỏi thực hành") },
                                    placeholder = { Text("Nhập câu hỏi (15-248 ký tự)") },
                                    minLines = 2,
                                    maxLines = 3
                                )
                            }
                        }
                    }

                    // Image selection
                    item {
                        Text("Ảnh bài học", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = !isUploading,
                                colors = ButtonDefaults.buttonColors(containerColor = Blue)
                            ) {
                                Text(
                                    if (isUploading) "Đang tải..." else "Chọn ảnh",
                                    color = Color.White
                                )
                            }
                            if (uploadError != null) {
                                Text(
                                    uploadError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Ảnh đã chọn",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(top = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Action buttons
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val coroutineScope = rememberCoroutineScope()
                            Button(
                                onClick = {
                                    var isValid = true

                                    // Validate title
                                    if (!isValidTitle(title)) {
                                        errorMsg = "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                        isValid = false
                                    }

                                    // Validate duration
                                    if (duration.isEmpty() || !duration.all { it.isDigit() } ||
                                        duration.toInt() <= 0 || duration.toInt() >= 720) {
                                        errorDuration = "Thời gian học không hợp lệ (phải là số nguyên dương và nhỏ hơn 720)"
                                        isValid = false
                                    }

                                    // Validate content sections
                                    val validContentSections = contentSections.filter {
                                        it.content_title.isNotBlank() &&
                                                it.content_description.isNotBlank() &&
                                                it.content_description.length >= 250 &&
                                                it.content_description.length <= 1500
                                    }

                                    if (validContentSections.isEmpty()) {
                                        errorContent = "Cần ít nhất 1 phần nội dung hợp lệ (tiêu đề không rỗng, nội dung 250-1500 ký tự)"
                                        isValid = false
                                    }

                                    // Validate practice questions
                                    val validPracticeQuestions = practiceQuestions.filter {
                                        it.question.isNotBlank() &&
                                                it.question.length >= 15 &&
                                                it.question.length <= 248
                                    }

                                    if (validPracticeQuestions.isEmpty()) {
                                        errorContent = "Cần ít nhất 1 câu hỏi thực hành hợp lệ (15-248 ký tự)"
                                        isValid = false
                                    }

                                    if (isValid) {
                                        viewModel.checkLessonExists(
                                            title,
                                            "update",
                                            selected?.id,
                                            selectedCourse?.id ?: 0
                                        ) { exists ->
                                            if (exists) {
                                                errorMsg = "Tiêu đề bài học đã tồn tại"
                                            } else {
                                                isUploading = true
                                                uploadError = null

                                                // Create new JSON structure
                                                val contentLessonArray = JSONArray()
                                                // Thêm object rỗng ở đầu để khớp với logic hiển thị
                                                contentLessonArray.put(JSONObject())

                                                validContentSections.forEach { section ->
                                                    val sectionObj = JSONObject().apply {
                                                        put("content_title", section.content_title)
                                                        put("content_description", section.content_description)

                                                        if (section.example != null && section.example.example_description.isNotBlank()) {
                                                            val exampleObj = JSONObject().apply {
                                                                put("example_description", section.example.example_description)
                                                                if (!section.example.code_example.isNullOrBlank()) {
                                                                    put("code_example", section.example.code_example)
                                                                }
                                                            }
                                                            put("example", exampleObj)
                                                        }
                                                    }
                                                    contentLessonArray.put(sectionObj)
                                                }

                                                val practiceQuestionsArray = JSONArray()
                                                validPracticeQuestions.forEach { question ->
                                                    val questionObj = JSONObject().apply {
                                                        put("question", question.question)
                                                        put("type", question.type)
                                                    }
                                                    practiceQuestionsArray.put(questionObj)
                                                }

                                                coroutineScope.launch {
                                                    val file = imageFileToUpload
                                                    if (file != null) {
                                                        // Upload new image
                                                        val url = CloudinaryService.uploadImage(file)
                                                        if (url != null) {
                                                            imageUrl = url
                                                            imagePublicId = getPublicIdFromUrl(url)

                                                            // Delete old image
                                                            if (selected?.url_image != null) {
                                                                val oldPublicId = getPublicIdFromUrl(selected!!.url_image)
                                                                CloudinaryService.deleteImage(oldPublicId)
                                                            }
                                                        } else {
                                                            uploadError = "Upload ảnh thất bại!"
                                                            isUploading = false
                                                            return@launch
                                                        }
                                                    } else {
                                                        // Keep existing image
                                                        imageUrl = selected?.url_image
                                                        imagePublicId = selected?.public_id_image
                                                    }

                                                    val updatedLesson = lessons(
                                                        selected!!.id,
                                                        id_course = selectedCourse?.id ?: 0,
                                                        title_lesson = title.trim(),
                                                        content_lesson = contentLessonArray.toString(),
                                                        duration = duration.toInt(),
                                                        public_id_image = imagePublicId ?: "",
                                                        url_image = imageUrl ?: "",
                                                        practice_questions = practiceQuestionsArray.toString()
                                                    )

                                                    viewModel.updateLesson(updatedLesson)
                                                    showUpdateDialog = false
                                                    isUploading = false
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                enabled = !isUploading
                            ) {
                                Text(
                                    if (isUploading) "Đang xử lý..." else "Cập nhật",
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Hủy")
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog && selected != null) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Xác nhận xóa",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "Bạn có thực sự muốn xóa ?",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                selected?.let { viewModel.deleteLesson(it.id ?: 0) }
                                showDeleteDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) { Text("Xóa", color = Color.White) }
                        OutlinedButton(
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f)
                        ) { Text("Hủy") }
                    }
                }
            }
        }
    }
}
