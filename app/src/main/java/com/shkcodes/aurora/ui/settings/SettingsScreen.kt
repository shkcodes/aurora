package com.shkcodes.aurora.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.shkcodes.aurora.R
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent.ToggleAutoplayVideos

@Composable
fun SettingsScreen() {
    val viewModel = hiltNavGraphViewModel<SettingsViewModel>()

    val state = viewModel.composableState()

    Column {
        Row(modifier = Modifier
            .clickable {
                viewModel.handleIntent(ToggleAutoplayVideos)
            }
            .padding(Dimens.keyline_1),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.autoplay_videos),
                style = typography.subtitle2,
                modifier = Modifier.weight(1F)
            )
            Switch(checked = state.autoplayVideos, onCheckedChange = null)
        }
    }
}
