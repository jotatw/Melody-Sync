package com.melodysync.desktop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.Section

private data class SidebarItem(
    val section: Section,
    val label: String,
    val icon: ImageVector,
)

private val items = listOf(
    SidebarItem(Section.LIBRARY, "Library", Icons.Filled.LibraryMusic),
    SidebarItem(Section.STATISTICS, "Statistics", Icons.Filled.Insights),
    SidebarItem(Section.HEALTH, "Health", Icons.Filled.HealthAndSafety),
    SidebarItem(Section.DUPLICATES, "Duplicates", Icons.Filled.Repeat),
    SidebarItem(Section.ORGANIZE, "Organize", Icons.Filled.FolderOpen),
)

@Composable
fun Sidebar(state: AppState) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEach { item ->
            SidebarItemRow(
                item = item,
                selected = state.currentSection == item.section,
                onClick = { state.setSection(item.section) },
            )
        }
    }
}

@Composable
private fun SidebarItemRow(
    item: SidebarItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
            )
            Text(
                item.label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
        }
    }
}