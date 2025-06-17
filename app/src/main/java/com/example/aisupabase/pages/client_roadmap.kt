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
import com.example.aisupabase.components.card_components.roadmapItem
import com.example.aisupabase.components.card_components.tagItem
import com.example.aisupabase.controllers.BlogRepository
import com.example.aisupabase.controllers.BlogResult
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import com.example.aisupabase.controllers.TagRepository
import com.example.aisupabase.controllers.TagResult
import com.example.aisupabase.models.Tags
import course_roadmaps
import courses
import kotlin.collections.plus

class ClientRMViewModel(
    private val courseRepository: CourseRepository,
    private val roadMapRepository: RoadMapRepository,
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

    private val _roadmapCourseCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val roadmapCourseCounts: StateFlow<Map<Int, Int>> = _roadmapCourseCounts

    init {
        fetchRoadMap()
    }

    private fun fetchRoadMap() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = roadMapRepository.getRoadMaps()) {
                is RoadMapResult.Success -> _roadMapList.value = result.data ?: emptyList()
                is RoadMapResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun loadCourseCountForRoadmap(roadmapId: Int) {
        viewModelScope.launch {
            when (val result = courseRepository.getCourseByIDRoadMap(roadmapId)) {
                is CourseResult.Success -> {
                    val count = result.data?.size ?: 0
                    _roadmapCourseCounts.value = _roadmapCourseCounts.value + (roadmapId to count)
                }
                else -> { /* handle error if needed */ }
            }
        }
    }
}

// view factory
class ClientRMViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientRMViewModel::class.java)) {
            return ClientRMViewModel(CourseRepository(supabase), RoadMapRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Client_RM(navController: NavController) {
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
    ClientRMHomeView(navController,supabase)

}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientRMHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: ClientRMViewModel = viewModel(factory = ClientRMViewModelFactory(supabase))
)
{
    val ListRoadMap by viewModel.roadMapList.collectAsState()

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

    val roadmapCourseCounts by viewModel.roadmapCourseCounts.collectAsState()
    LaunchedEffect(currentRoute) {
        selectedIndex = routeToIndex[currentRoute] ?: 0
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
                            text = "Danh sách lộ trình",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 32.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }

                    items(ListRoadMap) { roadmap ->
                        if(roadmap.title !== "Người dùng")
                        {
                        val count = roadmapCourseCounts[roadmap.id ?: 0] ?: 0
                        LaunchedEffect(roadmap.id) {
                            viewModel.loadCourseCountForRoadmap(roadmap.id ?: 0)
                        }
                        roadmapItem(roadmap, count,navController)
                        }

                    }
                }
            }

        }
    }
}
