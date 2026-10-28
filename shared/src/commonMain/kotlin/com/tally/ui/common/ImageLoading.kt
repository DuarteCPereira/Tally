package com.tally.ui.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.Composable

@Composable
expect fun rememberImageBitmap(path: String?): ImageBitmap?
