package com.example.aisupabase.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import blogs
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.BlogRepository
import com.example.aisupabase.controllers.BlogResult
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.TagRepository
import com.example.aisupabase.controllers.TagResult
import com.example.aisupabase.controllers.authUser
import com.example.aisupabase.models.Tags
import courses
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.aisupabase.components.bottombar.BottomNavigationBar
import com.example.aisupabase.R
import com.example.aisupabase.components.card_components.BlogPostItem
import com.example.aisupabase.components.card_components.PopularCourseItem

// view model
class homeViewModel(
    private val blogRespository: BlogRepository,
    private val courseRepository: CourseRepository) : ViewModel()
{

    private val _blogsList = MutableStateFlow<List<blogs>>(emptyList())
    val blogsList: StateFlow<List<blogs>> = _blogsList

    private val _courseList = MutableStateFlow<List<courses>>(emptyList())
    val courseList: StateFlow<List<courses>> = _courseList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchBlogs()
        fetchCourses()
    }


    private fun fetchBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = blogRespository.getLatestBlogs()) {
                is BlogResult.Success -> _blogsList.value = result.data ?: emptyList()
                is BlogResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    private fun fetchCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = courseRepository.getLatestCourses()) {
                is CourseResult.Success -> _courseList.value = result.data ?: emptyList()
                is CourseResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
}

// view model factory
class HomeViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(homeViewModel::class.java)) {
            return homeViewModel(
                BlogRepository(supabase),
                CourseRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//  Main Activity
@Composable
fun ClientHomeScreen(navController: NavController) {
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
    ClientHomeView(navController,supabase)

}
// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: homeViewModel = viewModel(factory = HomeViewModelFactory (supabase))){

    val Listblogs by viewModel.blogsList.collectAsState()
    val Listcourses by viewModel.courseList.collectAsState()

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // thông tin user
    val session = authUser().getUserSession(context)
    val username = session["username"] as? String

    // xử lý lay anh đại diện
    val indexImage = session["index_image"] as? Int
    val imageName = "avatar_" + (indexImage?.toString() ?: "")
    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    val imageResId = if (resId != 0) resId else R.drawable.background

    var selectedIndex by remember { mutableStateOf(0) }
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index -> selectedIndex = index },
                    navController
                )
            }
        ){paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ){
                Image(
                    painter = painterResource(id = R.drawable.client_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 1f
                )

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp))
                {

                    // header - user welcome
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            // avatar
                            Box(
                                modifier = Modifier
                                    .size(59.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)) {
                                Image(
                                    painter = painterResource(id = imageResId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(59.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray), // optional nếu ảnh không full bo
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            // welcome text
                            Column {
                                Text(
                                    "Chào buổi sáng",
                                    fontSize = 15.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "${username}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // danh sach khoa hoc
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Khóa học mới nhất",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_course") }) {
                                    Text("Xem tất cả", color = Color(0xFF4ECDC4))
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Listcourses.forEach { course ->
                                    PopularCourseItem(course = course)
                                }
                            }
                        }
                    }

                    // danh sach blo
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Blog mới nhất",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_blog") }) {
                                    Text("Xem tất cả",color = Color(0xFF4ECDC4))
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Listblogs.forEach { blog ->
                                    BlogPostItem(blog)
                                }
                            }
                        }
                    }
                }
            }

    }
}



