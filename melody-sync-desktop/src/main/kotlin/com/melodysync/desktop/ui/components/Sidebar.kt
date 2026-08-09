package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
    RailItem(Section.ORGANIZE, "Organize", Icons.Filled.FolderOpen),
)

private val footerItems = listOf(
    RailItem(Section.SETTINGS, "Settings", Icons.Filled.Settings),
    RailItem(Section.ABOUT, "About", Icons.Filled.Info),
)

@Composable
fun Sidebar(state: AppState) {
    val expanded = state.sidebarExpanded
    val railWidth = if (expanded) 200.dp else 80.dp

    NavigationRail(
        modifier = Modifier.fillMaxHeight().width(railWidth),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RailItemRow(item: RailItem, state: AppState, expanded: Boolean) {
    NavigationRailItem(
        selected = state.currentSection == item.section,
        onClick = { state.setSection(item.section) },
        icon = {
            if (expanded) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                )
            } else {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(item.label) } },
                    state = rememberTooltipState(),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                }
            }
        },
        label = if (expanded) {
            { Text(item.label) }
        } else {
            null
        },
    )
}
