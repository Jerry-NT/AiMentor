package com.example.aisupabase.pages.client

import UserRepository
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aisupabase.components.card_components.PricingCard
import com.example.aisupabase.config.PaymentsUtil
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.TypeAccountRepository
import com.example.aisupabase.controllers.TypeAccountResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.UserRole
import com.example.aisupabase.models.Users
import com.example.aisupabase.models.type_accounts
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.PaymentData
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.wallet.PaymentsClient
import invoices
import invoicesRepository
import kotlinx.datetime.Clock.System.now
import kotlin.let

data class PricingPlan(
    val id: Int,
    val name: String,
    val price: Double,
    val period: String,
    val content: String,
    val max_count: Int,
    val gradientColors: List<Color>,
    val buttonColor: Color,
)

data class GooglePayUiState(
    val isLoading: Boolean = true,
    val isGooglePayAvailable: Boolean = false,
    val paymentSuccess: Boolean = false,
    val paymentData: String? = null,
    val errorMessage: String? = null
)

class ClientTypeAccountsViewModel (private val repository: TypeAccountRepository,
                                   private val invoiceR: invoicesRepository,
        private val userRepository: UserRepository):ViewModel() {
    private val _typeAccountsList = MutableStateFlow<List<type_accounts>>(emptyList())
    val typeAccountsList: StateFlow<List<type_accounts>> = _typeAccountsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _uiState = MutableStateFlow(GooglePayUiState())
    val uiState: StateFlow<GooglePayUiState> = _uiState.asStateFlow()

    private var paymentsClient: PaymentsClient? = null

    fun initializeGooglePay(context: android.content.Context) {
        paymentsClient = PaymentsUtil.createPaymentsClient(context)
        checkGooglePayAvailability()
    }

    private fun checkGooglePayAvailability() {
        val isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest()
        val request = com.google.android.gms.wallet.IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())

        paymentsClient?.isReadyToPay(request)?.addOnCompleteListener { task ->
            try {
                val result = task.getResult(ApiException::class.java)
                _uiState.value = _uiState.value.copy(
                    isGooglePayAvailable = result == true,
                    isLoading = false
                )
            } catch (exception: ApiException) {
                _uiState.value = _uiState.value.copy(
                    isGooglePayAvailable = false,
                    isLoading = false,
                    errorMessage = "Lỗi kiểm tra Google Pay: ${exception.message}"
                )
            }
        }
    }

    fun createPaymentDataRequest(amount: Long): com.google.android.gms.wallet.PaymentDataRequest {
        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(amount)
        return com.google.android.gms.wallet.PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
    }

    fun handlePaymentSuccess(paymentData: PaymentData, price: Double, id_type:Int, session: Map<String, Any>?, navController: NavController, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val paymentMethodData = paymentData.toJson()
                _uiState.value = _uiState.value.copy(
                    paymentSuccess = true,
                    paymentData = paymentMethodData,
                    errorMessage = null
                )
                viewModelScope.launch {
                    invoiceR.addInvoice(
                        invoices(
                            null,
                            id_user = (session?.get("id") ?: 0) as Int,
                            amount = price,// dữ liệu price ở đây là double nhưng csdl lưu là float8
                            status = "completed",
                            transaction_date  =  now().toString()
                        )
                    )
                    userRepository.updateUser(
                        Users(
                            id = (session?.get("id") ?: 0) as Int,
                            username = session?.get("username") as String? ?: "",
                            email = session?.get("email") as String? ?: "",
                            phone = session?.get("phone") as String? ?: "",
                            index_image =(session?.get("index_image") ?: 0) as Int,
                            id_type_account = id_type,
                            role = (session?.get("role") as? String)?.let { UserRole.valueOf(it) } ?: UserRole.client
                    ))

                    authUser().clearUserSession(context)
                    navController.navigate("login") {
                        popUpTo("client_home") { inclusive = true }
                    }
                }

            } catch (e: Exception) {
                Log.e("GooglePayTest", "Error handling payment success", e)
                _uiState.value = _uiState.value.copy(
                    paymentSuccess = false,
                    errorMessage = "Lỗi xử lý thanh toán: ${e.message}"
                )
            }
        }
    }

    fun handlePaymentError(errorMessage: String) {
        Log.e("GooglePayTest", "Payment Error: $errorMessage")
        _uiState.value = _uiState.value.copy(
            paymentSuccess = false,
            errorMessage = errorMessage
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    init {
        getTypeAccounts()
    }

    fun getTypeAccounts()
    {
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

class ClientTypeAccountsViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientTypeAccountsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientTypeAccountsViewModel(TypeAccountRepository(supabase),invoicesRepository(supabase),UserRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Client_Update_Account(navController: NavController) {
    val context = LocalContext.current
    val session = authUser().getUserSession(context)
    LaunchedEffect(Unit) {
        val role = session["role"] as? String
        val username = session["username"] as? String
        if (username == null || role != "client") {
            authUser().clearUserSession(context)
            navController.navigate("login");
        }
    }
    val type_account = session["type_account"] as? String
    val supabase = SupabaseClientProvider.client
    PricingPlansScreen(session as Map<String, Any>?,type_account,supabase,navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingPlansScreen(
    session: Map<String, Any>?,
    type_account: String?,
    supabase: SupabaseClient,
    navController: NavController,
    viewModel: ClientTypeAccountsViewModel = viewModel(factory = ClientTypeAccountsViewModelFactory (supabase)))
{
    val listColor = listOf(
        listOf(
            Color(0xFFFF6B6B), // Red
            Color(0xFFFF8E53)  // Orange
        ),
        listOf(
            Color(0xFF4ECDC4), // Teal
            Color(0xFF44A08D)  // Green
        ),
        listOf(
            Color(0xFFFF6B9D), // Pink
            Color(0xFFC44569)  // Dark Pink
        )
    )
    val listColorButton = listOf(
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFFFF6B9D)  // Pink
    )
    // type = name , price = price , content = des
    val typeAccountsList by viewModel.typeAccountsList.collectAsState()
    var price by remember { mutableDoubleStateOf(0.0) }
    var id_type by remember { mutableIntStateOf(0) }

    val pricingPlans = typeAccountsList
        .sortedBy { it.id }
        .mapIndexed { index, typeAccount ->
        PricingPlan(
            id = typeAccount.id ?: 0,
            name = typeAccount.type,
            price = typeAccount.price,
            period = "Vĩnh viễn",
            content = typeAccount.des,
            max_count = typeAccount.max_course,
            gradientColors = listColor.getOrElse(index) {
                listOf(Color.Gray, Color.DarkGray) // fallback nếu thiếu
            },
            buttonColor = listColorButton.getOrElse(index) {
                Color.Gray // fallback nếu thiếu
            }
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val googlePayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let { paymentData ->
                        viewModel.handlePaymentSuccess(paymentData,price,id_type,session,navController,context)
                    }
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                viewModel.handlePaymentError("Thanh toán bị hủy")
            }
            else -> {
                viewModel.handlePaymentError("Lỗi không xác định trong quá trình thanh toán")
            }
        }
    }

    // Khởi tạo Google Pay khi màn hình được tạo
    LaunchedEffect(Unit) {
        viewModel.initializeGooglePay(context)
    }
    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nâng Cấp Tài Khoản",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ){ paddingValues ->
        Box(
        modifier = Modifier.padding(paddingValues)
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center vertically
        ) {
            Text(
                text = "Bảng Giá Dịch Vụ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
                Text(
                    text = "Đang kiểm tra Google Pay...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Thông báo Google Pay không khả dụng
            if (!uiState.isGooglePayAvailable && !uiState.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Google Pay không khả dụng trên thiết bị này",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Đóng")
                        }
                    }
                }
            }

            if (uiState.paymentSuccess) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "✅ Thanh toán thành công!",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {

                items(pricingPlans) { plan ->
                    PricingCard(
                        type_account,
                        plan = plan,
                        allPlans = pricingPlans,
                        modifier = Modifier.width(280.dp),

                        onClick = {
                            price = plan.price
                            id_type=plan.id
                            if (uiState.isGooglePayAvailable && !uiState.isLoading) {
                                val paymentDataRequest = viewModel.createPaymentDataRequest(plan.price.toLong()*100) // 100.000 VND
                                val paymentsClient = PaymentsUtil.createPaymentsClient(context)
                                val task = paymentsClient.loadPaymentData(paymentDataRequest)

                                task.addOnCompleteListener { completedTask ->
                                    try {
                                        val paymentData = completedTask.getResult(ApiException::class.java)

                                        viewModel.handlePaymentSuccess(paymentData,plan.price,id_type,session,navController,context)
                                    } catch (exception: ApiException) {
                                        when (exception.statusCode) {
                                            CommonStatusCodes.RESOLUTION_REQUIRED -> {
                                                try {
                                                    // Sử dụng launcher để resolve
                                                    exception.status.resolution?.let { resolution ->
                                                        val intentSenderRequest = IntentSenderRequest.Builder(resolution).build()
                                                        googlePayLauncher.launch(intentSenderRequest)
                                                    }
                                                } catch (e: Exception) {
                                                    viewModel.handlePaymentError("Lỗi khởi động Google Pay: ${e.message}")
                                                }
                                            }
                                            CommonStatusCodes.DEVELOPER_ERROR -> {
                                                viewModel.handlePaymentError("Lỗi cấu hình Google Pay")
                                            }
                                            else -> {
                                                viewModel.handlePaymentError("Lỗi Google Pay: ${exception.message}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

            }
        }
    }}

}



