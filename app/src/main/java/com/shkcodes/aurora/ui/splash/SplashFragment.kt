package com.shkcodes.aurora.ui.splash

import android.graphics.Paint
import androidx.fragment.app.viewModels
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentSplashBinding
import com.shkcodes.aurora.ui.Screen2.Home
import com.shkcodes.aurora.ui.Screen2.Login
import com.shkcodes.aurora.ui.splash.SplashContract.Intent
import com.shkcodes.aurora.ui.splash.SplashContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<State, Intent>() {

    override val binding by viewBinding(FragmentSplashBinding::inflate)

    override val viewModel by viewModels<SplashViewModel>()

    override fun setupView() {
        with(binding.name) {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    override fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {
        when (sideEffect.screen) {
            Login -> {
                navigate(SplashFragmentDirections.moveToLogin())
            }
            Home -> {
                navigate(SplashFragmentDirections.moveToHome())
            }
        }
    }
}
