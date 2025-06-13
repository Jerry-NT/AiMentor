package com.example.aisupabase.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import blogs
import coil.compose.AsyncImage
import com.example.aisupabase.config.SupabaseClientProvider
import com.example.aisupabase.config.handle.TagName
import courses
import lessons

object card_components {
    val supabase = SupabaseClientProvider.client
    @Composable
    fun CourseCard(course: courses) {
        Card(
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                ) {
                    //avatar
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    course.title_course,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    course.des_course,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
//                if (course.progress > 0) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    LinearProgressIndicator(
//                        progress = course.progress,
//                        modifier = Modifier.fillMaxWidth(),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
            }
        }
    }

    @Composable
    fun PopularCourseItem(course: courses,navController: NavController) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .clickable { navController.navigate("client_detail_course/${course.id}") },
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    // hien thi anh tu csdl course.url_image
                    AsyncImage(
                        model = course.url_image,
                        contentDescription = "Ảnh khóa học",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        course.title_course,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
//                    Text(
//                        course.des_course,
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(top = 2.dp)
//                    )
                }
            }
        }
    }

    @Composable
    fun BlogPostItem(blogPost: blogs,navController: NavController) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically ,
                    modifier = Modifier.clickable { navController.navigate("client_detail_blog/${blogPost.id}") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        AsyncImage(
                            model = blogPost.url_image,
                            contentDescription = "Ảnh khóa học",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                        ) {
                                TagName(supabase, blogPost.id_tag) }
                        Text(
                            blogPost.title_blog,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
}