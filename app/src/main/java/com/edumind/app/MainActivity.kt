//package com.edumind.app
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.edumind.app.ui.theme.EdumindmobileTheme
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            EdumindmobileTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "EduMind",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Welcome to $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    EdumindmobileTheme {
//        Greeting("EduMind")
//    }
//}
package com.edumind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edumind.app.ui.theme.EduMindTheme
import com.edumind.app.ui.theme.EduMindTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduMindTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EduMindPracticeCard(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EduMindPracticeCard(modifier: Modifier = Modifier) {
    // 1. Quản lý trạng thái (State)
    var studentName by remember { mutableStateOf("") }
    var enrolledCount by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "EduMind - Nền tảng học trực tuyến",
                style = MaterialTheme.typography.titleLarge
            )

            // Ô nhập liệu (TextField)
            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it },
                label = { Text("Tên học viên") },
                placeholder = { Text("Nhập họ và tên...") },
                modifier = Modifier.fillMaxWidth()
            )

            // Phản hồi UI tức thì theo state
            if (studentName.isNotBlank()) {
                Text(
                    text = "Xin chào, $studentName! Chúc bạn học tốt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            // Row chứa nút bấm tăng số khóa học
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Đã đăng ký: $enrolledCount khóa")

                Button(onClick = { enrolledCount++ }) {
                    Text("+ Đăng ký khóa")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PracticePreview() {
    EduMindTheme() {
        EduMindPracticeCard(modifier = Modifier.padding(16.dp))
    }
}
