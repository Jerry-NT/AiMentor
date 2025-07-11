package com.example.aisupabase.pages.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.LessonRepository
import com.example.aisupabase.controllers.LessonResult
import com.example.aisupabase.controllers.LearnResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.ui.theme.Purple100
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lessons
import org.json.JSONArray

class lessonDetailViewModel(private val repository: LessonRepository, private val learnRepository: LearnRepository): ViewModel() {
    private val _lessonsList = MutableStateFlow<List<lessons>>(emptyList())
    val lessonsList: StateFlow<List<lessons>> = _lessonsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted

    fun getLessonByID(id: Int, id_user: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getLessonsByID(id)) {
                is LessonResult.Success -> {
                    _lessonsList.value = result.data ?: emptyList()
                    checkLessonCompleted(id_user, id)
                }
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    private fun checkLessonCompleted(id_user: Int, id_lesson: Int) {
        viewModelScope.launch {
            val result = learnRepository.checkLessonCompleted(id_user, id_lesson)
            _isCompleted.value = result
        }
    }

    fun completeLesson(id_user: Int, id_lesson: Int) {
        viewModelScope.launch {
            when (val result = learnRepository.completeLesson(id_user, id_lesson)) {
                is LearnResult.Success -> _isCompleted.value = true
                is LearnResult.Error -> _isCompleted.value = false
            }
        }
    }
}

class lessonDetailViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(lessonDetailViewModel::class.java)) {
            return lessonDetailViewModel(LessonRepository(supabase), LearnRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Lesson_Detail(navController: NavController, id: Int) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val session = authUser().getUserSession(context)
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "client") {
            authUser().clearUserSession(context)
            navController.navigate("login")
        }
    }

    val supabase = SupabaseClientProvider.client
    LessonDetailView(id, navController, supabase)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LessonDetailView(
    id: Int,
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: lessonDetailViewModel = viewModel(factory = lessonDetailViewModelFactory(supabase))
) {
    val context = LocalContext.current
    val session = authUser().getUserSession(context)
    val id_user = session["id"] as Int
    LaunchedEffect(id, id_user) {
        viewModel.getLessonByID(id, id_user)
    }

    val ListLesson by viewModel.lessonsList.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (ListLesson.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            AsyncImage(
                                model = ListLesson[0].url_image,
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
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-20).dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                        ) {

                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x99957CDC), // Purple-900
                                                Color(0x99F3EFEF)// Indigo-500
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = ListLesson[0].title_lesson,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2D3748),
                                        lineHeight = 36.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 20.dp),
                                        color = Color.Gray.copy(alpha = 0.2f)
                                    )

                                    val contentArray = JSONArray(ListLesson[0].content_lesson)

                                    if (contentArray.length() != 0) {
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

                                        val practiceQuestions = ListLesson[0].practice_questions
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
                                        Spacer(modifier = Modifier.height(32.dp))
                                        if (!isCompleted) {
                                            Button(
                                                onClick = {
                                                    viewModel.completeLesson(id_user, id)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Purple100
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Hoàn thành",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = {

                                                    navController.navigate("client_quizzes/${ListLesson[0].title_lesson}")
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Purple100
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Ôn tập",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = {

                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Purple100
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Đã hoàn thành",
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
        }

}

