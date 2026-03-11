package com.dush1729.cfseeker.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberRequestPermissionAndSync(
    onSync: () -> Unit,
    onDenied: () -> Unit
): () -> Unit
