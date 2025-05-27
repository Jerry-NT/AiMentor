package com.example.aisupabase.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.controllers.TagRepository
import com.example.aisupabase.controllers.TagResult
import com.example.aisupabase.models.Tags
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Viewmodel sử dụng để quản lý trạng thái của danh sách tags -> CRUD operations
class TagViewModel(private val repository: TagRepository) : ViewModel() {
    private val _tagList = MutableStateFlow<List<Tags>>(emptyList())
    val tagList: StateFlow<List<Tags>> = _tagList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getTags()
    }

    fun getTags() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getTags()) {
                is TagResult.Success -> _tagList.value = result.data ?: emptyList()
                is TagResult.Error -> _error.value = "Failed to load tags: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun removeTag(tag: Tags) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteTag(tag.id.toString())) {
                is TagResult.Success -> getTags()
                is TagResult.Error -> _error.value = "Failed to delete tag: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateTag(id: String, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateTag(id, title)) {
                is TagResult.Success -> getTags()
                is TagResult.Error -> _error.value = "Failed to update tag: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun addTag(title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addTag(title)) {
                is TagResult.Success -> getTags()
                is TagResult.Error -> _error.value = "Failed to add tag: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }
}

// ViewModel Factory sử dụng để tạo TagViewModel với SupabaseClient
class TagViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagViewModel::class.java)) {
            return TagViewModel(TagRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//  Main Activity
@Composable
fun Admin_Tag_Blogs(navController: NavController) {
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
    TagManagementApp(supabase)
}

// Validation function for tag title
private fun isValidTagTitle(title: String): Boolean {
    val trimmed = title.trim()
    val regex = Regex("^[a-zA-Z0-9\\sÀ-ỹ]+$")
    return trimmed.isNotEmpty() && trimmed == title && regex.matches(title)
}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagementApp(
    supabase: SupabaseClient,
    viewModel: TagViewModel = viewModel(factory = TagViewModelFactory(supabase))
) {
    val tags by viewModel.tagList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf<Tags?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
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
                        Button(onClick = { viewModel.getTags() }) { Text("Retry") }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ID", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Tên sản phẩm", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                                Text("Thao tác", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                            Divider()
                        }
                        items(tags) { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tag.id.toString(), modifier = Modifier.weight(1f))
                                Text(tag.title_tag, modifier = Modifier.weight(2f))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedTag = tag
                                            showUpdateDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) { Text("Sửa", color = Color.White) }
                                    Button(
                                        onClick = {
                                            selectedTag = tag
                                            showDeleteDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) { Text("Xóa", color = Color.White) }
                                }
                            }
                            Divider()
                        }
                    }
                }
            }

            // Add Dialog
            if (showAddDialog) {
                Dialog(onDismissRequest = { showAddDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        var tagTitle by remember { mutableStateOf("") }
                        var errorMsg by remember { mutableStateOf<String?>(null) }

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
                                Text("Thêm tag", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { showAddDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tiêu đề", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = tagTitle,
                                onValueChange = {
                                    tagTitle = it
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
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!isValidTagTitle(tagTitle)) {
                                            errorMsg = "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                        } else {
                                            viewModel.addTag(tagTitle.trim())
                                            showAddDialog = false
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
            if (showUpdateDialog && selectedTag != null) {
                Dialog(onDismissRequest = { showUpdateDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        var tagTitle by remember { mutableStateOf(selectedTag!!.title_tag) }
                        var errorMsg by remember { mutableStateOf<String?>(null) }

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
                                Text("Cập nhập tag", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { showUpdateDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tiêu đề", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = tagTitle,
                                onValueChange = {
                                    tagTitle = it
                                    errorMsg = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = errorMsg != null
                            )
                            if (errorMsg != null) {
                                Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!isValidTagTitle(tagTitle)) {
                                            errorMsg = "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                        } else {
                                            viewModel.updateTag(selectedTag!!.id.toString(), tagTitle.trim())
                                            showUpdateDialog = false
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
            if (showDeleteDialog && selectedTag != null) {
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
                            Text("Xác nhận xóa", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                            Text("Bạn có thực sự muốn xóa ?", fontSize = 16.sp, modifier = Modifier.padding(bottom = 24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedTag?.let { viewModel.removeTag(it) }
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
    }
}
