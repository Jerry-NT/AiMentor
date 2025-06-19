package com.example.aisupabase.pages.client

import UserRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aisupabase.components.card_components.LessonItem
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.LearnResult
import com.example.aisupabase.controllers.LessonRepository
import com.example.aisupabase.controllers.LessonResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Users
import courses
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lessons
import user_course

class CourseDetailViewModel(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val userRepository: UserRepository,
    private val learnRepository: LearnRepository): ViewModel()
{
    private val _coursesList = MutableStateFlow<List<courses>>(emptyList())
    val coursesList: StateFlow<List<courses>> = _coursesList

    private val _lessonsList = MutableStateFlow<List<lessons>>(emptyList())
    val lessonsList: StateFlow<List<lessons>> = _lessonsList

    private val _userList = MutableStateFlow<List<Users>>(emptyList())
    val userList: StateFlow<List<Users>> = _userList

    private val _learnList =  MutableStateFlow<List<user_course>>(emptyList())
    val learnList: StateFlow<List<user_course>> = _learnList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getCourseById(id:Int)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = courseRepository.getCourseByID(id)) {
                is CourseResult.Success -> _coursesList.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun getLessonById(id:Int)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = lessonRepository.getLessonsByIdCourse(id)) {
                is LessonResult.Success -> _lessonsList.value = result.data ?: emptyList()
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun getUserById(id:Int)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = userRepository.getUserByID(id)) {
                is UserResult.Success -> _userList.value = result.data ?: emptyList()
                is UserResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun subCourse(id_user:Int,id_course:Int)
    {
        viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        when (val result = learnRepository.SubCourse(id_user,id_course)) {
            is LearnResult.Success -> "Thanh cong"
            is LearnResult.Error -> _error.value = result.exception.message
        }
        _isLoading.value = false
    }}

    fun checkSub(id_user:Int,id_course:Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = learnRepository.checkSub(id_user,id_course)
            onResult(exists)
        }
    }

    // In CourseDetailViewModel
    fun continueLesson(id_user: Int, id_course: Int, onResult: (lessons?) -> Unit) {
        viewModelScope.launch {
            val nextLesson = learnRepository.continueLesson(id_user, id_course)
            onResult(nextLesson)
        }
    }

    }

class courseDetailViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            return CourseDetailViewModel(
                CourseRepository(supabase),
                LessonRepository(supabase),
                UserRepository(supabase),
                LearnRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Course_Detail(navController: NavController,id:Int)
{
    val context = LocalContext.current
    val session = authUser().getUserSession(context)
    LaunchedEffect(Unit) {
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "client") {
            authUser().clearUserSession(context)
            navController.navigate("login");
        }
    }
    val supabase = SupabaseClientProvider.client
    CourseDetailView(id, session["id"] as Int,navController,supabase)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseDetailView(
    id: Int,
    id_user:Int,
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: CourseDetailViewModel = viewModel(factory = courseDetailViewModelFactory(supabase))
) {
    LaunchedEffect(id) {
        viewModel.getCourseById(id)
        viewModel.getLessonById(id)
    }

    val listCourse by viewModel.coursesList.collectAsState()
    val listLesson by viewModel.lessonsList.collectAsState()
    val listUser by viewModel.userList.collectAsState()

    var textbutton by remember { mutableStateOf("Đăng ký học") }

    LaunchedEffect(id_user, id) {
        viewModel.checkSub(id_user, id) { exists ->
            if (exists) {
                viewModel.continueLesson(id_user, id) { nextLesson ->
                    textbutton = if (nextLesson == null) "Hoàn thành" else "Tiếp tục học"
                }
            } else {
                textbutton = "Đăng ký học"
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (listCourse.isNotEmpty()) {
            val user_create = listCourse[0].user_create
            LaunchedEffect(user_create) {
                viewModel.getUserById(user_create)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Hero Image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = listCourse[0].url_image,
                            contentDescription = "Ảnh khóa học",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        ),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                    }
                }

                // Content Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Course Title
                            Text(
                                text = listCourse[0].title_course,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748),
                                lineHeight = 32.sp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Instructor Info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    AsyncImage(
                                        model = listCourse[0].url_image,
                                        contentDescription = "Ảnh giảng viên",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Người tạo:",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    if(listUser.isNotEmpty())
                                    {
                                        Text(
                                            text = listUser[0].username,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2D3748)
                                        )
                                    }
                                }
                            }

                            // Course Description
                            Text(
                                text = "Mô tả khóa học",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D3748),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Text(
                                text = listCourse[0].des_course,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = Color(0xFF4A5568),
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )

                            // Lessons Section
                            if (listLesson.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Nội dung khóa học",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2D3748)
                                    )

                                    Text(
                                        text = "${listLesson.size} bài học",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }

                                // Lessons List
                                listLesson.forEachIndexed { index, lesson ->
                                    LessonItem(
                                        lesson = lesson,
                                        onClick = {
                                            navController.navigate("client_detail_lesson/${lesson.id}")
                                            // Navigate to lesson detail
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Start Button
                                if (textbutton == "Hoàn thành") {
                                    Text(
                                        text = "Đã hoàn thành",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4ECDC4),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                            .wrapContentHeight(Alignment.CenterVertically),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.checkSub(id_user, id) { exists ->
                                                if (exists) {
                                                    viewModel.continueLesson(id_user, id) { nextLesson ->
                                                        if (nextLesson != null) {
                                                            navController.navigate("client_detail_lesson/${nextLesson.id}")
                                                        }
                                                    }
                                                } else {
                                                    viewModel.subCourse(id_user, id)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4ECDC4)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = textbutton,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
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
