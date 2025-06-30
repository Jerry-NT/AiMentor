package com.example.aisupabase.pages.client

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.example.aisupabase.config.GeminiService
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.ChatMessage
import com.example.aisupabase.ui.theme.ChatTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.items
import android.util.Log
import androidx.compose.ui.text.font.FontStyle
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.LearnResult
import com.example.aisupabase.controllers.LessonRepository
import com.example.aisupabase.controllers.LessonResult
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import course_roadmaps
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lessons
import org.json.JSONObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONArray

class ClientQuestionViewModel(
    private val course_repository: CourseRepository,
    private val roadmap_repository: RoadMapRepository,
    private val lessonRepository: LessonRepository,
    private val learnRepository: LearnRepository) : ViewModel() {

    private val _roadmapList = MutableStateFlow<List<course_roadmaps>>(emptyList())
    val roadmapList: StateFlow<List<course_roadmaps>> = _roadmapList

    private val _loading = MutableStateFlow(false)
    val isloading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun getRoadMapByTitle(title: String, onResult: (Int?) -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = roadmap_repository.getRoadMapTitle(title)) {
                is RoadMapResult.Success -> {
                    _roadmapList.value = result.data ?: emptyList()
                    val id = result.data?.firstOrNull()?.id as? Int
                    onResult(id)
                    Log.d("RoadMap", "Data: ${_roadmapList.value}")
                }
                is RoadMapResult.Error -> {
                    _error.value = result.exception.message
                    onResult(null)
                    Log.d("RoadMap", "Error: ${result.exception.message}")
                }
            }
        }
    }

    fun addCourse(
        title: String, description: String, publicId: String, urlImage: String, isPrivate: Boolean, userCreate: Int, id_roadmap: Int, onSuccess: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (
                val result = course_repository.addCourse(title, description, publicId, urlImage, isPrivate, userCreate, id_roadmap
                )
            ) {
                is CourseResult.Success -> {
                    val newCourse = result.data
                    // Gọi callback với ID của course vừa tạo
                    newCourse?.id?.let { courseId ->
                        onSuccess(courseId)
                    }
                }
                is CourseResult.Error ->{
                    Log.d("AddCourse", " Data: ${result.exception.message}")
                    _error.value = result.exception.message}
            }
            _loading.value = false
        }
    }

    fun addLesson(lesson: lessons,onSuccess: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = lessonRepository.addLesson(lesson.id_course, lesson.title_lesson, lesson.content_lesson, lesson.duration)) {
                is LessonResult.Success -> {

                    val newLesson = result.data
                    // Gọi callback với ID của course vừa tạo
                    newLesson?.id?.let { id ->
                        onSuccess(id)
                    }
                }
                is LessonResult.Error -> _error.value = result.exception.message
            }
            _loading.value = false
        }
    }

    fun subCourse(id_user:Int,id_course:Int)
    {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = learnRepository.SubCourse(id_user,id_course)) {
                is LearnResult.Success -> "Thanh cong"
                is LearnResult.Error -> _error.value = result.exception.message
            }
            _loading.value = false
        }}
}
class ClientQuestionViewModelFactory(private val supabase: SupabaseClient) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientQuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientQuestionViewModel(
                CourseRepository(supabase),
                RoadMapRepository(supabase),
                LessonRepository(supabase),
                LearnRepository(supabase)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun Client_Question(navController: NavController) {
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
    val geminiService = GeminiService()
    val supabase = SupabaseClientProvider.client
    ChatTheme {
        ChatScreen(
            navController,
            supabase,
            geminiService = geminiService,
            onBackPressed = {
                // quay lai
                navController.navigate("client_home") {
                    popUpTo("client_home") { inclusive = true }
                }}
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: ClientQuestionViewModel = viewModel(factory = ClientQuestionViewModelFactory(supabase)),
    geminiService: GeminiService,
    onBackPressed: () -> Unit,
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val roadmapList by viewModel.roadmapList.collectAsState()
    val session = authUser().getUserSession(context)
    // Welcome message
    LaunchedEffect(Unit) {
        val welcomeMessage = ChatMessage(
            message = "Chào bạn! Tôi sẽ giúp bạn tạo khóa học. Hãy cho tôi biết bạn muốn tạo khóa học về chủ đề gì?",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        messages = listOf(welcomeMessage)
    }

    // Auto scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var fullPrompt by remember { mutableStateOf("") }
    var shouldCallGemini by remember { mutableStateOf(false) }

    LaunchedEffect(shouldCallGemini) {
        if (shouldCallGemini) {
            try {
                viewModel.getRoadMapByTitle("Người dùng")
                val jsonResult = JSONObject(fullPrompt)
                val titleCourse = jsonResult.getString("title_course")
                val desCourse = jsonResult.getString("des_course")

                viewModel.getRoadMapByTitle("Người dùng") { roadmapId ->
                    if (roadmapId != null) {
                        viewModel.addCourse(
                            title = titleCourse,
                            description = desCourse,
                            publicId = "cong-nghe-ai-moi-nhat_wjkuw3",
                            urlImage = "https://res.cloudinary.com/dwgzc6k8i/image/upload/v1751196195/cong-nghe-ai-moi-nhat_wjkuw3.webp",
                            isPrivate = true,
                            userCreate = session["id"] as Int,
                            id_roadmap = roadmapId,
                            onSuccess = { courseId ->
                                viewModel.subCourse(session["id"] as Int, courseId)

                                val lessonsArray = jsonResult.getJSONArray("lessons")

                                // Hàm đệ quy để thêm lessons tuần tự
                                fun addLessonSequentially(index: Int) {
                                    if (index >= lessonsArray.length()) {
                                        navController.navigate("client_course_user")
                                        return
                                    }

                                    val lesson = lessonsArray.getJSONObject(index)
                                    val titleLessonOriginal = lesson.getString("title_lesson")
                                    val contentLesson = lesson.getString("content_lesson")
                                    val duration = lesson.getString("duration")
                                    val contentLessonObj = JSONObject()
                                    contentLessonObj.put("content_lession", contentLesson)

                                    viewModel.addLesson(
                                        lessons(
                                            null,
                                            id_course = courseId,
                                            title_lesson = titleLessonOriginal.trim(),
                                            content_lesson = contentLessonObj.toString(),
                                            duration = duration.toIntOrNull() ?: 0
                                        ),
                                        onSuccess = {
                                            // Thêm lesson tiếp theo sau khi lesson hiện tại thành công
                                            addLessonSequentially(index + 1)
                                        }
                                    )
                                }

                                // Bắt đầu thêm từ lesson đầu tiên
                                addLessonSequentially(0)
                            }
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("Đã xảy ra lỗi khi xử lý Gemini hoặc dữ liệu JSON: ${e.message}")
            } finally {
                shouldCallGemini = false
                fullPrompt = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Tạo khóa học cùng Gemini",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }

            // Typing indicator
            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Input Section
        ChatInputSection(
            messageText = messageText,
            onMessageChange = { messageText = it },
            // Thay thế phần onSendMessage trong ChatInputSection

            onSendMessage = {
                if (messageText.isNotBlank() && !isLoading) {
                    val userMessage = ChatMessage(
                        message = messageText.trim(),
                        isUser = true,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + userMessage

                    val prompt = messageText.trim()
                    messageText = ""
                    isLoading = true

                    // Call Gemini API with chat history
                    (context as ComponentActivity).lifecycleScope.launch {
                        try {
                            // Truyền lịch sử chat vào generateText (loại bỏ welcome message)
                            val chatHistory = messages.filter { it.message != "Chào bạn! Tôi sẽ giúp bạn tạo khóa học. Hãy cho tôi biết bạn muốn tạo khóa học về chủ đề gì?" }

                            val response = geminiService.generateText(prompt, chatHistory)
                            val botMessage = ChatMessage(
                                message = response,
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                            messages = messages + botMessage

                            // Log JSON data cho developer (chỉ trong debug mode)
                            geminiService.getFinalExport()?.let { jsonData ->
                                fullPrompt = jsonData
                                shouldCallGemini = true
                            }

                        } catch (e: Exception) {
                            val errorMessage = ChatMessage(
                                message = "Xin lỗi, đã xảy ra lỗi, vui lòng thử lại.",
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                            messages = messages + errorMessage
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            isLoading = isLoading
        )
    }
}


@Composable
fun ChatMessageItem(message: ChatMessage) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (message.isUser) {
            // User message
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Card(
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = message.message,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                )
            }
        } else {
            // Bot message
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Card(
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 20.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gemini",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = message.message,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 4.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun ChatInputSection(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 120.dp),
                placeholder = { Text("Nhập nội dung tin nhắn...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Tạo khóa học"
                    )
                }
            }
        }
    }
}