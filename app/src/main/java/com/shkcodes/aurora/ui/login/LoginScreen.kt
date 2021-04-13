package com.shkcodes.aurora.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.theme.ThemedPreview
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.login.LoginContract.Intent.ShowAuthScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    LaunchedEffect(Unit) {
        launch {
            viewModel.getSideEffects().collect { handleActions(it, navController) }
        }
    }

    Content { viewModel.handleIntent(ShowAuthScreen) }
}

@Composable
fun Content(login: () -> Unit) {
    Scaffold {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.login_prompt),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(
                    vertical = Dimens.space_xxxlarge.dp,
                    horizontal = Dimens.keyline_1.dp
                )
            )
            Spacer(modifier = Modifier.weight(1F))
            Button(
                onClick = login,
                modifier = Modifier
                    .padding(Dimens.keyline_1.dp)
                    .align(Alignment.End),
            ) {
                Text(
                    text = stringResource(id = R.string.proceed).toUpperCase(Locale.ROOT),
                    style = MaterialTheme.typography.button
                )
            }
        }
    }
}

private fun handleActions(sideEffect: SideEffect, navController: NavController) {
    when (sideEffect) {
        is SideEffect.DisplayScreen<*> -> {
            navController.navigate((sideEffect.screen as Screen).name)
        }
    }
}

@Preview
@Composable
fun PreviewContent() {
    ThemedPreview {
        Content {}
    }
}
