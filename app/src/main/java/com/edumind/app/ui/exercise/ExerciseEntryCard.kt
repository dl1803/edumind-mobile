package com.edumind.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edumind.app.data.model.response.ExerciseEntry
import com.edumind.app.ui.theme.EduMindFontFamily
import com.edumind.app.ui.theme.Neutral200
import com.edumind.app.ui.theme.Neutral500
import com.edumind.app.ui.theme.Neutral950
import com.edumind.app.ui.theme.Primary50
import com.edumind.app.ui.theme.Primary700

@Composable
fun ExerciseEntryCard(
    entry: ExerciseEntry?,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (entry == null) return

    val displayTitle = remember(entry.title) {
        val trimmed = entry.title.trimStart()
        val detail = if (trimmed.startsWith("Bài tập cuối bài", ignoreCase = true)) {
            trimmed.drop("Bài tập cuối bài".length)
                .trimStart()
                .removePrefix(":")
                .trim()
        } else {
            entry.title.trim()
        }
        detail.ifEmpty { entry.title }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Neutral200, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button) { onClick(entry.exerciseId) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Quiz,
                contentDescription = null,
                tint = Primary700,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bài tập cuối bài",
                fontFamily = EduMindFontFamily,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Neutral500
            )
            Text(
                text = displayTitle,
                fontFamily = EduMindFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral950
            )
        }
        Text(
            text = "Làm bài",
            fontFamily = EduMindFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Primary700
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseEntryCardPreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        ExerciseEntryCard(
            entry = ExerciseEntry(
                exerciseId = "exercise-mock-01",
                title = "Bài tập cuối bài: Biến và kiểu dữ liệu"
            ),
            onClick = {}
        )
    }
}
