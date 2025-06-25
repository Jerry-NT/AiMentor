package com.example.aisupabase.pages.client

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.aisupabase.R
import com.example.aisupabase.controllers.notification_crud.cancelDailyNotification
import com.example.aisupabase.controllers.notification_crud.createNotificationChannel
import com.example.aisupabase.controllers.notification_crud.getScheduledNotificationDetails
import com.example.aisupabase.controllers.notification_crud.scheduleDailyNotification
import com.example.aisupabase.ui.theme.Red
import kotlinx.datetime.LocalTime

data class AlarmItem(
    val id: Int,
    val time: String,
    val frequency: String,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmSettingsScreen() {
    // Sample alarm data
    val context = LocalContext.current

// Lấy danh sách notificationDetails từ WorkManager
    val notificationDetails = getScheduledNotificationDetails(context) // List<NotificationDetail>
// Kết hợp thành danh sách AlarmItem
    var alarms by remember {
        mutableStateOf(
            notificationDetails.map { detail ->
                val hourStr = detail.hour.toString().padStart(2, '0')
                val minuteStr = detail.minute.toString().padStart(2, '0')
                val timeFormatted = "$hourStr:$minuteStr"
                AlarmItem(
                    id = detail.id,
                    time = timeFormatted,
                    frequency = detail.title,
                )
            }
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<AlarmItem?>(null) }
    var showTimeDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Quản lý lịch hẹn thông báo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                },

                actions = {
                    FloatingActionButton(
                        onClick = {  showTimeDialog = true },
                        modifier = Modifier.size(36.dp),
                        containerColor = Color(0xFFFF4444),
                        contentColor = Color.White
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Alarm",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)

        ) {
            AsyncImage(
                model = R.drawable.bg_7,
                contentDescription = "Ảnh",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(alarms) { alarm ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showDeleteDialog = true;selected = alarm }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),

                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = alarm.time,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.Black,
                                letterSpacing = (-1).sp
                            )
                        }
                    }
                }
            }
        }

        if (showTimeDialog) {
            TimePickerDialog(
                onDismissRequest = { showTimeDialog = false },
                onTimeSelected = { time -> // lưu giờ người dùng chọn
                    showTimeDialog = false
                    createNotificationChannel(context)
                    scheduleDailyNotification(context,"tieu de","message",time.hour,time.minute)
                    alarms = getScheduledNotificationDetails(context).map { detail ->
                        val hourStr = detail.hour.toString().padStart(2, '0')
                        val minuteStr = detail.minute.toString().padStart(2, '0')
                        val timeFormatted = "$hourStr:$minuteStr"
                        AlarmItem(
                            id = detail.id,
                            time = timeFormatted,
                            frequency = detail.title,
                        )
                    }
                }
            )
        }

        if (showDeleteDialog && selected != null) {
            Dialog(onDismissRequest = { showDeleteDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Xác nhận xóa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            "Bạn có thực sự muốn xóa ?",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    cancelDailyNotification(context, selected!!.id)
                                    alarms = getScheduledNotificationDetails(context).map { detail ->
                                        val hourStr = detail.hour.toString().padStart(2, '0')
                                        val minuteStr = detail.minute.toString().padStart(2, '0')
                                        val timeFormatted = "$hourStr:$minuteStr"
                                        AlarmItem(
                                            id = detail.id,
                                            time = timeFormatted,
                                            frequency = detail.title,
                                        )
                                    }
                                    showDeleteDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Red)
                            ) { Text("Xóa", color = Color.White) }
                            OutlinedButton(
                                onClick = { showDeleteDialog = false },
                                modifier = Modifier.weight(1f)
                            ) { Text("Hủy") }
                        }
                    }
                }
            }
        }
    }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    val timeState = rememberTimePickerState(is24Hour = true)   // 24 h, đổi thành false nếu muốn 12 h

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timeState)

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = onDismissRequest) {
                        Text("Hủy")
                    }
                    Button(onClick = {
                        val picked = LocalTime(timeState.hour, timeState.minute)
                        onTimeSelected(picked)
                    }) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}
@Composable
fun Client_Noti(navController: NavController) {
        AlarmSettingsScreen()
}