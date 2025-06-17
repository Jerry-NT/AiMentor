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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import questionRepositon
import question_option_type
import questions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aisupabase.R
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import com.example.aisupabase.config.handle.isValidTitle
import com.example.aisupabase.models.type_accounts

//viewmodels
class questionViewModel(private val repository: questionRepositon): ViewModel(){
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

    fun removeQuestion(question: questions) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when(val result = repository.deleteQuestion(question.id ?: 0)){
                is QuestionResult.Success -> {
                    getQuestions() // Refresh the list after deletion
                }
                is QuestionResult.Error -> {
                    _error.value = "Failed to delete question: ${result.exception.message}"
                }
            }
            _loading.value = false
        }
    }

    fun addQuestion(title: String, option: String, type_option: question_option_type) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when(val result = repository.addQuestion(title, option, type_option)){
                is QuestionResult.Success -> {
                    getQuestions() // Refresh the list after adding
                }
                is QuestionResult.Error -> {
                    _error.value = "Failed to add question: ${result.exception.message}"
                }
            }
            _loading.value = false
        }
    }

    fun updateQuestion(id:Int,title: String, option: String, type_option: question_option_type) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when(val result = repository.updateQuestion(id,title,option,type_option)){
                is QuestionResult.Success -> {
                    getQuestions() // Refresh the list after updating
                }
                is QuestionResult.Error -> {
                    _error.value = "Failed to update question: ${result.exception.message}"
                }
            }
            _loading.value = false
        }
    }

    fun checkQuestionExists(title: String, case: String = "add", id: Int? = null,type_option: question_option_type,onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = repository.checkQuestionExist(title, case, id,type_option)
            onResult(exists)
        }
    }

}

// viewmodel factory
class questionViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(questionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return questionViewModel(questionRepositon(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//  Main Activity
@Composable
fun Admin_Questions( navController: NavController) {
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
    question_app(supabase = supabase, navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun question_app(supabase: SupabaseClient, viewModel: questionViewModel = viewModel(factory = questionViewModelFactory(supabase)),
                 navController: NavController) {
    val questionList by viewModel.questionlist.collectAsState()
    val isloading by viewModel.isloading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<questions?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý question") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm câu hỏi", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm", color = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )

            when {
                isloading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        Button(onClick = { viewModel.getQuestions() }) { Text("Retry") }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(questionList) { index, question ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Số thứ tự: ${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Câu hỏi: ${question.title}",
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    if (question.type_option == question_option_type.input) {
                                        Text(
                                            text = "",
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    } else {
                                        val options = try {
                                            org.json.JSONObject(question.option)
                                        } catch (e: Exception) {
                                            null
                                        }
                                        if (options != null) {
                                            Column {
                                                options.keys().forEach { key ->
                                                    val value = options.optString(key)
                                                    Text(
                                                        text = "$key: $value",
                                                        fontSize = 15.sp,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selected = question
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                selected = question
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

    // Add Question Dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                var title by remember { mutableStateOf("") }
                var answerA by remember { mutableStateOf("") }
                var answerB by remember { mutableStateOf("") }
                var answerC by remember { mutableStateOf("") }
                var answerD by remember { mutableStateOf("") }
                var errorMsg by remember { mutableStateOf<String?>(null) }
                var errorAMsg by remember { mutableStateOf<String?>(null) }
                var errorBMsg by remember { mutableStateOf<String?>(null) }

                var expanded by remember { mutableStateOf(false) }
                var selectedTypeOption by remember { mutableStateOf(question_option_type.input) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thêm câu hỏi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    //thêm câu hỏi
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Câu hỏi") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMsg != null
                    )
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = when (selectedTypeOption) {
                            question_option_type.input -> "input"
                            question_option_type.abcd -> "abcd"
                            else -> selectedTypeOption.name
                        },
                        onValueChange = {},
                        label = { Text("Loại câu hỏi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Chọn loại"
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("input") },
                            onClick = {
                                selectedTypeOption = question_option_type.input
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("abcd") },
                            onClick = {
                                selectedTypeOption = question_option_type.abcd
                                expanded = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedTypeOption == question_option_type.abcd) {
                        OutlinedTextField(
                            value = answerA,
                            onValueChange = { answerA = it },
                            label = { Text("Nhập đáp án A") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorAMsg != null
                        )
                        if (errorAMsg != null) {
                            Text(errorAMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                        OutlinedTextField(
                            value = answerB,
                            onValueChange = { answerB = it },
                            label = { Text("Nhập đáp án B") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorBMsg != null
                        )
                        if (errorBMsg != null) {
                            Text(errorBMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                        OutlinedTextField(
                            value = answerC,
                            onValueChange = { answerC = it },
                            label = { Text("Nhập đáp án C") },
                            modifier = Modifier.fillMaxWidth(),

                        )

                        OutlinedTextField(
                            value = answerD,
                            onValueChange = { answerD = it },
                            label = { Text("Nhập đáp án D") },
                            modifier = Modifier.fillMaxWidth(),

                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                var check = true
                                if (!isValidTitle(title)) {
                                    errorMsg =
                                        "Loại tài khoản không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if (title.length > 150) {
                                    errorMsg = "Loại tài khoản không được quá 150 ký tự"
                                    check = false
                                }

                                if (!isValidTitle(answerA) && selectedTypeOption == question_option_type.abcd ) {
                                    errorAMsg =
                                        "Câu hỏi không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if (answerA.length > 150 && selectedTypeOption == question_option_type.abcd) {
                                    errorAMsg = "Câu hỏi không được quá 150 ký tự"
                                    check = false
                                }

                                if (!isValidTitle(answerB) && selectedTypeOption == question_option_type.abcd) {
                                    errorBMsg =
                                        "Câu hỏi không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if (answerB.length > 150 && selectedTypeOption == question_option_type.abcd) {
                                    errorBMsg = "Câu hỏi không được quá 150 ký tự"
                                    check = false
                                }


                                if (check) {
                                    viewModel.checkQuestionExists(title,"add",null,type_option = selectedTypeOption) { exists ->
                                        if (exists) {
                                            errorMsg = "Câu hỏi đã tồn tại"
                                            check = false
                                        } else {
                                            if (selectedTypeOption == question_option_type.abcd) {
                                                val options = org.json.JSONObject()
                                                options.put("A", answerA)
                                                options.put("B", answerB)
                                                options.put("C", answerC)
                                                options.put("D", answerD)
                                                viewModel.addQuestion(
                                                    title,
                                                    options.toString(),
                                                    selectedTypeOption
                                                )
                                            }
                                            else {
                                                viewModel.addQuestion(
                                                    title,
                                                    "",
                                                    selectedTypeOption
                                                )
                                            }
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

    if(showUpdateDialog && selected != null)
    {
        Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                var title by remember { mutableStateOf(selected!!.title) }

                var answerA by remember { mutableStateOf("") }
                var answerB by remember { mutableStateOf("") }
                var answerC by remember { mutableStateOf("") }
                var answerD by remember { mutableStateOf("") }

                LaunchedEffect(selected) {
                    if (selected?.type_option == question_option_type.abcd) {
                        val options = try {
                            org.json.JSONObject(selected?.option ?: "")
                        } catch (e: Exception) {
                            null
                        }
                        if (options != null) {
                            answerA = options.optString("A", "")
                            answerB = options.optString("B", "")
                            answerC = options.optString("C", "")
                            answerD = options.optString("D", "")
                        }
                    }
                }

                var errorMsg by remember { mutableStateOf<String?>(null) }
                var errorAMsg by remember { mutableStateOf<String?>(null) }
                var errorBMsg by remember { mutableStateOf<String?>(null) }


                var expanded by remember { mutableStateOf(false) }
                var selectedTypeOption by remember { mutableStateOf(selected!!.type_option) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cập nhập câu hỏi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showUpdateDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    //thêm câu hỏi
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Câu hỏi") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMsg != null
                    )
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = when (selectedTypeOption) {
                            question_option_type.input -> "input"
                            question_option_type.abcd -> "abcd"
                            else -> selectedTypeOption.name
                        },
                        onValueChange = {},
                        label = { Text("Loại câu hỏi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Chọn loại"
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("input") },
                            onClick = {
                                selectedTypeOption = question_option_type.input
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("abcd") },
                            onClick = {
                                selectedTypeOption = question_option_type.abcd
                                expanded = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedTypeOption == question_option_type.abcd) {
                        OutlinedTextField(
                            value = answerA,
                            onValueChange = { answerA = it },
                            label = { Text("Nhập đáp án A") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorAMsg != null
                        )
                        if (errorAMsg != null) {
                            Text(errorAMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                        OutlinedTextField(
                            value = answerB,
                            onValueChange = { answerB = it },
                            label = { Text("Nhập đáp án B") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorBMsg != null
                        )
                        if (errorBMsg != null) {
                            Text(errorBMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                        OutlinedTextField(
                            value = answerC,
                            onValueChange = { answerC = it },
                            label = { Text("Nhập đáp án C") },
                            modifier = Modifier.fillMaxWidth(),

                        )

                        OutlinedTextField(
                            value = answerD,
                            onValueChange = { answerD = it },
                            label = { Text("Nhập đáp án D") },
                            modifier = Modifier.fillMaxWidth(),

                        )

                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                var hasError = true
                                if (!isValidTitle(title)) {
                                    errorMsg =
                                        "Loại tài khoản không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    hasError = false
                                }

                                if (title.length > 150) {
                                    errorMsg = "Loại tài khoản không được quá 150 ký tự"
                                    hasError = false
                                }

                                if (!isValidTitle(answerA) && selectedTypeOption == question_option_type.abcd) {
                                    errorAMsg =
                                        "Câu hỏi không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    hasError = false
                                }
                                if (answerA.length > 150 && selectedTypeOption == question_option_type.abcd) {
                                    errorAMsg = "Câu hỏi không được quá 150 ký tự"
                                    hasError = false
                                }

                                if (!isValidTitle(answerB) && selectedTypeOption == question_option_type.abcd) {
                                    errorBMsg =
                                        "Câu hỏi không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    hasError = false
                                }
                                if (answerB.length > 150 && selectedTypeOption == question_option_type.abcd) {
                                    errorBMsg = "Câu hỏi không được quá 150 ký tự"
                                    hasError = false
                                }

                                if (hasError) {
                                    viewModel.checkQuestionExists(title,"update", selected!!.id,type_option = selectedTypeOption) { exists ->
                                        if (exists) {
                                            errorMsg = "Câu hỏi đã tồn tại"
                                            hasError = false
                                        } else {
                                            if (selectedTypeOption == question_option_type.abcd) {
                                                val options = org.json.JSONObject()
                                                options.put("A", answerA)
                                                options.put("B", answerB)
                                                options.put("C", answerC)
                                                options.put("D", answerD)
                                                viewModel.updateQuestion(
                                                    selected!!.id ?: 0,
                                                    title,
                                                    options.toString(),
                                                    selectedTypeOption
                                                )
                                            } else {
                                                viewModel.updateQuestion(
                                                    selected!!.id ?: 0,
                                                    title,
                                                    "",
                                                    selectedTypeOption
                                                )
                                            }
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
                                selected?.let { viewModel.removeQuestion(it) }
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