package com.pronaycoding.blankee.core.ui.components

/**
 * Section title card component for sound categories.
 *
 * This file provides composable functions for displaying section headers in the sound list.
 * Features:
 * - Category title display with primary color
 * - Horizontal divider line for visual separation
 * - Proper spacing and typography
 *
 * Used to group sounds by category (Nature, Travel, Interiors, etc.)
 *
 * @see androidx.compose.material3.Text for text rendering
 * @see androidx.compose.material3.HorizontalDivider for divider rendering
 */

import com.pronaycoding.blankee.core.ui.theme.LocalAccentColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TitleCardView(text: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp, start = 4.dp),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
    }
}

// @Preview (showBackground = true)
@Composable
fun TitleCardView(
    modifier: Modifier = Modifier,
    typeText: String,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 8.dp)
                .padding(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
    ) {
        Text(
            text = typeText,
            color = LocalAccentColor.current,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        HorizontalDivider()
    }
}

@Composable
@Preview(showSystemUi = true)
fun Preview(modifier: Modifier = Modifier) {
    TitleCardView(text = "Title")
}
