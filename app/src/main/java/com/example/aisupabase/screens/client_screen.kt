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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.aisupabase.controllers.authUser
import courses
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.aisupabase.components.bottombar.BottomNavigationBar
import com.example.aisupabase.R
import com.example.aisupabase.components.card_components.BlogPostItem
import com.example.aisupabase.components.card_components.PopularCourseItem
import com.example.aisupabase.components.card_components.roadmapItem
import com.example.aisupabase.components.card_components.tagItem
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.RoadMapRepository
import com.example.aisupabase.controllers.RoadMapResult
import com.example.aisupabase.controllers.TagRepository
import com.example.aisupabase.controllers.TagResult
import com.example.aisupabase.models.Tags
import course_roadmaps
import user_course
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import com.example.aisupabase.components.card_components.BannerItem
import com.example.aisupabase.components.card_components.CarouselBanner
import com.example.aisupabase.components.card_components.CourseCard

// view model
class homeViewModel(
    private val blogRepository: BlogRepository,
    private val courseRepository: CourseRepository,
    private val roadMapRepository: RoadMapRepository,
    private val tagRepository: TagRepository,
    private val learnRepository: LearnRepository
    ) : ViewModel()
{

    private val _blogsList = MutableStateFlow<List<blogs>>(emptyList())
    val blogsList: StateFlow<List<blogs>> = _blogsList

    private val _courseList = MutableStateFlow<List<courses>>(emptyList())
    val courseList: StateFlow<List<courses>> = _courseList

    private val _courseListbyUser = MutableStateFlow<List<courses>>(emptyList())
    val courseListbyUser: StateFlow<List<courses>> = _courseListbyUser

    private val _roadMapList = MutableStateFlow<List<course_roadmaps>>(emptyList())
    val roadMapList: StateFlow<List<course_roadmaps>> = _roadMapList

    private val _tagList = MutableStateFlow<List<Tags>>(emptyList())
    val tagList: StateFlow<List<Tags>> = _tagList

    private val _listUserCourse = MutableStateFlow<List<user_course>>(emptyList())
    val listUserCourse: StateFlow<List<user_course>> = _listUserCourse

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _roadmapCourseCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val roadmapCourseCounts: StateFlow<Map<Int, Int>> = _roadmapCourseCounts

    private val _tagCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val tagCounts: StateFlow<Map<Int, Int>> = _tagCounts


    init {
        fetchBlogs()
        fetchCourses()
        fetchRoadMap()
        fetchTag()
    }

    private fun fetchBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = blogRepository.getLatestBlogs()) {
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

    private fun fetchTag()
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = tagRepository.getTags()) {
                is TagResult.Success -> _tagList.value = result.data ?: emptyList()
                is TagResult.Error -> _error.value = result.exception.message
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

    fun loadBlogConuntForTag(id:Int)
    {
        viewModelScope.launch {
            when (val result = blogRepository.getBlogByTagID(id)) {
                is BlogResult.Success -> {
                    val count = result.data?.size ?: 0
                    _tagCounts.value = _tagCounts.value + (id to count)
                }
                else -> { /* handle error if needed */ }
            }
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

// view model factory
class HomeViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(homeViewModel::class.java)) {
            return homeViewModel(
                BlogRepository(supabase),
                CourseRepository(supabase),
                RoadMapRepository(supabase),
                TagRepository(supabase),
                LearnRepository(supabase)
            ) as T
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
    viewModel: homeViewModel = viewModel(factory = HomeViewModelFactory (supabase)))
{

    val Listblogs by viewModel.blogsList.collectAsState()
    val Listcourses by viewModel.courseList.collectAsState()
    val ListRoadMap by viewModel.roadMapList.collectAsState()
    val ListTag by viewModel.tagList.collectAsState()
    val ListcoursesByUser by viewModel.courseListbyUser.collectAsState()

    val roadmapCourseCounts by viewModel.roadmapCourseCounts.collectAsState()
    val subCount by viewModel.subCount.collectAsState()
    val processMap by viewModel.processMap.collectAsState()

    val tagCounts by viewModel.tagCounts.collectAsState()
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

    val bannerItems = listOf(
        BannerItem(
            title = "AI Mentor bạn của mọi nhà.",
            subtitle = "Hãy tận hưởng, trải nghiệm và nâng cấp kỹ năng",
            imageRes = R.drawable.pic_1
        ),
        BannerItem(
            title = "AI Mentor hãy học theo cái cách của bạn.",
            subtitle = "Gần mực thì đen, gần quên thì xuất hiện.",
            imageRes = R.drawable.pic_1 // Replace with your actual drawable
        ),
        BannerItem(
            title = "AI Mentor && Fast Document",
            subtitle = "Tạo nhanh tài liệu, học nhanh kiến thức.",
            imageRes = R.drawable.pic_1
        )
    )
    val id_user = session["id"]
    LaunchedEffect(id_user) {
        viewModel.fetchCoursesByUser(id_user as Int)
    }

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
                    .padding(paddingValues)
            ){

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x994C1D95),
                                    Color(0x996366F1)  ,
                                    Color(0x9972658F), // Purple-900
                                    Color(0x999595B7)  // Indigo-500
                                )
                            )
                        )
                        .blur(radius = 20.dp)
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
                                    color = Color.White
                                )
                                Text(
                                    "${username}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // wellcome text
                    item {
                        Text(
                            text = "Hãy chọn\nKhóa học dành cho bạn",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            lineHeight = 36.sp
                        )
                    }

                    // banner
                    item{
                        CarouselBanner(
                            bannerItems = bannerItems,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // điểm danh
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE6E6FA)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x994C1D95), // Purple-900
                                                Color(0x996366F1) , // Indigo-500
                                                Color(0x99CF55E7)
                                            )
                                        )
                                    )
                            )
                            {
                                Row(
                                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔥",
                                            fontSize = 24.sp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "19",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Chuỗi bùng nổ",
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF6366F1),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .wrapContentSize(Alignment.Center)
                                        )
                                    }
                                }
                            }

                        }
                    }

                    // khoa hoc da hoc
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Khóa học đang học",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_course_user") }) {
                                    Text("Xem tất cả", color = Color.White)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
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

                    // list lo trinh
                    item {
                        Column {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Lộ trình",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_roadmap") }) {
                                    Text("Xem tất cả",color = Color.White)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ListRoadMap.take(2).forEach {
                                    roadmap ->
                                    if(roadmap.title != "Người dùng")
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

                    // list tag
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Loại bài đăng",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_tag") }) {
                                    Text("Xem tất cả",color = Color.White)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ListTag.take(2).forEach {
                                        tag ->
                                    val count = tagCounts[tag.id ] ?: 0
                                    LaunchedEffect(tag.id) {
                                        viewModel.loadBlogConuntForTag(tag.id )
                                    }
                                    tagItem(tag,count,navController)
                                }
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
                                    Text("Xem tất cả", color = Color.White)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
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

                    // AI Mentor Bottom Banner
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFE8D5FF), // Light purple
                                                Color(0xFFD8B4FE), // Medium purple
                                                Color(0xFFC084FC)  // Slightly darker purple
                                            ),
                                            radius = 400f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "AI MENTOR",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4C1D95), // Dark purple
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Học nữa, học mãi, học không ngừng.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF6B46C1), // Medium purple
                                        textAlign = TextAlign.Center
                                    )
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
                                    Text("Xem tất cả",color = Color.White)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(Listblogs) { blog ->
                                        BlogPostItem(blog, navController)
                                    }
                                }

                            }
                        }
                    }
                }
            }
    }
}