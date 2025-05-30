package com.example.aisupabase.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.TypeAccountRepository
import com.example.aisupabase.controllers.TypeAccountResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Tags
import com.example.aisupabase.models.type_accounts
import com.example.aisupabase.ui.theme.Blue
import com.example.aisupabase.ui.theme.Red
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// view model
class TypeAccountsViewModel (private val repository: TypeAccountRepository):ViewModel() {
    private val _typeAccountsList = MutableStateFlow<List<type_accounts>>(emptyList())
    val typeAccountsList: StateFlow<List<type_accounts>> = _typeAccountsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getTypeAccounts()
    }

    fun getTypeAccounts()
    {
        viewModelScope.launch {
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                when (val result = repository.getTypeAccounts()) {
                    is TypeAccountResult.Success -> _typeAccountsList.value = result.data ?: emptyList()
                    is TypeAccountResult.Error -> {
                        _error.value = result.exception.message
                    }
                }
                _isLoading.value = false
            }
        }
    }

    fun deleteTypeAccount(typeAccount: type_accounts) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.deleteTypeAccount(typeAccount.id.toString())) {
                is TypeAccountResult.Success -> getTypeAccounts() // Refresh the list after deletion
                is TypeAccountResult.Error -> {
                    _error.value = result.exception.message
                }
            }
            _isLoading.value = false
        }
    }

    fun updateTypeAccount(typeAccount: type_accounts, type: String, des: String, max_course: Int, price: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.updateTypeAccount(typeAccount.id.toString(), type, des, max_course, price)) {
                is TypeAccountResult.Success -> getTypeAccounts() // Refresh the list after update
                is TypeAccountResult.Error -> {
                    _error.value = result.exception.message
                }
            }
            _isLoading.value = false
        }
    }

    fun addTypeAccount(type: String, des: String, max_course: Int, price: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.addTypeAccount(type, des, max_course, price)) {
                is TypeAccountResult.Success -> getTypeAccounts() // Refresh the list after addition
                is TypeAccountResult.Error -> {
                    _error.value = result.exception.message
                }
            }
            _isLoading.value = false
        }
    }
}


// ViewModel Factory
class TypeAccountsViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TypeAccountsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TypeAccountsViewModel(TypeAccountRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//  Main Activity
@Composable
fun Admin_Type_Accounts( navController: NavController) {
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
    typeAccountManagementApp(supabase)
}

// crud view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun typeAccountManagementApp(
    supabase: SupabaseClient,
    viewModel: TypeAccountsViewModel = viewModel(factory = TypeAccountsViewModelFactory (supabase))
){
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val typeAccountsList by viewModel.typeAccountsList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<type_accounts?>(null) }

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
    ){paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )

            when{
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
                        Button(onClick = { viewModel.getTypeAccounts() }) { Text("Retry") }
                    }
                }

                else ->{
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        itemsIndexed(typeAccountsList) { index, TA ->
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
                                        text = "Loại tài khoản: ${TA.type}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Text(
                                        text = "Mô tả: ${TA.des}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Text(
                                        text = "Số khóa học tối đa: ${TA.max_course}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Text(
                                        text = "Giá: ${TA.price} VNĐ",
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
                                                selected = TA
                                                showUpdateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Sửa", color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                selected = TA
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
}