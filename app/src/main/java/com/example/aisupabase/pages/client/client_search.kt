package com.example.aisupabase.pages.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import blogs
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.components.bottombar.BottomNavigationBar
import com.example.aisupabase.components.card_components.BlogPostItem
import com.example.aisupabase.components.card_components.PopularCourseItem
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.BlogRepository
import com.example.aisupabase.controllers.BlogResult
import com.example.aisupabase.controllers.CourseRepository
import com.example.aisupabase.controllers.CourseResult
import com.example.aisupabase.controllers.LearnRepository
import com.example.aisupabase.controllers.authUser
import courses
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.get

class searchViewModel(
    private val blogRespository: BlogRepository,
    private val courseRepository: CourseRepository,
    private val learnRepository: LearnRepository):ViewModel()
{
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _blogsList = MutableStateFlow<List<blogs>>(emptyList())
    val blogsList: StateFlow<List<blogs>> = _blogsList

    private val _coursesList = MutableStateFlow<List<courses>>(emptyList())
    val coursesList: StateFlow<List<courses>> = _coursesList

    private val _subCount = MutableStateFlow(0)
    val subCount: StateFlow<Int> = _subCount

    fun searchCourse(query: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = courseRepository.searchCourse(query)
            when (result) {
                is CourseResult.Success -> {
                    _coursesList.value = result.data ?: emptyList()
                }
                is CourseResult.Error -> {
                    _error.value = result.exception.message
                }
            }
            _isLoading.value = false
        }
    }

    fun searchBlog(query: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = blogRespository.searchBlog(query)
            when (result) {
                is BlogResult.Success -> {
                    _blogsList.value = result.data ?: emptyList()
                }
                is BlogResult.Error -> {
                    _error.value = result.exception.message
                }
            }
            _isLoading.value = false
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
}

// viewFactory
class searchViewModelFactory(private val supabase: SupabaseClient): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(searchViewModel::class.java)) {
            return searchViewModel(BlogRepository(supabase), CourseRepository(supabase),LearnRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
@Composable
fun Client_Search(navController: NavController) {
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
    SearchHomeView(navController,supabase)
}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: searchViewModel = viewModel(factory = searchViewModelFactory (supabase))
)
{
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

    var expanded by rememberSaveable { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()
    val searchResults by viewModel.coursesList.collectAsState()
    val searchResultsBlog by viewModel.blogsList.collectAsState()
    val subCount by viewModel.subCount.collectAsState()

    val onSearch: (String) -> Unit = { query ->
        viewModel.searchCourse(query)
        viewModel.searchBlog(query)
    }

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
    ){paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

        ) {
            AsyncImage(
                model = R.drawable.bg_6,
                contentDescription = "Ảnh",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
                Box(modifier = Modifier
                    .fillMaxSize()
                    .semantics { isTraversalGroup = true })
                {
                    SearchBar(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .semantics { traversalIndex = 0f },
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = textFieldState.text.toString(),
                                onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                                onSearch = {
                                    onSearch(textFieldState.text.toString())
                                    expanded = false
                                },
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                placeholder = { Text("Search") }
                            )
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            if(!searchResults.isEmpty()) {
                                Text(
                                    "Tài liệu",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = searchResults,
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
                            if(!searchResultsBlog.isEmpty()){
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Bài đăng",
                                    fontSize =20.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(
                                        items = searchResultsBlog,
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

