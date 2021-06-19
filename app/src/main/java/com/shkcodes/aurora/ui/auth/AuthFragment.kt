package com.shkcodes.aurora.ui.auth

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentAuthBinding
import com.shkcodes.aurora.ui.Screen.Home
import com.shkcodes.aurora.ui.auth.AuthContract.Intent
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.RequestAccessToken
import com.shkcodes.aurora.ui.auth.AuthContract.Intent.Retry
import com.shkcodes.aurora.ui.auth.AuthContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : BaseFragment<State, Intent>() {

    override val viewModel by viewModels<AuthViewModel>()

    override val binding by viewBinding(FragmentAuthBinding::inflate)

    override fun setupView() {
        with(binding) {
            webView.webViewClient = AuthWebViewClient {
                dispatchIntent(RequestAccessToken(it))
            }
            retryButton.setOnClickListener {
                dispatchIntent(Retry)
            }
        }
    }

    override fun renderState(state: State) {
        with(binding) {
            progressBar.isVisible = state is State.Loading
            errorMessage.isVisible = state is State.Error
            retryButton.isVisible = state is State.Error
            webView.isVisible = state is State.RequestToken
            (state as? State.Error)?.let {
                errorMessage.text = it.message
            }
            (state as? State.RequestToken)?.let {
                webView.loadUrl(it.authorizationUrl)
            }
        }
    }

    override fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {
        when (sideEffect.screen) {
            Home -> {
                navigate(AuthFragmentDirections.moveToHome())
            }
        }
    }
}
