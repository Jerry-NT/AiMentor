package com.example.aisupabase.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.example.aisupabase.components.card_components.BannerItem
import com.example.aisupabase.components.card_components.CarouselBanner
import com.example.aisupabase.components.card_components.CourseCard
import com.example.aisupabase.config.function_handle_public.LazyLoadItem
import com.example.aisupabase.controllers.LearnResult
import com.example.aisupabase.models.streaks
import com.example.aisupabase.ui.theme.Purple100
import java.time.LocalDate

// view model
class homeViewModel(
    private val blogRepository: BlogRepository,
    private val courseRepository: CourseRepository,
    private val roadMapRepository: RoadMapRepository,
    private val tagRepository: TagRepository,
    private val learnRepository: LearnRepository
) : ViewModel() {
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _roadmapCourseCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val roadmapCourseCounts: StateFlow<Map<Int, Int>> = _roadmapCourseCounts

    private val _tagCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val tagCounts: StateFlow<Map<Int, Int>> = _tagCounts

    private val _processMap = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val processMap: StateFlow<Map<Int, Double>> = _processMap

    private val _subCount = MutableStateFlow(0)
    val subCount: StateFlow<Int> = _subCount

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

    fun fetchCoursesByUser(id_user: Int) {
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

    private fun fetchTag() {
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

                else -> { /* handle error if needed */
                }
            }
        }
    }

    fun loadBlogConuntForTag(id: Int) {
        viewModelScope.launch {
            when (val result = blogRepository.getBlogByTagID(id)) {
                is BlogResult.Success -> {
                    val count = result.data?.size ?: 0
                    _tagCounts.value = _tagCounts.value + (id to count)
                }

                else -> { /* handle error if needed */
                }
            }
        }
    }

    fun getCountSub(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = learnRepository.getCountSub(id)) {
                else -> _subCount.value = result
            }
            _isLoading.value = false
        }
    }

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


    private val _StreakList = MutableStateFlow<List<streaks>>(emptyList())
    val StreakList: StateFlow<List<streaks>> = _StreakList

    fun getStreak(id_user: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = learnRepository.getStreak(id_user)) {
                is LearnResult.Success -> {
                Log.d("Streak", "getStreak: ${result.data}")
                    _StreakList.value = result.data ?: emptyList()
                }

                is LearnResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }

    fun checkStreak(id_user: Int)
    {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = learnRepository.addOrUpdateStreak(id_user)) {
                is LearnResult.Success -> {
                     getStreak(id_user)
                }
                is LearnResult.Error -> {
                    _error.value = result.exception.message
                }

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
    ClientHomeView(navController, supabase)
}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: homeViewModel = viewModel(factory = HomeViewModelFactory(supabase))
) {
    val Listblogs by viewModel.blogsList.collectAsState()
    val Listcourses by viewModel.courseList.collectAsState()
    val ListRoadMap by viewModel.roadMapList.collectAsState()
    val ListTag by viewModel.tagList.collectAsState()
    val ListcoursesByUser by viewModel.courseListbyUser.collectAsState()
    val listStreak by viewModel.StreakList.collectAsState()

    val roadmapCourseCounts by viewModel.roadmapCourseCounts.collectAsState()
    val subCount by viewModel.subCount.collectAsState()
    val processMap by viewModel.processMap.collectAsState()
    val tagCounts by viewModel.tagCounts.collectAsState()

    val context = LocalContext.current

    // thông tin user
    val session = authUser().getUserSession(context)
    val username = session["username"] as? String
    val userid = session["id"]

    // xử lý lay anh đại diện
    val imageResId = remember(session["index_image"]) {
        val indexImage = session["index_image"] as? Int
        val imageName = "avatar_" + (indexImage?.toString() ?: "")
        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        if (resId != 0) resId else R.drawable.background
    }

    // xu ly list anh image_x tu 0 -> 5
    val imageList = remember {
        (0..7).map { index ->
            val imageName = "image_$index"
            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (resId != 0) resId else R.drawable.background
        }
    }

    val imageListBlog = remember {
        (0..7).map { index ->
            val imageName = "image_blog_$index"
            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            if (resId != 0) resId else R.drawable.background
        }
    }

    val bannerItems = remember {
        listOf(
            BannerItem(
                title = "AI Mentor bạn của mọi nhà.",
                subtitle = "Hãy tận hưởng, trải nghiệm và nâng cấp kỹ năng",
                imageRes = R.drawable.image_0
            ),
            BannerItem(
                title = "AI Mentor hãy học theo cái cách của bạn.",
                subtitle = "Gần mực thì đen, gần quên thì xuất hiện.",
                imageRes = R.drawable.image_1
            ),
            BannerItem(
                title = "AI Mentor && Fast Document",
                subtitle = "Tạo nhanh tài liệu, học nhanh kiến thức.",
                imageRes = R.drawable.image_2
            )
        )
    }

    // xu ly effect
    LaunchedEffect(userid) {
        viewModel.fetchCoursesByUser(userid as Int)
    }

    LaunchedEffect(userid) {
        viewModel.getStreak(userid as Int)
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            )
            {

                // header - user welcome
                item {
                    LazyLoadItem {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // avatar
                            Box(
                                modifier = Modifier
                                    .size(59.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            ) {
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
                                    "Welcome to,",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    "$username",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // wellcome text
                item {
                    LazyLoadItem {
                        Text(
                            text = buildAnnotatedString {
                                append("Hãy chọn\n")

                                withStyle(style = SpanStyle(color = Purple100)) {
                                    append("Khóa học dành cho bạn")
                                }
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        )
                    }
                }

                // banner
                item {
                    LazyLoadItem {
                        CarouselBanner(
                            bannerItems = bannerItems,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // điểm danh
                item {
                    LazyLoadItem {
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
                                                Color(0x996366F1), // Indigo-500
                                                Color(0x99CF55E7)
                                            )
                                        )
                                    )
                            )
                            {
                                Row(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween

                                ) {
                                    if (listStreak.isEmpty()) {
                                        // No streak yet, show default
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "🔥", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "0",
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
                                                .clickable(onClick = { viewModel.checkStreak(userid as Int) })
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
                                    } else {
                                        listStreak.forEach { streak ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "🔥", fontSize = 24.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "${streak.count}",
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
                                            val createdAtDate = LocalDate.parse(streak.created_at.substring(0, 10))
                                            val today = LocalDate.now()
                                            // Always show the check-in button if not checked in today
                                            if (createdAtDate.isBefore(today)) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF6366F1),
                                                    modifier = Modifier.size(48.dp)
                                                        .clickable(onClick = { viewModel.checkStreak(userid as Int) })
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
                            }
                        }
                    }
                }

                // khoa hoc da hoc
                item {
                    LazyLoadItem {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tài liệu đang tham khảo",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_course_user") }) {
                                    Text("Xem tất cả", color = Purple100)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = ListcoursesByUser,
                                        key = { course -> course.id ?: 0 }
                                    ) { course ->
                                        LaunchedEffect(course.id, userid) {
                                            viewModel.getProcess(
                                                userid as Int,
                                                course.id ?: 0
                                            )
                                        }
                                        val process = processMap[course.id ?: 0] ?: 0.0
                                        CourseCard(course, process, navController)
                                    }
                                }
                            }
                        }
                    }
                }

                // list lo trinh
                item {
                    LazyLoadItem {
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
                                    Text("Xem tất cả", color = Purple100)
                                }
                            }

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = ListRoadMap.take(2),
                                    key = { roadmap -> roadmap.id ?: 0 }
                                ) { roadmap ->
                                    val index = ListRoadMap.take(2).indexOf(roadmap)
                                    val count = roadmapCourseCounts[roadmap.id ?: 0] ?: 0
                                    val idImage =
                                        imageList.getOrNull(index) ?: R.drawable.background

                                    LaunchedEffect(roadmap.id) {
                                        if (roadmap.id != null) {
                                            viewModel.loadCourseCountForRoadmap(roadmap.id)
                                        }
                                    }
                                    roadmapItem(roadmap, count, navController, idImage)
                                }
                            }
                        }
                    }
                }

                // list tag
                item {
                    LazyLoadItem {
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
                                    Text("Xem tất cả", color = Purple100)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = ListTag.take(2),
                                        key = { tag -> tag.id }
                                    ) { tag ->
                                        val index = ListTag.take(2).indexOf(tag)
                                        val count = tagCounts[tag.id] ?: 0
                                        val idImage = imageListBlog.getOrNull(index)
                                            ?: R.drawable.background

                                        LaunchedEffect(tag.id) {
                                            viewModel.loadBlogConuntForTag(tag.id)
                                        }
                                        tagItem(tag, count, navController, idImage)
                                    }
                                }
                            }
                        }
                    }
                }

                // danh sach khoa hoc
                item {
                    LazyLoadItem {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tài liệu mới nhất",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { navController.navigate("client_course") }) {
                                    Text("Xem tất cả", color = Purple100)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = Listcourses,
                                        key = { course -> course.id ?: 0 }
                                    ) { course ->
                                        LaunchedEffect(course.id) {
                                            if (course.id != null) {
                                                viewModel.getCountSub(course.id)
                                            }
                                        }
                                        PopularCourseItem(course, navController, subCount)
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Mentor Bottom Banner
                item {
                    LazyLoadItem {
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
                }

                // danh sach blo
                item {
                    LazyLoadItem {
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
                                    Text("Xem tất cả", color = Purple100)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = Listblogs,
                                        key = { blog ->
                                            blog.id ?: 0
                                        }  // Assuming blog has id field
                                    ) { blog ->
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
}