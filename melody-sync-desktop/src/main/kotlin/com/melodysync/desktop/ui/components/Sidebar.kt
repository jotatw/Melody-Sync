package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

private val items = listOf(
    RailItem(Section.LIBRARY, "Library", Icons.Filled.LibraryMusic),
    RailItem(Section.STATISTICS, "Statistics", Icons.Filled.Insights),
    RailItem(Section.HEALTH, "Health", Icons.Filled.HealthAndSafety),
    RailItem(Section.DUPLICATES, "Duplicates", Icons.Filled.Repeat),
    RailItem(Section.ORGANIZE, "Organize", Icons.Filled.FolderOpen),
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
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEach { item ->
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
        }
    }
}
