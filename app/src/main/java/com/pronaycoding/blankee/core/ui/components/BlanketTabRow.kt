package com.pronaycoding.blankee.core.ui.components

/**
 * Tab row component for navigating between app sections.
 *
 * This composable provides a Material Design tab row for switching between:
 * - Home section (sound controls and presets)
 * - Settings section (preferences and options)
 *
 * Features:
 * - Smooth tab transitions
 * - Customizable styling and colors
 * - State management for selected tab
 *
 * @see androidx.compose.material3.TabRow for Material Design tab row documentation
 */

import com.pronaycoding.blankee.core.ui.theme.LocalAccentColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pronaycoding.blankee.R

@Composable
fun BlanketTabRow(modifier: Modifier = Modifier) {
    val titles =
        listOf(
            stringResource(R.string.tab_home),
            stringResource(R.string.tab_settings),
        )
    var state by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        TabRow(
            selectedTabIndex = state,
            divider = {},
            indicator = {
            },
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selectedContentColor = LocalAccentColor.current.copy(alpha = .5f),
                    selected = (index == state),
                    onClick = { state = index },
                    text = {
                        Text(
                            text = title,
                            color =
                                if (state == index) {
                                    LocalAccentColor.current
                                } else {
                                    MaterialTheme.colorScheme.inverseSurface.copy(
                                        alpha = .7f,
                                    )
                                },
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBlanketTabRow() {
    BlanketTabRow()
}
