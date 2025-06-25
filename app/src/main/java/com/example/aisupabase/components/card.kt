package com.example.aisupabase.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import blogs
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.config.function_handle_public.splitTextToSentences
import com.example.aisupabase.models.Tags
import com.example.aisupabase.pages.client.PricingPlan
import com.example.aisupabase.ui.theme.Purple100
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
                                    color = Color(0xFF4C1D95),
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
    fun tagItem(tags: Tags, count: Int, navController: NavController, imageRes: Int) {
        Card(
            modifier = Modifier
                .width(180.dp)
                .height(180.dp),
            onClick = { navController.navigate("client_blog_by_tag/${tags.id}") },
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // Canh giữa toàn bộ content
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Banner background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x994C1D95),
                                    Color(0xCC6366F1)
                                ),
                                radius = 300f
                            )
                        )
                )

                // Text content - Hoàn toàn center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tags.title_tag,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$count bài đăng",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun roadmapItem(roadmap:course_roadmaps,count:Int,navController: NavController,imageRes:Int)
    {
        Card(
            modifier = Modifier
                .width(180.dp)
                .height(180.dp),
            onClick = { navController.navigate("client_course_by_roadmap/${roadmap.id}") },
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // Canh giữa toàn bộ content
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Banner background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x994C1D95),
                                    Color(0xCC6366F1)
                                ),
                                radius = 300f
                            )
                        )
                )

                // Text content - Hoàn toàn center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = roadmap.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$count khóa học",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun LessonItem(
        lesson: lessons,
        onClick: () -> Unit,
        isCompleted: Boolean
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCompleted)
                    Color(0xFFF3E8FF) // Very light purple
                else
                    Color.White
            ),

            border = if (isCompleted) {
                BorderStroke(1.dp, Color(0xFFE9D5FF))
            } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Status Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = if (isCompleted) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6), // Purple-500
                                        Color(0xFFEC4899)  // Pink-500
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE9D5FF), // Purple-100
                                        Color(0xFFFCE7F3)  // Pink-100
                                    )
                                )
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when {
                        isCompleted -> Icons.Default.CheckCircle
                        else -> Icons.Default.PlayArrow
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isCompleted)
                            Color.White
                        else
                            Color(0xFF8B5CF6)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Lesson Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = lesson.title_lesson,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted)
                            Color(0xFF111827)
                        else
                            Color(0xFF374151),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Duration and status row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFF8B5CF6)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${lesson.duration} phút",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "✓ Xong",
                                    fontSize = 12.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Arrow indicator
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF8B5CF6).copy(alpha = 0.6f)
                )
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
        autoScrollDuration: Long = 3000L) {
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
                    .height(200.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                pageSpacing = 8.dp
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
        modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = "Banner background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay để text dễ đọc hơn
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xCC4C1D95), // Purple-900 with stronger transparency
                                    Color(0xCC6366F1)  // Indigo-500 with stronger transparency
                                )
                            )
                        )
                )

                // Text content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
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
            }
        }
    }


    @Composable
    fun PricingCard(
        type_account: String?,
        plan: PricingPlan,
        allPlans: List<PricingPlan>,
        modifier: Modifier = Modifier,
        onClick: () -> Unit) {

        val currentTypeMaxCount = allPlans.find { it.name == type_account }?.max_count ?: 0

        // Button enable logic
        val isDisabled = when {
            plan.name == type_account -> true // Disable button for current plan
            type_account == "Basic" -> plan.name == "Basic"
            else -> plan.name == "Basic" || plan.max_count < currentTypeMaxCount
        }

        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {
            Column {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = plan.gradientColors
                            ),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = plan.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        val displayPrice = if (plan.price % 1.0 == 0.0) {
                            plan.price.toInt().toString() // loại bỏ .0 nếu là số nguyên
                        } else {
                            "%.2f".format(plan.price)     // giữ 2 chữ số thập phân nếu có
                        }
                        Text(
                            text = "$displayPrice VNĐ",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = plan.period,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    // Features list
                    val features = splitTextToSentences(plan.content)
                    features.forEach { feature ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = plan.gradientColors.first(),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = feature,
                                fontSize = 16.sp,
                                color = Color(0xFF2D3748),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Get Started Button
                    Button(
                        onClick = onClick,
                        enabled = !isDisabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = plan.buttonColor
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Nâng cấp",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}