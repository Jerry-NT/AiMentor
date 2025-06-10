package com.example.aisupabase.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.config.handle.isValidTitle
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


// viewmodel
class AdminLessonsViewModel(private val repository: LessonRepository, private val course_repository: CourseRepository) : ViewModel(){
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
            when (val result = repository.addLesson(lesson.id_course, lesson.title_lesson, lesson.content_lesson, lesson.duration)) {
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
            when (val result = repository.updateLesson(lesson.id ?: 0, lesson.id_course, lesson.title_lesson, lesson.content_lesson, lesson.duration)) {
                is LessonResult.Success -> fetchLessons()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun checkLessonExists(title_lesson: String,case: String = "add", id: Int? = null,id_course:Int,onResult: (Boolean)-> Unit) {
        viewModelScope.launch {
            val exists = repository.checkLessonExists(title_lesson,case,id,id_course)
            onResult(exists)
        }
    }
}

// view factory
class AdminLessonsViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminLessonsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminLessonsViewModel(LessonRepository(supabase), CourseRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//  Main Activity
@Composable
fun Admin_Lessons( navController: NavController) {
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
    LessionManagermentApp(supabase)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessionManagermentApp(supabase: SupabaseClient, viewModel: AdminLessonsViewModel = viewModel(factory = AdminLessonsViewModelFactory(supabase)))
{
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
            )
        }
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            when{
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
                    ){
                        Text("Chọn khóa học:", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
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
                        if(selectedCourse != null)
                        {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm bài học", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm bài học", color = Color.White)
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ){
                                // filter lesson by selected course <=> lessons.id_course == selectedCourse?.id
                                itemsIndexed(lessonsList.filter { it.id_course == selectedCourse?.id }) { index, lesson ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ){
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ){
                                            // Số thứ tự
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

                                            val jsonLesson = try {
                                                org.json.JSONObject(lesson.content_lesson)
                                            } catch (e: Exception) {
                                                null
                                            }

                                            if (jsonLesson != null) {
                                                val contentLession = jsonLesson.optString("content_lession")
                                                val exampleObj = jsonLesson.optJSONObject("example")
                                                val desShort = exampleObj?.optString("des_short") ?: ""
                                                val code = exampleObj?.optString("code") ?: ""
                                                Column {
                                                    Text(
                                                        text = "Nội dung khóa học: $contentLession",
                                                        fontSize = 15.sp,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                    Text(
                                                        text = "Ví dụ: $desShort",
                                                        fontSize = 15.sp,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                    if(code != ""){
                                                        Text(
                                                            text = "Code mẫu: $code",
                                                            fontSize = 15.sp,
                                                            modifier = Modifier.padding(bottom = 4.dp)
                                                        )
                                                    }}
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Thời gian: ${lesson.duration} phút ",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
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
        var content by remember { mutableStateOf("") }
        var des_short by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var errorContent by remember { mutableStateOf<String?>(null) }
        var errorDuration by remember { mutableStateOf<String?>(null) }
        var errorDes_short by remember { mutableStateOf<String?>(null) }


        Dialog(onDismissRequest = { showAddDialog = false }) {
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
                    )
                    {
                        Text("Thêm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Tiêu đề và nội dung
                    Text(
                        "Tiêu đề",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMsg = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập tiêu đề") },
                        singleLine = true,
                        isError = errorMsg != null
                    )
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                    }
                    // noi dung
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nội dung",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                            errorContent = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập nội dung") },
                        singleLine = true,
                        isError = errorContent != null
                    )
                    if (errorContent != null) {
                        Text(errorContent!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nội dung ví dụ",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = des_short,
                        onValueChange = {
                            des_short = it
                            errorDes_short = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nội dung ví dụ") },
                        singleLine = true,
                        isError = errorDes_short != null
                    )
                    if (errorDes_short != null) {
                        Text(errorDes_short!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Code mẫu",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập code mẫu") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Thời gian

                    Text(
                        "Thời gian học",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = {
                            duration = it
                            errorDuration = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập thời gian học (phút)") },
                        singleLine = true,
                        isError = errorDuration != null
                    )
                    if (errorDuration != null) {
                        Text(errorDuration!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = {
                                var check = true
                                if (!isValidTitle(title)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if(!isValidTitle(content)) {
                                    errorContent = "Nội dung không hợp lệ (không rỗng, không ký tự đặc biệt, không dư khoảng trắng)"
                                    check = false
                                }
                                if(!isValidTitle(des_short)) {
                                    errorDes_short = "Nội dung ví dụ không hợp lệ (không rỗng, không ký tự đặc biệt, không dư khoảng trắng)"
                                    check = false
                                }
                                if (duration.isEmpty() || !duration.all { it.isDigit() } || duration.toInt() <= 0 || duration.toInt() >= 720) {
                                    errorDuration = "Thời gian học không hợp lệ (phải là số nguyên dương và nhỏ hơn 720)"
                                    check = false
                                }

                                if (check) {
                                    viewModel.checkLessonExists(title,"add",null,selectedCourse?.id?:0) { exists ->
                                        if (exists) {
                                            errorMsg = "Tiêu đề bài học đã tồn tại"
                                        } else {
                                            // tạo chuỗi json { } cho content_lessonAdd commentMore actions
                                            val exampleObj = org.json.JSONObject()
                                            exampleObj.put("des_short", des_short)
                                            if(code != "") {
                                                exampleObj.put("code", code)
                                            }
                                            // Create the main content_lesson object
                                            val contentLessonObj = org.json.JSONObject()
                                            contentLessonObj.put("content_lession", content)
                                            contentLessonObj.put("example", exampleObj)

                                            viewModel.addLesson(
                                                lessons(
                                                    selected?.id,
                                                    id_course = selectedCourse?.id ?: 0,
                                                    title_lesson = title.trim(),
                                                    content_lesson = contentLessonObj.toString(),
                                                    duration = duration.toIntOrNull() ?: 0
                                                )
                                            )
                                            showAddDialog = false
                                        }
                                    }
                                }

                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) { Text("Thêm", color = Color.White) }
                        OutlinedButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f)
                        ) { Text("Hủy") }
                    }
                }
            }
        }
    }

    // Update Dialog
    if (showUpdateDialog && selected != null) {
        var title by remember { mutableStateOf(selected!!.title_lesson) }
        var duration by remember { mutableStateOf(selected!!.duration) }

        // Parse the content_lesson JSON to extract content and example
        val jsonLesson = try {
            org.json.JSONObject(selected?.content_lesson ?: "{}")
        } catch (e: Exception) {
            null
        }
        val initialContent = jsonLesson?.optString("content_lession") ?: ""
        val exampleObj = jsonLesson?.optJSONObject("example")
        val initialDesShort = exampleObj?.optString("des_short") ?: ""
        val initialCode = exampleObj?.optString("code") ?: ""

        var content by remember { mutableStateOf(initialContent) }
        var des_short by remember { mutableStateOf(initialDesShort) }
        var code by remember { mutableStateOf(initialCode) }

        var errorMsg by remember { mutableStateOf<String?>(null) }
        var errorContent by remember { mutableStateOf<String?>(null) }
        var errorDuration by remember { mutableStateOf<String?>(null) }
        var errorDes_short by remember { mutableStateOf<String?>(null) }

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
                    )
                    {
                        Text("Cập nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showUpdateDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Tiêu đề và nội dung
                    Text(
                        "Tiêu đề",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMsg = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập tiêu đề") },
                        singleLine = true,
                        isError = errorMsg != null
                    )

                    if (errorMsg != null) {
                        Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                    }
                    // noi dung
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nội dung",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                            errorContent = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập nội dung") },
                        singleLine = true,
                        isError = errorContent != null
                    )
                    if (errorContent != null) {
                        Text(errorContent!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Nội dung ví dụ",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = des_short,
                        onValueChange = {
                            des_short = it
                            errorDes_short = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nội dung ví dụ") },
                        singleLine = true,
                        isError = errorDes_short != null
                    )
                    if (errorDes_short != null) {
                        Text(errorDes_short!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Code mẫu",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập code mẫu") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Thời gian

                    Text(
                        "Thời gian học",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = if (duration == 0) "" else duration.toString(),
                        onValueChange = {
                            duration = it.toIntOrNull() ?: 0
                            errorDuration = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập thời gian học (phút)") },
                        singleLine = true,
                        isError = errorDuration != null,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    if (errorDuration != null) {
                        Text(errorDuration!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = {
                                var check = true
                                if (!isValidTitle(title)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if(!isValidTitle(content)) {
                                    errorContent = "Nội dung không hợp lệ (không rỗng, không ký tự đặc biệt, không dư khoảng trắng)"
                                    check = false
                                }
                                if(!isValidTitle(des_short)) {
                                    errorDes_short = "Nội dung ví dụ không hợp lệ (không rỗng, không ký tự đặc biệt, không dư khoảng trắng)"
                                    check = false
                                }
                                if ( duration.toInt() <= 0 || duration.toInt() >= 720 || !duration.toString().all { it.isDigit() }) {
                                    errorDuration = "Thời gian học không hợp lệ (phải là số nguyên dương và nhỏ hơn 720)"
                                    check = false
                                }
                                if(check) {
                                    viewModel.checkLessonExists(title, "update", selected?.id,selectedCourse?.id?:0) { exists ->
                                        if (exists) {
                                            errorMsg = "Tiêu đề bài học đã tồn tại"
                                            check = false
                                        }

                                        else
                                        {
                                            // tạo chuỗi json { } cho content_lesson
                                            val exampleObj = org.json.JSONObject()
                                            exampleObj.put("des_short", des_short)
                                            if(code.isNotEmpty()) {
                                                exampleObj.put("code", code)
                                            }
                                            // Create the main content_lesson object
                                            val contentLessonObj = org.json.JSONObject()
                                            contentLessonObj.put("content_lession", content)
                                            contentLessonObj.put("example", exampleObj)

                                            // Update the lesson
                                            val updatedLesson = lessons(
                                                selected!!.id,
                                                id_course = selectedCourse?.id ?: 0,
                                                title_lesson = title,
                                                content_lesson = contentLessonObj.toString(),
                                                duration = duration.toInt()
                                            )
                                            viewModel.updateLesson(updatedLesson)
                                            showUpdateDialog = false
                                        }
                                    }
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
                                selected?.let { viewModel.deleteLesson(it.id?:0) }
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
