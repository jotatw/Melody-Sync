package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.desktop.ui.window.LocalWindowSizeClass

@Composable
fun TopBar(
    state: AppState,
    theme: AppTheme,
    onToggleTheme: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = state::toggleSidebar) {
                Icon(
                    imageVector = if (state.sidebarExpanded) Icons.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = if (state.sidebarExpanded) "Collapse sidebar" else "Expand sidebar",
                )
            }
            // Brand mark uses the editorial serif (DesignSystem §3).
            // Hidden in compact windows so the controls keep usable width.
            val sizeClass = LocalWindowSizeClass.current
            if (!sizeClass.isCompact) {
                Text(
                    "Melody Sync",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (theme == AppTheme.DARK) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (theme == AppTheme.DARK) "Switch to light theme" else "Switch to dark theme",
            )
        }
    }
}
