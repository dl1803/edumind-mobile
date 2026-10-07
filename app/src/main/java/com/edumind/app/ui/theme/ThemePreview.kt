package com.edumind.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemePreviewContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EduMind Design System Preview",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Divider()

        // 1. TYPOGRAPHY PREVIEW (Tiếng Việt có dấu)
        Text(
            text = "1. Typography (Inter Font Family)",
            style = MaterialTheme.typography.titleLarge,
            color = Neutral950
        )

        val sampleText = "Tiếng Việt có dấu: Trần Lê Đức Lợi"

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TypographyItem(name = "Headline Large (28sp Bold)", text = sampleText, style = MaterialTheme.typography.headlineLarge)
            TypographyItem(name = "Headline Medium (22sp SemiBold)", text = sampleText, style = MaterialTheme.typography.headlineMedium)
            TypographyItem(name = "Title Large (18sp SemiBold)", text = sampleText, style = MaterialTheme.typography.titleLarge)
            TypographyItem(name = "Title Medium (16sp SemiBold)", text = sampleText, style = MaterialTheme.typography.titleMedium)
            TypographyItem(name = "Body Large (14sp Medium)", text = sampleText, style = MaterialTheme.typography.bodyLarge)
            TypographyItem(name = "Body Medium (14sp Regular)", text = sampleText, style = MaterialTheme.typography.bodyMedium)
            TypographyItem(name = "Body Small / Caption (12sp Regular)", text = sampleText, style = MaterialTheme.typography.bodySmall)
            TypographyItem(name = "Label Medium (12sp Medium)", text = sampleText, style = MaterialTheme.typography.labelMedium)
            TypographyItem(name = "Label Small / Overline (10sp Medium)", text = sampleText, style = MaterialTheme.typography.labelSmall)
        }

        Divider()

        // 2. COLOR PALETTE PREVIEW
        Text(
            text = "2. Color Palette Tokens",
            style = MaterialTheme.typography.titleLarge,
            color = Neutral950
        )

        Text(text = "Primary Scale (Royal Purple)", style = MaterialTheme.typography.titleMedium, color = Neutral700)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch(name = "Primary 700", color = Primary700, hex = "#7C3AED")
            ColorSwatch(name = "Primary 600", color = Primary600, hex = "#8B5CF6")
            ColorSwatch(name = "Primary 500", color = Primary500, hex = "#A855F7")
            ColorSwatch(name = "Primary 100", color = Primary100, hex = "#F3E8FF")
            ColorSwatch(name = "Primary 50", color = Primary50, hex = "#FAF5FF")
        }

        Text(text = "Semantic Colors", style = MaterialTheme.typography.titleMedium, color = Neutral700)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch(name = "Success 600", color = Success600, hex = "#16A34A")
            ColorSwatch(name = "Warning 600", color = Warning600, hex = "#CA8A04")
            ColorSwatch(name = "Error 600", color = Error600, hex = "#DC2626")
            ColorSwatch(name = "Info 600", color = Info600, hex = "#2563EB")
        }

        Text(text = "Neutral & Canvas", style = MaterialTheme.typography.titleMedium, color = Neutral700)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch(name = "Neutral 950", color = Neutral950, hex = "#0A0A0A")
            ColorSwatch(name = "Neutral 700", color = Neutral700, hex = "#404040")
            ColorSwatch(name = "Neutral 400", color = Neutral400, hex = "#A3A3A3")
            ColorSwatch(name = "Neutral 100", color = Neutral100, hex = "#F5F5F5")
            ColorSwatch(name = "Canvas Mobile", color = BgCanvasMobile, hex = "#FAFAFA")
        }

        Text(text = "Nebula Gradient", style = MaterialTheme.typography.titleMedium, color = Neutral700)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NebulaGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nebula Gradient CTA (#3B82F6 → #8B5CF6 → #EC4899)",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TypographyItem(name: String, text: String, style: androidx.compose.ui.text.TextStyle) {
    Column {
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = Neutral500)
        Text(text = text, style = style, color = Neutral950)
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color, hex: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = Neutral700)
        Text(text = hex, style = MaterialTheme.typography.labelSmall, color = Neutral500)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ThemePreview() {
    EduMindTheme {
        Surface {
            ThemePreviewContent()
        }
    }
}
