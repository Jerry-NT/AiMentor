package com.example.aisupabase.pages

import QuestionResult
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
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import questionRepositon
import question_option_type
import questions

//viewmodels
class ClientQuestionViewModel(private val repository: questionRepositon): ViewModel(){
    private val _questionlist = MutableStateFlow<List<questions>>(emptyList())
    val questionlist: StateFlow<List<questions>> = _questionlist

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
            _error.value= null
            when(val result = repository.getQuestions()){
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



}

// viewmodel factory
class QuestionViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientQuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientQuestionViewModel(questionRepositon(supabase)) as T
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
    Client_Onboarding(supabase)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Client_Onboarding(
    supabase: SupabaseClient,
    viewModel: ClientQuestionViewModel = viewModel(factory = QuestionViewModelFactory(supabase))
) {
    val questionList by viewModel.questionlist.collectAsState()

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
    val progress = (currentQuestionIndex + 1).toFloat() / questionList.size // cong thuc tinh qa trinh trl ca hoi

    // Focus requester cho text input
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Kiểm tra xem câu hỏi hiện tại đã được trả lời chưa
    val isCurrentQuestionAnswered = when (currentQuestion.type_option) {
        question_option_type.abcd  -> selectedOption != -1
        question_option_type.input   -> textInput.trim().isNotEmpty()
    }

    // Load câu trả lời khi chuyển câu hỏi
    LaunchedEffect(currentQuestionIndex) {
        when (currentQuestion.type_option) {
            question_option_type.abcd  -> {
                selectedOption = selectedAnswers[currentQuestionIndex] ?: -1
            }
            question_option_type.input  -> {
                textInput = textAnswers[currentQuestionIndex] ?: ""
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
                                    org.json.JSONObject(currentQuestion.option)
                                } catch (e: Exception) {
                                    null
                                }
                                // Convert JSONObject to a list of pairs (key, value)
                                val optionList = options?.let { obj ->
                                    obj.keys().asSequence().map { key -> key to obj.optString(key) }.toList()
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
                                    }
                                    question_option_type.input -> {
                                        textAnswers[currentQuestionIndex] = textInput.trim()
                                    }
                                }

                                if (currentQuestionIndex < questionList.size - 1) {
                                    currentQuestionIndex++
                                    selectedOption = -1
                                    textInput = ""
                                } else {
                                    // Hoàn thành onboarding
                                    // Có thể xử lý dữ liệu ở đây
                                    println("Selected Answers: $selectedAnswers")
                                    println("Text Answers: $textAnswers")
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
