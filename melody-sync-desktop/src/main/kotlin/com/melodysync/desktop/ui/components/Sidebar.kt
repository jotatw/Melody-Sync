package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.Section

private data class RailItem(
    val section: Section,
    val label: String,
    val icon: ImageVector,
)

private val mainItems = listOf(
    RailItem(Section.LIBRARY, "Library", Icons.Filled.LibraryMusic),
    RailItem(Section.STATISTICS, "Statistics", Icons.Filled.Insights),
    RailItem(Section.HEALTH, "Health", Icons.Filled.HealthAndSafety),
    RailItem(Section.DUPLICATES, "Duplicates", Icons.Filled.Repeat),
    RailItem(Section.ORGANIZE, "Organize", Icons.Filled.FolderOpen),
)

private val footerItems = listOf(
    RailItem(Section.SETTINGS, "Settings", Icons.Filled.Settings),
    RailItem(Section.ABOUT, "About", Icons.Filled.Info),
)

@Composable
fun Sidebar(state: AppState) {
    val expanded = state.sidebarExpanded

    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        header = {
            if (expanded) {
                Text(
                    "Sections",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            mainItems.forEach { item ->
                RailItemRow(item = item, state = state, expanded = expanded)
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

            footerItems.forEach { item ->
                RailItemRow(item = item, state = state, expanded = expanded)
            }
        }
    }
}

@Composable
private fun RailItemRow(item: RailItem, state: AppState, expanded: Boolean) {
    NavigationRailItem(
        selected = state.currentSection == item.section,
        onClick = { state.setSection(item.section) },
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
            )
        },
        label = if (expanded) {
            { Text(item.label) }
        } else {
            null
        },
    )
}
