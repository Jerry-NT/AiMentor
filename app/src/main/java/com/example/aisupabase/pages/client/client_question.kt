package com.example.aisupabase.pages.client

import QuestionResult
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.components.question.Option_ABCD
import com.example.aisupabase.components.question.Option_input
import com.example.aisupabase.config.FirebaseGeminiService
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.LearnResult
import com.example.aisupabase.controllers.LessonRepository
import com.example.aisupabase.controllers.LessonResult
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import com.example.aisupabase.controllers.authUser
import course_roadmaps
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lessons
import org.json.JSONObject
import questionRepositon
import question_option_type
import questions

//viewmodels
class ClientQuestionViewModel(
    private val repository: questionRepositon,
    private val course_repository: CourseRepository,
    private val roadmap_repository: RoadMapRepository,
    private val lessonRepository: LessonRepository,
    private val learnRepository: LearnRepository
) : ViewModel() {

    private val _questionlist = MutableStateFlow<List<questions>>(emptyList())
    val questionlist: StateFlow<List<questions>> = _questionlist

    private val _roadmapList = MutableStateFlow<List<course_roadmaps>>(emptyList())
    val roadmapList: StateFlow<List<course_roadmaps>> = _roadmapList

    private val _loading = MutableStateFlow(false)
    val isloading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getQuestions()
    }

    fun getQuestions() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.getQuestions()) {
                is QuestionResult.Success -> {
                    _questionlist.value = result.data ?: emptyList()
                }

                is QuestionResult.Error -> {
                    _error.value = result.exception.message
                }
            }
            _loading.value = false
        }
    }

    fun getRoadMapByTitle(title: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = roadmap_repository.getRoadMapTitle(title)) {
                is RoadMapResult.Success -> {
                    _roadmapList.value = result.data ?: emptyList()
                }

                is RoadMapResult.Error -> {
                    _error.value = result.exception.message
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

// viewmodel factory
class ClientQuestionViewModelFactory(private val supabase: SupabaseClient) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientQuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientQuestionViewModel(
                questionRepositon(supabase),
                CourseRepository(supabase),
                RoadMapRepository(supabase),
                LessonRepository(supabase),
                LearnRepository(supabase)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

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

    val supabase = SupabaseClientProvider.client
    Client_Onboarding(navController,supabase)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Client_Onboarding(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: ClientQuestionViewModel = viewModel(factory = ClientQuestionViewModelFactory(supabase))
) {
    val questionList by viewModel.questionlist.collectAsState()
    val roadmapList by viewModel.roadmapList.collectAsState()
    if (questionList.isEmpty()) {
        // Show loading or empty state UI
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // State quản lý
    var currentQuestionIndex by remember { mutableIntStateOf(0) } // vi tri cau hoi
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) } // Cho trắc nghiệm
    var textAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) } // Cho nhập liệu
    var selectedOption by remember { mutableIntStateOf(-1) }
    var textInput by remember { mutableStateOf("") }

    val currentQuestion = questionList[currentQuestionIndex] // cau hoi lay ra tu index
    val progress =
        (currentQuestionIndex + 1).toFloat() / questionList.size // cong thuc tinh qa trinh trl ca hoi

    // Focus requester cho text input
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Kiểm tra xem câu hỏi hiện tại đã được trả lời chưa
    val isCurrentQuestionAnswered = when (currentQuestion.type_option) {
        question_option_type.abcd -> selectedOption != -1
        question_option_type.input -> textInput.trim().isNotEmpty()
    }

    // Load câu trả lời khi chuyển câu hỏi
    LaunchedEffect(currentQuestionIndex) {
        when (currentQuestion.type_option) {
            question_option_type.abcd -> {
                selectedOption = selectedAnswers[currentQuestionIndex] ?: -1
            }

            question_option_type.input -> {
                textInput = textAnswers[currentQuestionIndex] ?: ""
            }
        }
    }

    // setup tạo course custom
    val geminiService = FirebaseGeminiService()
    var shouldCallGemini by remember { mutableStateOf(false) }
    var fullPrompt by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val session = authUser().getUserSession(context)

    LaunchedEffect(shouldCallGemini) {
        if (shouldCallGemini) {
            try {
                // Gọi dữ liệu roadmap
                viewModel.getRoadMapByTitle("Người dùng")
                // Gửi prompt đến Gemini
                val result = geminiService.generateText(fullPrompt)
                // Tách phần JSON từ kết quả
                val start = result.indexOf('{')
                val end = result.lastIndexOf('}')
                if (start != -1 && end != -1 && end > start) {
                    val jsonString = result.substring(start, end + 1)
                    val jsonResult = JSONObject(jsonString)
                    print(jsonResult)
                    // Lấy thông tin khoá học
                    val titleCourse = jsonResult.getString("title_course")
                    val desCourse = jsonResult.getString("des_course")

                    viewModel.addCourse(
                        title = titleCourse,
                        description = desCourse,
                        publicId = "",
                        urlImage = "",
                        isPrivate = true,
                        userCreate = session["id"] as Int,
                        id_roadmap = roadmapList[0].id as Int,
                        onSuccess = { courseId ->
                            viewModel.subCourse(session["id"] as Int,courseId)
                            // Tiếp tục xử lý lessons với courseId
                            val lessonsArray = jsonResult.getJSONArray("lessons")
                            for (i in 0 until lessonsArray.length()) {
                                val lesson = lessonsArray.getJSONObject(i)

                                val titleLessonOriginal = lesson.getString("title_lesson")
                                val titleLesson = "Bài ${i + 1}: $titleLessonOriginal"

                                val contentLesson = lesson.getString("content_lesson")
                                val duration = lesson.getString("duration")

                                val contentLessonObj = JSONObject()
                                contentLessonObj.put("content_lession", contentLesson)

                                viewModel.addLesson(
                                    lessons(
                                        null,
                                        id_course = courseId,
                                        title_lesson = titleLesson.trim(),
                                        content_lesson = contentLessonObj.toString(),
                                        duration = duration.toIntOrNull() ?: 0
                                    ),
                                    onSuccess = { lessonID ->
                                        Log.d("ID_LESSON","${lessonID}")
                                    }
                                )
                            }
                        }
                    )
                } else {
                    println("Không tìm thấy JSON hợp lệ trong kết quả của Gemini.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Đã xảy ra lỗi khi xử lý Gemini hoặc dữ liệu JSON: ${e.message}")
            } finally {
                isLoading = false
                shouldCallGemini = false
                navController.navigate("client_course_user")
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentQuestionIndex > 0) {
                            // Lưu câu trả lời hiện tại trước khi chuyển
                            when (currentQuestion.type_option) {
                                question_option_type.abcd -> {
                                    if (selectedOption != -1) {
                                        selectedAnswers[currentQuestionIndex] = selectedOption
                                    }
                                }

                                question_option_type.input -> {
                                    if (textInput.trim().isNotEmpty()) {
                                        textAnswers[currentQuestionIndex] = textInput.trim()
                                    }
                                }
                            }

                            currentQuestionIndex--
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4ECDC4),
                    trackColor = Color(0xFFE2E8F0),
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Question
                        Text(
                            text = currentQuestion.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // Hiển thị options hoặc text input tùy theo loại câu hỏi
                        when (currentQuestion.type_option) {
                            question_option_type.abcd -> {
                                // Multiple Choice Options
                                val options = try {
                                    JSONObject(currentQuestion.option)
                                } catch (e: Exception) {
                                    null
                                }
                                // Convert JSONObject to a list of pairs (key, value)
                                val optionList = options?.let { obj ->
                                    obj.keys().asSequence().map { key -> key to obj.optString(key) }
                                        .toList()
                                } ?: emptyList()

                                optionList.forEachIndexed { index, (key, value) ->
                                    Option_ABCD(
                                        text = "$key: $value",
                                        isSelected = selectedOption == index,
                                        onClick = {
                                            selectedOption = index
                                            selectedAnswers[currentQuestionIndex] = index
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    )
                                }
                            }

                            question_option_type.input -> {
                                // Text Input Field
                                Option_input(
                                    value = textInput,
                                    onValueChange = { newValue ->
                                        if (newValue.length <= 100) {
                                            textInput = newValue
                                            textAnswers[currentQuestionIndex] = newValue.trim()
                                        }
                                    },
                                    placeholder = "",
                                    keyboardType = KeyboardType.Text,
                                    maxLength = 100,
                                    focusRequester = focusRequester,
                                    onDone = {
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Next Button
                        Button(
                            onClick = {
                                // Lưu câu trả lời hiện tại

                                when (currentQuestion.type_option) {
                                    question_option_type.abcd -> {
                                        selectedAnswers[currentQuestionIndex] = selectedOption
                                        shouldCallGemini = false
                                    }
                                    question_option_type.input -> {
                                        textAnswers[currentQuestionIndex] = textInput.trim()
                                        shouldCallGemini = false
                                    }
                                }

                                if (currentQuestionIndex < questionList.size - 1) {
                                    currentQuestionIndex++
                                    selectedOption = -1
                                    textInput = ""
                                    shouldCallGemini = false
                                } else {
                                    val promptBuilder = StringBuilder()
                                    questionList.forEachIndexed { index, question ->
                                        val answer = when (question.type_option) {
                                            question_option_type.abcd -> {
                                                val selectedIdx = selectedAnswers[index]
                                                val options = JSONObject(question.option)
                                                val optionKey = options.keys().asSequence()
                                                    .elementAtOrNull(selectedIdx ?: -1)
                                                val optionValue =
                                                    optionKey?.let { options.optString(it) } ?: ""
                                                "$optionKey: $optionValue"
                                            }

                                            question_option_type.input -> {
                                                textAnswers[index] ?: ""
                                            }
                                        }
                                        promptBuilder.append("Question ${index + 1}: $answer\n")
                                    }
                                    val prompt = promptBuilder.toString()
                                    val instruction = """
Hãy tạo nội dung cho một khóa học và trả về kết quả dưới dạng một đối tượng JSON duy nhất. Đối tượng JSON này phải có cấu trúc như sau:
{
  "title_course": "string", 
  "des_course": "string", 
  "lessons": [
    {
      "title_lesson": "string",
      "duration":"số phút",
      "content_lesson": "string", // Mô tả ngắn gọn nội dung bài học
      "example": {
        "des_short": "string", // Mô tả ngắn gọn về ví dụ
        "code": "string"  // Đoạn code ví dụ, sử dụng \n cho các dòng mới nếu có nhiều dòng
      }
    }
  ]
}
// Đối với title_course chỉ từ 10-248 kí tự, content_course thì từ 10-498 kí tự
// Đối với các chủ đề không phải là lập trình thì trong mục example bỏ đi mục code
// Đối với title_lesson phải ít nhất 15-248 kí tự, content_lesson phải dao động ít nhất 250 kí tự trở lên tới < 1500 kí tự. des_short và code ~ 150 kí tự cho từng phần
""".trimIndent()

                                    fullPrompt = "$prompt\n$instruction"
                                    shouldCallGemini = true
                                    isLoading = true
                                }
                            },
                            enabled = isCurrentQuestionAnswered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4ECDC4),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            if (isLoading && currentQuestionIndex == questionList.size - 1) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.height(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (currentQuestionIndex < questionList.size - 1) "Tiếp tục" else "Hoàn thành",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
