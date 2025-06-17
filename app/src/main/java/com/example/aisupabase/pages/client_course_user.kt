package com.example.aisupabase.pages

import androidx.compose.foundation.background
import com.example.aisupabase.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.aisupabase.components.bottombar.BottomNavigationBar
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.authUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.get
import androidx.compose.foundation.lazy.grid.items
import com.example.aisupabase.components.card_components.CourseCard
import com.example.aisupabase.components.card_components.PopularCourseItem
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.RoadMapRepository
import courses
import kotlin.collections.plus

class ClientCourseUserViewModel(
    private val courseRepository: CourseRepository
):ViewModel()
{
    private val _courseList = MutableStateFlow<List<courses>>(emptyList())
    val courseList: StateFlow<List<courses>> = _courseList

    private val _courseListbyUser = MutableStateFlow<List<courses>>(emptyList())
    val courseListbyUser: StateFlow<List<courses>> = _courseListbyUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchCoursesByUser(id_user:Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = courseRepository.getCourseByUserID(id_user)) {
                is CourseResult.Success -> _courseListbyUser.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }


    private val _processMap = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val processMap: StateFlow<Map<Int, Double>> = _processMap

    fun getProcess(id_user: Int, id_course: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = courseRepository.processCourse(id_user, id_course)) {
                else -> _processMap.value = _processMap.value + (id_course to result)
            }
            _isLoading.value = false
        }
    }
}

// view factory
class ClientCourseUserViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientCourseUserViewModel::class.java)) {
            return ClientCourseUserViewModel(CourseRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Client_Course_User(navController: NavController) {
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
    ClientCourseUserHomeView(navController,supabase)
}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCourseUserHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: ClientCourseUserViewModel = viewModel(factory =ClientCourseUserViewModelFactory(supabase))
)
{
    val ListcoursesByUser by viewModel.courseListbyUser.collectAsState()
    val processMap by viewModel.processMap.collectAsState()
    // thông tin user
    val context = LocalContext.current
    val session = authUser().getUserSession(context)
    val id_user = session["id"]
    // bottom bar setup
    val routeToIndex = mapOf(
        "client_home" to 0,
        "client_course" to 1,
        "client_search" to 2,
        "client_blog" to 3,
        "client_profile" to 4
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedIndex by remember { mutableStateOf(routeToIndex[currentRoute] ?: 0) }

    LaunchedEffect(currentRoute) {
        selectedIndex = routeToIndex[currentRoute] ?: 0
    }

    LaunchedEffect(id_user) {
        viewModel.fetchCoursesByUser(id_user as Int)
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = selectedIndex,
                onItemSelected = { index -> selectedIndex = index },
                navController
            )
        }
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x994C1D95),
                                Color(0x996366F1),
                                Color(0x9972658F),
                                Color(0x999595B7)
                            )
                        )
                    )
            ) {
                AsyncImage(
                    model = R.drawable.pic_1,
                    contentDescription = "Ảnh",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Danh sách khóa học",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 32.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }
                            items(ListcoursesByUser) { course ->
                                LaunchedEffect(course.id,id_user) {
                                    viewModel.getProcess(id_user as Int,course.id ?: 0)
                                }
                                val process = processMap[course.id ?: 0] ?: 0.0
                                CourseCard(course, process,navController)
                    }

                }
            }

        }
    }
}
