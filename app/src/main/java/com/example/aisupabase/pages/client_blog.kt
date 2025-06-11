package com.example.aisupabase.pages

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.compose.currentBackStackEntryAsState
import blogs
import com.example.aisupabase.R
import com.example.aisupabase.components.bottombar.BottomNavigationBar
import com.example.aisupabase.components.card_components.BlogPostItem
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.controllers.BlogRepository
import com.example.aisupabase.controllers.BlogResult
import com.example.aisupabase.controllers.authUser
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.get

class blogViewModel(private val blogRespository: BlogRepository):ViewModel()
{
    private val _blogsList = MutableStateFlow<List<blogs>>(emptyList())
    val blogsList: StateFlow<List<blogs>> = _blogsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchBlogs()
    }
    private fun fetchBlogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = blogRespository.getBlogs()) {
                is BlogResult.Success -> _blogsList.value = result.data ?: emptyList()
                is BlogResult.Error -> _error.value = result.exception.message
            }
            _isLoading.value = false
        }
    }
}

// view factory
class blogViewModelFactory(private val supabase: SupabaseClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(blogViewModel::class.java)) {
            return blogViewModel(BlogRepository(supabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun Client_Blog(navController: NavController) {
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
    BlogHomeView(navController,supabase)
}

// CRUD view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogHomeView(
    navController: NavController,
    supabase: SupabaseClient,
    viewModel: blogViewModel = viewModel(factory = blogViewModelFactory(supabase))
)
{
    val Listblogs by viewModel.blogsList.collectAsState()
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

    // dropdown loc blog - moi nhat - cu nhat theo created_at
    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("Mới nhất", "Cũ nhất")
    var selectedFilter by remember { mutableStateOf(filterOptions[0]) }
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
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Danh sách blog",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold ,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        // Dropdown filter
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(selectedFilter, color = Color(0xFF4ECDC4))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text=option, color = Color(0xFF4ECDC4))},
                                        onClick = {
                                            selectedFilter = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val blogToShow = when (selectedFilter) {
                                "Mới nhất" -> Listblogs.sortedByDescending { it.created_at }
                                "Cũ nhất" -> Listblogs.sortedBy { it.created_at }
                                else -> Listblogs
                            }

                            blogToShow.forEach { blog ->
                                BlogPostItem(blog)
                            }
                        }
                    }
                }
            }
        }

    }
}