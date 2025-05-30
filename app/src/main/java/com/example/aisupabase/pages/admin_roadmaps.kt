package com.example.aisupabase.pages

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Tags
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import course_roadmaps
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// viewmodels
class RoadMapViewModel (private val repository: RoadMapRepository): ViewModel(){
    private val _roadmapList = MutableStateFlow<List<course_roadmaps>>(emptyList())
    val roadmapList: StateFlow<List<course_roadmaps>> = _roadmapList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init{
        getRoadMaps()
    }
    fun getRoadMaps() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getRoadMaps()) {
                is RoadMapResult.Success -> _roadmapList.value = result.data ?: emptyList()
                is RoadMapResult.Error -> _error.value = "Failed to load roadmaps: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun removeRoadMap(roadmap: course_roadmaps) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteRoadMap(roadmap.id.toString())) {
                is RoadMapResult.Success -> getRoadMaps()
                is RoadMapResult.Error -> _error.value = "Failed to delete roadmap: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateRoadMap(id: String, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateRoadMap(id, title)) {
                is RoadMapResult.Success -> getRoadMaps()
                is RoadMapResult.Error -> _error.value = "Failed to update roadmap: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }

    fun addRoadMap(title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addRoadMap(title)) {
                is RoadMapResult.Success -> getRoadMaps()
                is RoadMapResult.Error -> _error.value  = "Failed to add roadmap: ${result.exception.message}"
            }
            _isLoading.value = false
        }
    }
}

// viewnodel factory
class RoadMapViewModelFactory(private val repository: RoadMapRepository) :ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoadMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoadMapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//  Main Activity
@Composable
fun Admin_Roadmaps( navController: NavController) {
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
    RoadMapManagementApp(supabase)
}
// Validation function for  title
private fun isValidTitle(title: String): Boolean {
    val trimmed = title.trim()
    val regex = Regex("^[a-zA-Z0-9\\sÀ-ỹ]+$")
    return trimmed.isNotEmpty() && trimmed == title && regex.matches(title)
}

// CRUD  ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadMapManagementApp(
    supabase: SupabaseClient,
    viewModel: RoadMapViewModel = viewModel(factory = RoadMapViewModelFactory(RoadMapRepository(supabase)))
) {
    val roadmaps by viewModel.roadmapList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRoadMap by remember { mutableStateOf<course_roadmaps?>(null) }

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
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            when {
                // xử lý trạng thái loading
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
                        Button(onClick = { viewModel.getRoadMaps() }) { Text("Retry") }
                    }
                }
                // render danh sách
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(roadmaps) { index, roadmap ->
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

                                    // Tiêu đề
                                    Text(
                                        text = "Tiêu đề: ${roadmap.title}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    // Thao tác
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selectedRoadMap = roadmap
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                selectedRoadMap = roadmap
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
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                var roadmapTitle by remember { mutableStateOf("") }
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
                    Text(
                        "Tiêu đề",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = roadmapTitle,
                        onValueChange = {
                            roadmapTitle = it
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
                                if (!isValidTitle(roadmapTitle)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                } else {
                                    viewModel.addRoadMap(roadmapTitle.trim())
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
    if (showUpdateDialog && selectedRoadMap != null) {
        Dialog(onDismissRequest = { showUpdateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                var roadmapTitle by remember { mutableStateOf(selectedRoadMap!!.title) }
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
                    Text(
                        "Tiêu đề",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = roadmapTitle,
                        onValueChange = {
                            roadmapTitle = it
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
                                if (!isValidTitle(roadmapTitle)) {
                                    errorMsg =
                                        "Tiêu đề không hợp lệ (không rỗng, không dư khoảng trắng, không ký tự đặc biệt)"
                                } else {
                                    viewModel.updateRoadMap(
                                        selectedRoadMap!!.id.toString(),
                                        roadmapTitle.trim()
                                    )
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
    if (showDeleteDialog && selectedRoadMap != null) {
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
                                selectedRoadMap?.let { viewModel.removeRoadMap(it) }
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