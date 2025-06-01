package com.example.aisupabase.pages

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import blogs
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.BlogRepository
import com.example.aisupabase.controllers.BlogResult
import com.example.aisupabase.controllers.authUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aisupabase.R
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import coil.compose.AsyncImage
import com.example.aisupabase.controllers.TagRepository
import com.example.aisupabase.controllers.TagResult
import com.example.aisupabase.models.Tags

//ViewModel for managing the state of blogs
class BlogsViewModel(private val repository: BlogRepository, private val tag_repository: TagRepository) : ViewModel() {
    private val _blogsList = MutableStateFlow<List<blogs>>(emptyList())
    val blogsList: StateFlow<List<blogs>> = _blogsList
    // theem tags
    private val _tagList = MutableStateFlow<List<Tags>>(emptyList())
    val tagList: StateFlow<List<Tags>> = _tagList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getBlogs()
        getTags()
    }

    fun getTags() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = tag_repository.getTags()) {
                is TagResult.Success -> _tagList.value = result.data ?: emptyList()
                is TagResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
    fun getBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getBlogs()) {
                is BlogResult.Success -> _blogsList.value = result.data ?: emptyList()
                is BlogResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun deleteBlog(blog: blogs) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteBlog(blog.id.toString())) {
                is BlogResult.Success -> getBlogs()
                is BlogResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun updateBlog(blog: blogs, title: String, publicId: String, url: String, tagId: Int, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateBlog(blog.id.toString(), title, publicId, url, tagId, content)) {
                is BlogResult.Success -> getBlogs()
                is BlogResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun addBlog(title: String, publicId: String, url: String, tagId: Int, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addBlog(title, publicId, url, tagId, content)) {
                is BlogResult.Success -> getBlogs()
                is BlogResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
}


//  Main Activity for Admin Blogs Page
@Composable
fun Admin_Blogs( navController: NavController) {
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
    BlogManagementApp(supabase = supabase)
}

// ViewModel factory for BlogsViewModel
class BlogsViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlogsViewModel(BlogRepository(supabase),TagRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Main Composable function for Blog Management
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogManagementApp(
    supabase: SupabaseClient,
    viewModel: BlogsViewModel = viewModel(factory = BlogsViewModelFactory(supabase))
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val blogsList by viewModel.blogsList.collectAsState()


    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<blogs?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Blog") },
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
                            text = error ?: "Unknown error",
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.getBlogs() }) { Text("Retry") }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(blogsList) { index, blog ->
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
                                    // Số thứ tự
                                    Text(
                                        text = "Số thứ tự: ${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Tiêu đề blog
                                    Text(
                                        text = "Tiêu đề: ${blog.title_blog}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Hình ảnh blog (dùng Coil)
                                    AsyncImage(
                                        model = blog.url_image,
                                        contentDescription = "Ảnh blog",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    // the tag của blog ? => ?

                                    // Nội dung blog (rút gọn)
                                    Text(
                                        text = "Nội dung: ${blog.content_blog}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Ngày tạo
                                    blog.created_at?.let {
                                        Text(
                                            text = "Ngày tạo: $it",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    // Thao tác
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selected = blog
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                selected = blog
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

    // Add Dialog
    if (showAddDialog) {
        val tagsList by viewModel.tagList.collectAsState()
        var expanded by remember { mutableStateOf(false) }
        var selectedTag: Tags? by remember { mutableStateOf(null) }


        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                var title_blog by remember { mutableStateOf("") }
                var errorMsg by remember { mutableStateOf<String?>(null) }

                var content by remember { mutableStateOf("") }
                var errorcontentMsg by remember { mutableStateOf<String?>(null) }
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
                        Text("Thêm blog", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tiêu đề",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = title_blog,
                        onValueChange = {
                            title_blog = it
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
                            errorcontentMsg = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập nội dung") },
                        singleLine = true,
                        isError = errorcontentMsg != null
                    )
                    if (errorcontentMsg != null) {
                        Text(errorcontentMsg!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Chọn tag", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedTag?.title_tag ?: "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            enabled = false,
                            placeholder = { Text("Chọn tag") }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            tagsList.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag.title_tag) },
                                    onClick = {
                                        selectedTag = tag
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isValidTitle(title_blog)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                }
                                if(title_blog.length <150) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (ít nhất 150 ký tự)"
                                }
                                if (!isValidTitle(content)) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                }
                                else {
                                    val tagId = selectedTag?.id ?: 0
                                    viewModel.addBlog(title_blog, "1", "1", tagId, content)
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
}
private fun isValidTitle(title: String): Boolean {
    val trimmed = title.trim() // Loại bỏ khoảng trắng đầu và cuối
    val regex = Regex("^[a-zA-Z0-9\\sÀ-ỹ]+$") // Chỉ cho phép chữ cái, số, khoảng trắng và ký tự tiếng Việt
    return trimmed.isNotEmpty() && trimmed == title && regex.matches(title)
}
