package com.shkcodes.aurora.ui.profile

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ProfileScreen(userHandle: String) {
    Text(text = "Hello $userHandle")
}
