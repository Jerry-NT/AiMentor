package com.example.aisupabase.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.aisupabase.config.handle.formatTransactionDate
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Users
import invoices
import invoicesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InvoiceViewModel(private val repository: invoicesRepository) : ViewModel() {
    private val _invoices = MutableStateFlow<List<invoices>>(emptyList())
    val invoicelist:StateFlow<List<invoices>> = _invoices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    // Khởi tạo dữ liệu hóa đơn
    init {
        getInvoices()
    }
    fun getInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getInvoice()) {
                is InvoiceResult.Success -> _invoices.value = result.data ?: emptyList()
                is InvoiceResult.Error -> _error.value = "Failed to load tags: ${result.exception.message}"
                else ->{}
            }
            _isLoading.value = false
        }
    }
}
class InvoiceViewModelFactory(private val supabase: SupabaseClient): ViewModelProvider.Factory{
    override fun <T: ViewModel>create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            return InvoiceViewModel(invoicesRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//  Main Activity
@Composable
fun Admin_User_Invoids( navController: NavController) {
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
    invoicesScreen(supabase)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun invoicesScreen(
    supabase: SupabaseClient,
    viewModel: InvoiceViewModel= viewModel(factory = InvoiceViewModelFactory(supabase))
){
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val invoices by viewModel.invoicelist.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
            )
        }
    ){ paddingValues ->
        Box (modifier = Modifier.padding(paddingValues)){
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
                        Button(onClick = { viewModel.getInvoices() }) { Text("Retry") }
                    }
                }
                else->{
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){

                        itemsIndexed (invoices) { index, invoice ->
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
                                        text = "STT: ${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    UsersText(supabase, invoice.id_user)
                                    // Tiêu đề
                                    Text(
                                        text = "Số lượng: ${invoice.amount}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "Tiền tệ: ${invoice.currency}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "Phương thức: ${invoice.payment_method}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "Trạng thái: ${invoice.status}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "Ngày giao dịch: ${formatTransactionDate(invoice.transaction_date)}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 16.dp)
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


@Composable
fun UsersText(supabase: SupabaseClient, id: Int) {
    // Lấy thông tin người dùng theo ID
    var users by remember { mutableStateOf<Users?>(null) }
    LaunchedEffect(id) {
        users= getuserbyid(supabase, id)
    }

    Text(
        text = "Họ và tên: ${users?.username ?: "Unknown"}",
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

}

suspend fun getuserbyid(supabase: SupabaseClient, id: Int): Users?{
    // Lấy thông tin người dùng theo ID
    val userlist=supabase.postgrest["users"]
        .select{
            filter { eq("id",id) }
        }
        .decodeList<Users>()
    return userlist.firstOrNull()
}