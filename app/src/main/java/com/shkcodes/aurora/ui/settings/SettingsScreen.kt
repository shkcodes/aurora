package com.shkcodes.aurora.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.shkcodes.aurora.R
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.settings.SettingsContract.Intent.ToggleAutoplayVideos

@Composable
fun SettingsScreen() {
    val viewModel = hiltNavGraphViewModel<SettingsViewModel>()

    val state = viewModel.getState().collectAsState().value

    Column(modifier = Modifier.padding(Dimens.keyline_1)) {
        Row {
            Text(
                text = stringResource(id = R.string.autoplay_videos),
                style = typography.subtitle2,
                modifier = Modifier.weight(1F)
            )
            Switch(
                checked = state.autoplayVideos,
                onCheckedChange = {
                    viewModel.handleIntent(ToggleAutoplayVideos)
                })
        }
    }
}
