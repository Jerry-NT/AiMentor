package com.example.aisupabase.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import blogs
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.models.Tags
import course_roadmaps
import courses
import kotlinx.coroutines.delay
import lessons

object card_components {
    val supabase = SupabaseClientProvider.client
    @Composable
    fun CourseCard(course: courses, process: Double, navController: NavController) {
        Card(
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            onClick = {
                navController.navigate("client_detail_course/${course.id}")
            }
        ) {
            Column {
                // Course Image/Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    // Image for courses like Data Science
                    AsyncImage(
                        model = course.url_image,
                        contentDescription = course.title_course,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Course Details Section with Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x994C1D95), // Purple-900
                                    Color(0x996366F1)  // Indigo-500
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Course Title
                        Text(
                            text = course.title_course,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )

                        // Progress Section at Bottom

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LinearProgressIndicator(
                                    progress = { (process / 100).toFloat() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF4CAF50),
                                    trackColor = Color(0xFFE0E0E0),
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${process.toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                    }
                }
            }
        }
    }

    @Composable
    fun PopularCourseItem(course: courses,navController: NavController,count:Int) {
        Card(
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            onClick = {
                navController.navigate("client_detail_course/${course.id}")
            }
        ) {
            Column {
                // Course Image/Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    // Image for courses like Data Science
                    AsyncImage(
                        model = course.url_image,
                        contentDescription = course.title_course,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x994C1D95), // Purple-900
                                    Color(0x996366F1)  // Indigo-500
                                )
                            )
                        )
                ){
                    // Course Details
                    Column(
                        modifier = Modifier.padding(16.dp)
                            .height(60.dp)
                    ) {
                        // Course Title
                        Text(
                            text = course.title_course,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Rating and Learner Count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Learner Count
                            Text(
                                text = "${count} Đăng ký",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }

            }
        }
    }

    @Composable
    fun BlogPostItem(blogPost: blogs,navController: NavController) {
        Card(
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            onClick = {
                navController.navigate("client_detail_blog/${blogPost.id}")
            }
        ) {
            Column {
                // Course Image/Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    // Image for courses like Data Science
                    AsyncImage(
                        model = blogPost.url_image,
                        contentDescription = blogPost.title_blog,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x994C1D95), // Purple-900
                                    Color(0x996366F1)  // Indigo-500
                                )
                            )
                        )
                ){
                    // Course Details
                    Column(
                        modifier = Modifier.padding(16.dp)
                            .height(60.dp)
                    ) {

                        // Course Title
                        Text(
                            text = blogPost.title_blog,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

            }
        }
    }

    @Composable
    fun tagItem(tags: Tags,count: Int,navController: NavController)
    {
        Card(
            shape = RoundedCornerShape(16.dp),
            onClick = {navController.navigate("client_blog_by_tag/${tags.id}")},
            modifier = Modifier
                .width(180.dp)
                .height(180.dp),
            ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4C1D95), // Purple-900
                                Color(0xFF6366F1)
                            )
                        )
                    )
            ){
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tags.title_tag,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${count} blog",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon placeholder
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)))
                    {
                        Image(
                            painter = painterResource(id = R.drawable.pic_2),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

        }
    }

    @Composable
    fun roadmapItem(roadmap:course_roadmaps,count:Int,navController: NavController)
    {
        Card(
            shape = RoundedCornerShape(16.dp),
            onClick = {navController.navigate("client_course_by_roadmap/${roadmap.id}")},
            modifier = Modifier
                .width(180.dp)
                .height(180.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4C1D95), // Purple-900
                                Color(0xFF6366F1)
                            )
                        )
                    )
            ){
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = roadmap.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${count} khóa học",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon placeholder
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp)))
                    {
                        Image(
                            painter = painterResource(id = R.drawable.pic_1),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun LessonItem(
        lesson: lessons,
        onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (lesson.isCompleted)
//                Color(0xFF4ECDC4).copy(alpha = 0.1f)
//            else
//                Color(0xFFF8F9FA)
//        ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Check Icon
                Surface(
                    shape = CircleShape,
//                color = if (lesson.isCompleted)
//                    Color(0xFF4ECDC4)
//                else if (lesson.isLocked)
//                    Color.Gray.copy(alpha = 0.3f)
//                else
                    color = Color(0xFF4ECDC4).copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        //when {
//                        lesson.isCompleted -> Icons.Default.Check
//                        lesson.isLocked -> Icons.Default.Lock
//                        else -> Icons.Default.PlayArrow },
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .wrapContentSize(Alignment.Center),
//                    tint = if (lesson.isCompleted)
//                        Color.White
//                    else if (lesson.isLocked)
//                        Color.Gray
//                    else
                        Color(0xFF4ECDC4)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Lesson Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = lesson.title_lesson,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
//                    color = if (lesson.isLocked)
//                        Color.Gray
//                    else
                        color = Color(0xFF2D3748),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${lesson.duration}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Status indicator
//            if (lesson.isCompleted) {
//                Surface(
//                    shape = RoundedCornerShape(12.dp),
//                    color = Color(0xFF4ECDC4).copy(alpha = 0.2f)
//                ) {
//                    Text(
//                        text = "Hoàn thành",
//                        fontSize = 12.sp,
//                        color = Color(0xFF4ECDC4),
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                    )
//                }
//            }
            }
        }
    }

    data class BannerItem(
        val title: String,
        val subtitle: String,
        val imageRes: Int // Replace with your actual image resource
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CarouselBanner(
        bannerItems: List<BannerItem>,
        modifier: Modifier = Modifier,
        autoScrollDuration: Long = 3000L
    ) {
        val pagerState = rememberPagerState(pageCount = { bannerItems.size })

        // Auto-scroll effect
        LaunchedEffect(pagerState) {
            while (true) {
                delay(autoScrollDuration)
                val nextPage = (pagerState.currentPage + 1) % bannerItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }

        Column(modifier = modifier) {
            // Main banner content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) { page ->
                BannerCard(
                    item = bannerItems[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Carousel indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(bannerItems.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    ) {
                        // Empty box for indicator dot
                    }
                    if (index < bannerItems.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun BannerCard(
        item: BannerItem,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4C1D95), // Purple-900
                                Color(0xFF6366F1)  // Indigo-500
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side - Text content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )


                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right side - Illustration
                    Box(
                        modifier = Modifier
                            .size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = "Banner illustration",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}