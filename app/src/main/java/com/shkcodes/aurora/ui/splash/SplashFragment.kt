package com.shkcodes.aurora.ui.splash

import androidx.fragment.app.viewModels
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentSplashBinding
import com.shkcodes.aurora.ui.splash.SplashContract.Intent
import com.shkcodes.aurora.ui.splash.SplashContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<State, Intent>() {

    override val binding by viewBinding(FragmentSplashBinding::inflate)

    override val viewModel by viewModels<SplashViewModel>()
}
