package com.example.aisupabase.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
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
import com.example.aisupabase.config.handle.getPublicIdFromUrl
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import course_roadmaps
import courses
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.OptIn
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.emptyList
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.let
import com.example.aisupabase.config.handle.isValidTitle
import com.example.aisupabase.config.handle.uriToFile
import kotlinx.coroutines.coroutineScope
import java.io.File

// ViewModel quản lý state courses
class CoursesViewModel(private val repository: CourseRepository, private val roadmap_repository: RoadMapRepository) : ViewModel() {
    private val _coursesList = MutableStateFlow<List<courses>>(emptyList())
    val coursesList: StateFlow<List<courses>> = _coursesList
    //them roadmap
    private val _roadmapList = MutableStateFlow<List<course_roadmaps>>(emptyList())
    val roadmapList: StateFlow<List<course_roadmaps>> = _roadmapList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getCourses()
        getroadmap()
    }
    fun getroadmap() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = roadmap_repository.getRoadMaps()) {
                is RoadMapResult.Success -> _roadmapList.value = result.data ?: emptyList()
                is RoadMapResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
    fun getCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getCourses()) {
                is CourseResult.Success -> _coursesList.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun deleteCourse(course: courses) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteCourse(course.id ?: 0)) {
                is CourseResult.Success -> getCourses()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun updateCourse(id: Int, title: String, description: String, publicId: String, urlImage: String, userCreate: Int,id_roadmap:Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateCourse(id, title, description, publicId, urlImage, userCreate,id_roadmap)) {
                is CourseResult.Success -> getCourses()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun addCourse(
        title: String, description: String, publicId: String, urlImage: String, isPrivate: Boolean, userCreate: Int,id_roadmap: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (
                val result = repository.addCourse(title, description, publicId, urlImage, isPrivate ,userCreate,id_roadmap)
            ) {
                is CourseResult.Success -> getCourses()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
}

// ViewModel Factory cho CoursesViewModel
class CoursesViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoursesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoursesViewModel(CourseRepository(supabase),
                RoadMapRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Composable màn hình quản lý courses (kiểm tra role admin)
@Composable
fun Admin_Courses(navController: NavController) {
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
    CourseManagementApp(supabase = supabase)
}

// Composable giao diện quản lý courses
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseManagementApp(supabase: SupabaseClient, viewModel: CoursesViewModel = viewModel(factory = CoursesViewModelFactory(supabase))) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coursesList by viewModel.coursesList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<courses?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Khóa học") },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm khóa học", tint = Color.White)
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
                        Button(onClick = { viewModel.getCourses() }) { Text("Retry") }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(coursesList) { index, course ->
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
                                        text = "Tiêu đề: ${course.title_course}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    AsyncImage(
                                        model = course.url_image,
                                        contentDescription = "Ảnh khóa học",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Text(
                                        text = "Mô tả: ${course.des_course}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    UsersText(supabase, course.user_create)
                                    // lo trinh nao ?
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selected = course
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                selected = course
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

    //add dialog
    if (showAddDialog) {
        val roadmaplist by viewModel.roadmapList.collectAsState()
        var expanded by remember { mutableStateOf(false) }
        var selectedRoadmap: course_roadmaps? by remember { mutableStateOf(null) }
        val context = LocalContext.current
        val session = authUser().getUserSession(context)
        val id = session["id"] as? Int ?: 0
        var title_course by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        var description by remember { mutableStateOf("") }
        var errorcontentMsg by remember { mutableStateOf<String?>(null) }
        var errorrmMsg by remember { mutableStateOf<String?>(null) }

        // Thêm state cho ảnh
        var imageUri by remember { mutableStateOf<Uri?>(null) }

        var imageUrl by remember { mutableStateOf<String?>(null) }
        var imagePublicId by remember { mutableStateOf<String?>(null) }

        var isUploading by remember { mutableStateOf(false) }
        var uploadError by remember { mutableStateOf<String?>(null) }
        var imageFileToUpload by remember { mutableStateOf<File?>(null) }

        // Launcher để chọn ảnh từ thiết bị
        val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                uri: Uri? ->
            imageUri = uri
            uploadError = null
            if (uri != null) {
                val file = uriToFile(context, uri)
                if (file != null) {
                    imageFileToUpload = file // Đánh dấu file cần upload
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
                    ) {
                        Text("Thêm khóa học", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        value = title_course,
                        onValueChange = {
                            title_course = it
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
                        value = description,
                        onValueChange = {
                            description = it
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

                    Text("Chọn lộ trình: ", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedRoadmap?.title ?: "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            enabled = false,
                            placeholder = { Text("Chọn lộ trình:") }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roadmaplist.forEach { roadmap ->
                                DropdownMenuItem(
                                    text = { Text(roadmap.title) },
                                    onClick = {
                                        selectedRoadmap = roadmap
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (errorrmMsg != null) {
                        Text(errorrmMsg!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chọn ảnh & hiển thị ảnh đã chọn
                    Text("Ảnh blog", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !isUploading,
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Text(if (isUploading) "Đang tải..." else "Chọn ảnh", color = Color.White)
                        }
                        if (uploadError != null) {
                            Text(uploadError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
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


                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val coroutineScope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                var check = true
                                if (!isValidTitle(title_course)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }
                                if(title_course.length > 150) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (tối đa 150 ký tự)"
                                    check = false

                                }
                                if (!isValidTitle(description)) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }
                                if(description.length > 500) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (tối đa 500 ký tự)"
                                    check = false
                                }

                                if (selectedRoadmap == null) {
                                    errorrmMsg = "Vui lòng chọn lộ trình"
                                    check = false
                                }

                                if (imageUri == null) {
                                    uploadError = "Vui lòng chọn và upload ảnh!"
                                    check = false
                                }

                                if(check) {
                                    isUploading = true
                                    uploadError = null
                                    val roadmapID = selectedRoadmap?.id ?: 0
                                    coroutineScope.launch {
                                        val file = imageFileToUpload
                                        if (file != null) {
                                            val url = CloudinaryService.uploadImage(file)
                                            if (url != null) {
                                                imageUrl = url
                                                imagePublicId = getPublicIdFromUrl(url)
                                                viewModel.addCourse(title_course, description, imagePublicId ?: "", imageUrl ?: "",false, id, roadmapID)
                                                showAddDialog = false
                                                isUploading = false
                                            } else {
                                                uploadError = "Upload ảnh thất bại!"
                                                isUploading = false
                                            }
                                        }
                                        isUploading = false
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

    //Delete dialog
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
                                selected?.let { viewModel.deleteCourse(it) }
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

    //update dialog
    if (showUpdateDialog && selected != null) {
        val roadmaplist by viewModel.roadmapList.collectAsState()
        var expanded by remember { mutableStateOf(false) }
        var selectedRoadmap by remember {
            mutableStateOf(
                roadmaplist.find { it.id == selected?.id_roadmap }
            )
        }

        LaunchedEffect(roadmaplist, selected) {
            selectedRoadmap = roadmaplist.find { it.id == selected?.id_roadmap }
        }

        // Thêm state cho ảnh
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imageUrl by remember { mutableStateOf<String?>(null) }
        var imagePublicId by remember { mutableStateOf<String?>(null) }
        var isUploading by remember { mutableStateOf(false) }
        var uploadError by remember { mutableStateOf<String?>(null) }
        var imageFileToUpload by remember { mutableStateOf<File?>(null) }

        val context = LocalContext.current
        val session = authUser().getUserSession(context)
        val id = session["id"] as? Int ?: 0
        val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                uri: Uri? ->
            imageUri = uri
            uploadError = null
            if (uri != null) {
                val file = uriToFile(context, uri)
                if (file != null) {
                    imageFileToUpload = file // Đánh dấu file cần upload
                } else {
                    uploadError = "Không thể đọc file ảnh!"
                    isUploading = false
                }
            }
        }
        Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                var title_course by remember { mutableStateOf(selected!!.title_course) }
                var errorMsg by remember { mutableStateOf<String?>(null) }

                var description by remember { mutableStateOf(selected!!.des_course) }
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
                        Text("Sửa khóa học", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showUpdateDialog = false }) {
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
                        value = title_course,
                        onValueChange = {
                            title_course = it
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
                        value = description,
                        onValueChange = {
                            description = it
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

                    Text("Chọn lộ trình: ", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedRoadmap?.title ?: "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            enabled = false,
                            placeholder = { Text("Chọn lộ trình:") }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roadmaplist.forEach { roadmap ->
                                DropdownMenuItem(
                                    text = { Text(roadmap.title) },
                                    onClick = {
                                        selectedRoadmap = roadmap
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Ảnh blog", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !isUploading,
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Text(if (isUploading) "Đang tải..." else "Chọn ảnh", color = Color.White)
                        }
                        if (uploadError != null) {
                            Text(uploadError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
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

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                var check = true
                                if (!isValidTitle(title_course)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if(title_course.length > 150) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (tối đa 150 ký tự)"
                                    check = false

                                }

                                if (!isValidTitle(description)) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                    check = false
                                }

                                if(check) {
                                    isUploading = true
                                    uploadError = null
                                    val tagId = selectedRoadmap?.id ?: 0
                                    coroutineScope.launch {
                                        val file = imageFileToUpload
                                        if (file != null) {
                                            val url = CloudinaryService.uploadImage(file)
                                            if (url != null) {
                                                imageUrl = url
                                                imagePublicId = getPublicIdFromUrl(url)
                                                // xử lý xóa ảnh cũ
                                                if (selected?.url_image != null) {
                                                    val oldPublicId = getPublicIdFromUrl(selected!!.url_image)
                                                    CloudinaryService.deleteImage(oldPublicId)
                                                }
                                                viewModel.updateCourse(selected?.id ?:0 ,title_course,description, imagePublicId ?: "", imageUrl ?: "", id, tagId)
                                                showUpdateDialog = false
                                                isUploading = false
                                            } else {
                                                uploadError = "Upload ảnh thất bại!"
                                                isUploading = false
                                            }
                                        }
                                        isUploading = false
                                    }
                                }

                                if(description.length > 500) {
                                    errorcontentMsg =
                                        "Nội dung không hợp lệ (tối đa 500 ký tự)"
                                    check = false
                                }

                                if (check){
                                    val roadmapID = selectedRoadmap?.id ?: 0
                                    viewModel.updateCourse(selected?.id ?: 0,title_course, description,  imagePublicId ?: "", imageUrl ?: "", id,roadmapID)
                                    showUpdateDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) { Text("Cập nhật", color = Color.White) }
                        OutlinedButton(
                            onClick = { showUpdateDialog = false },
                            modifier = Modifier.weight(1f)
                        ) { Text("Hủy") }
                    }
                }
            }
        }
    }
}
