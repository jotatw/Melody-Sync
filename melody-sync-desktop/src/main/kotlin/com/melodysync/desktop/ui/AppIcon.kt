package com.melodysync.desktop.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

/**
 * Decodes the bundled application icon (`resources/icon.png`) into a Compose
 * bitmap. Used for the window icon and the About screen identity.
 */
fun loadAppIcon(): ImageBitmap {
    val stream = requireNotNull(AppIconLoader::class.java.getResourceAsStream("/icon.png")) {
        "Missing resources/icon.png"
    }
    val bytes = stream.use { it.readBytes() }
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

private object AppIconLoader