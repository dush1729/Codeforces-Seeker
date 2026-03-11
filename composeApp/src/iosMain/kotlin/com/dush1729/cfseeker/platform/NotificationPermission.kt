package com.dush1729.cfseeker.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberRequestPermissionAndSync(
    onSync: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    return { onSync() }
}
