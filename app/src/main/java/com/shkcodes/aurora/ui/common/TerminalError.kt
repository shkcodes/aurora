package com.shkcodes.aurora.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shkcodes.aurora.R
import com.shkcodes.aurora.theme.Dimens
import java.util.Locale

@Composable
fun TerminalError(message: String, action: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            Modifier.padding(Dimens.keyline_1.dp),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = action,
            modifier = Modifier.padding(Dimens.space.dp),
        ) {
            Text(
                text = stringResource(id = R.string.retry).toUpperCase(Locale.ROOT),
                style = MaterialTheme.typography.button
            )
        }
    }
}
