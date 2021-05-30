package com.shkcodes.aurora.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.BaseActivity
import com.shkcodes.aurora.databinding.ActivityMainBinding
import com.shkcodes.aurora.ui.bottomNavScreens
import com.shkcodes.aurora.ui.main.MainContract.Intent
import com.shkcodes.aurora.ui.main.MainContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<State, Intent>() {

    override val viewModel by viewModels<MainViewModel>()

    override val binding by viewBinding(ActivityMainBinding::inflate)

    override fun setUpActivity(savedInstanceState: Bundle?) {
        with(binding) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            val navController = navHostFragment.navController
            bottomNav.setupWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                bottomNavigationGroup.isVisible = bottomNavScreens.contains(destination.id)
            }
        }
    }

    override fun renderState(state: State) {
        with(binding) {
            progressBarBackground.animate().alpha(if (state.isLoading) 1F else 0F).start()
            bottomNavIndicator.animate().scaleY(if (state.isLoading) 0F else 1F).start()
            progressBar.pivotY = progressBar.height.toFloat()
            progressBar.animate().scaleY(if (state.isLoading) 1F else 0F).start()
        }
    }
}
