package com.example.aisupabase.pages.client

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
import com.example.aisupabase.components.card_components.PopularCourseItem
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import course_roadmaps
import courses

class ClientCourseByRMViewModel(
    private val courseRepository: CourseRepository,
    private val roadMapRepository: RoadMapRepository,
    private val learnRepository: LearnRepository
):ViewModel()
{
    private val _courseList = MutableStateFlow<List<courses>>(emptyList())
    val courseList: StateFlow<List<courses>> = _courseList

    private val _roadMapList = MutableStateFlow<List<course_roadmaps>>(emptyList())
    val roadMapList: StateFlow<List<course_roadmaps>> = _roadMapList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun loadCourseCountForRoadmap(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = courseRepository.getCourseByIDRoadMap(id)) {
                is CourseResult.Success -> _courseList.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
    fun getRoadMapByID(id:Int)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = roadMapRepository.getRoadMapByID(id)) {
                is RoadMapResult.Success -> _roadMapList.value = result.data ?: emptyList()
                is RoadMapResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }


    private val _subCount = MutableStateFlow(0)
    val subCount: StateFlow<Int> = _subCount

    fun getCountSub(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = learnRepository.getCountSub(id)) {
                else -> _subCount.value = result ?: 0
            }
            _isLoading.value = false
        }
    }
}
// view factory
class ClientCourseByRMViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientCourseByRMViewModel::class.java)) {
            return ClientCourseByRMViewModel(CourseRepository(supabase), RoadMapRepository(supabase),
                LearnRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun ClientCourseByRM(navController: NavController,id:Int) {
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
    ClientCourseByRMHomeView(id,navController,supabase)
}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientCourseByRMHomeView(
    id:Int,
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: ClientCourseByRMViewModel = viewModel(factory =ClientCourseByRMViewModelFactory(supabase))
)
{
    val Listcourses by viewModel.courseList.collectAsState()
    val Listroadmap by viewModel.roadMapList.collectAsState()
    val subCount by viewModel.subCount.collectAsState()
    // thông tin user
    val context = LocalContext.current
    val session = authUser().getUserSession(context)
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

    LaunchedEffect(id) {
        viewModel.getRoadMapByID(id)
        viewModel.loadCourseCountForRoadmap(id)
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

            ) {
                AsyncImage(
                    model = R.drawable.bg_6,
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
                    if(Listroadmap.isNotEmpty())
                    {
                            items(Listcourses) { course ->
                                LaunchedEffect(course.id) {
                                    viewModel.getCountSub(course.id ?: 0)
                                }
                                PopularCourseItem(course, navController,subCount)
                            }
                    }
                }
            }
        }
    }
}
