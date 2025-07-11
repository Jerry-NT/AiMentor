package com.example.aisupabase.pages.client

import Question
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aisupabase.config.GeminiService
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Client_Quiz(
    navController: NavController,
    prompt: String
) {
    // State management
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResults by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    val geminiService = remember { GeminiService() }

    // Load questions effect
    LaunchedEffect(prompt) {
        isLoading = true
        errorMessage = null

        try {
            val result = geminiService.generateQuestion(prompt)
            Log.d("Client_Quiz", "Result: $result")

            result.onSuccess { generatedQuestions ->
                questions = generatedQuestions
                errorMessage = null
                // Reset states when new questions load
                selectedAnswers.clear()
                showResults = false
                isSubmitted = false
            }.onFailure { error ->
                errorMessage = error.message ?: "Lỗi không xác định"
                questions = emptyList()
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi tải câu hỏi: ${e.message}"
            questions = emptyList()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with improved styling
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Quiz Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Bài Trắc Nghiệm",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (questions.isNotEmpty()) "${questions.size} câu hỏi" else "Đang tải...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content area
        when {
            isLoading -> {
                LoadingState()
            }
            errorMessage != null -> {
                ErrorState(
                    message = errorMessage!!,
                    onRetry = {
                        isLoading = true
                        errorMessage = null
                    }
                )
            }
            questions.isEmpty() -> {
                EmptyState()
            }
            else -> {
                QuizContent(
                    questions = questions,
                    selectedAnswers = selectedAnswers,
                    showResults = showResults,
                    isSubmitted = isSubmitted,
                    onAnswerSelected = { questionIndex, answer ->
                        if (!isSubmitted) {
                            selectedAnswers = selectedAnswers.toMutableMap().apply {
                                put(questionIndex, answer)
                            }
                        }
                    },
                    onSubmit = {
                        isSubmitted = true
                        showResults = true
                    },
                    onShowResults = { showResults = !showResults },
                    onReset = {
                        selectedAnswers.clear()
                        selectedAnswers = mutableMapOf()
                        showResults = false
                        isSubmitted = false
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đang tạo câu hỏi...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Vui lòng chờ trong giây lát",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Có lỗi xảy ra",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thử lại")
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "No questions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có câu hỏi nào",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Vui lòng thử lại sau",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun QuizContent(
    questions: List<Question>,
    selectedAnswers: Map<Int, String>,
    showResults: Boolean,
    isSubmitted: Boolean,
    onAnswerSelected: (Int, String) -> Unit,
    onSubmit: () -> Unit,
    onShowResults: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Questions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(questions) { index, question ->
                QuestionCard(
                    questionIndex = index,
                    question = question,
                    selectedAnswer = selectedAnswers[index],
                    onAnswerSelected = { answer -> onAnswerSelected(index, answer) },
                    showCorrectAnswer = showResults,
                    isSubmitted = isSubmitted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Progress indicator
        if (!isSubmitted) {
            ProgressIndicator(
                answeredCount = selectedAnswers.size,
                totalCount = questions.size
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons
        ActionButtons(
            selectedAnswers = selectedAnswers,
            totalQuestions = questions.size,
            showResults = showResults,
            isSubmitted = isSubmitted,
            onSubmit = onSubmit,
            onShowResults = onShowResults,
            onReset = onReset
        )

        // Results Summary - Always show when submitted
        if (isSubmitted) {
            Spacer(modifier = Modifier.height(16.dp))
            ResultsSummary(
                questions = questions,
                selectedAnswers = selectedAnswers
            )
        }
    }
}

@Composable
fun ProgressIndicator(
    answeredCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$answeredCount/$totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (totalCount > 0) answeredCount.toFloat() / totalCount else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ActionButtons(
    selectedAnswers: Map<Int, String>,
    totalQuestions: Int,
    showResults: Boolean,
    isSubmitted: Boolean,
    onSubmit: () -> Unit,
    onShowResults: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Đảm bảo nút "Nộp bài" luôn hiển thị khi chưa nộp bài
        if (!isSubmitted) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                enabled = selectedAnswers.size == totalQuestions, // Chỉ cho phép nộp khi đã chọn đủ đáp án
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Submit"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nộp bài")
            }
        } else {
            Button(
                onClick = onShowResults,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showResults) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (showResults) Icons.Default.Close else Icons.Default.CheckCircle,
                    contentDescription = if (showResults) "Hide answers" else "Show answers"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showResults) "Ẩn đáp án" else "Xem đáp án")
            }
        }

        // Nút "Làm lại" luôn hiển thị
        Button(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Làm lại")
        }
    }
}

@Composable
fun QuestionCard(
    questionIndex: Int,
    question: Question,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    showCorrectAnswer: Boolean,
    isSubmitted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Question header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Câu ${questionIndex + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Difficulty indicator (could be enhanced based on question index)
                val difficulty = when {
                    questionIndex < 2 -> "Cơ bản"
                    questionIndex < 4 -> "Trung bình"
                    else -> "Nâng cao"
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = difficulty,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question Text
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Answer Options
            val options = listOf(
                "A" to question.A,
                "B" to question.B,
                "C" to question.C,
                "D" to question.D
            )

            options.forEach { (optionKey, optionText) ->
                AnswerOption(
                    optionKey = optionKey,
                    optionText = optionText,
                    isSelected = selectedAnswer == optionKey,
                    isCorrect = question.choice == optionKey,
                    showCorrectAnswer = showCorrectAnswer,
                    isSubmitted = isSubmitted,
                    onSelected = { onAnswerSelected(optionKey) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AnswerOption(
    optionKey: String,
    optionText: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showCorrectAnswer: Boolean,
    isSubmitted: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        showCorrectAnswer && isCorrect -> Color(0xFF4CAF50) // Green for correct
        showCorrectAnswer && isSelected && !isCorrect -> Color(0xFFF44336) // Red for wrong selection
        isSelected && !showCorrectAnswer -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        showCorrectAnswer && isCorrect -> Color.White
        showCorrectAnswer && isSelected && !isCorrect -> Color.White
        isSelected && !showCorrectAnswer -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = when {
        showCorrectAnswer && isCorrect -> Color(0xFF4CAF50)
        showCorrectAnswer && isSelected && !isCorrect -> Color(0xFFF44336)
        isSelected && !showCorrectAnswer -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { if (!isSubmitted) onSelected() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected || (showCorrectAnswer && isCorrect)) 2.dp else 1.dp,
            color = borderColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option Letter
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = when {
                    showCorrectAnswer && isCorrect -> Color.White
                    showCorrectAnswer && isSelected && !isCorrect -> Color.White
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optionKey,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            showCorrectAnswer && isCorrect -> Color(0xFF4CAF50)
                            showCorrectAnswer && isSelected && !isCorrect -> Color(0xFFF44336)
                            isSelected -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Option Text
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )

            // Selection/Result Indicator
            if (showCorrectAnswer && isCorrect) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Correct",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else if (showCorrectAnswer && isSelected && !isCorrect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Wrong",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = if (showCorrectAnswer && !isCorrect) Color.White
                        else MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun ResultsSummary(
    questions: List<Question>,
    selectedAnswers: Map<Int, String>,
    modifier: Modifier = Modifier
) {
    val correctCount = questions.indices.count { index ->
        selectedAnswers[index] == questions[index].choice
    }
    val totalQuestions = questions.size
    val percentage = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0

    val resultColor = when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 60 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }

    val resultText = when {
        percentage >= 80 -> "Xuất sắc!"
        percentage >= 60 -> "Khá tốt!"
        else -> "Cần cải thiện"
    }

    val resultIcon = when {
        percentage >= 80 -> Icons.Default.Star
        percentage >= 60 -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result icon
            Surface(
                color = resultColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(
                    imageVector = resultIcon,
                    contentDescription = "Result",
                    tint = resultColor,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result text
            Text(
                text = resultText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = resultColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Score display
            Text(
                text = "Điểm số: $correctCount/$totalQuestions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "($percentage%)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = "Đúng",
                    value = correctCount.toString(),
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.CheckCircle
                )

                StatItem(
                    title = "Sai",
                    value = (totalQuestions - correctCount).toString(),
                    color = Color(0xFFF44336),
                    icon = Icons.Default.Close
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Motivational message
            val motivationalMessage = when {
                percentage >= 80 -> "Bạn đã làm rất tốt! Hãy tiếp tục duy trì phong độ này."
                percentage >= 60 -> "Kết quả khá tốt! Hãy ôn tập thêm để đạt điểm cao hơn."
                else -> "Đừng nản lòng! Hãy ôn tập và thử lại để cải thiện kết quả."
            }

            Text(
                text = motivationalMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier
                    .size(32.dp)
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
    }
}
