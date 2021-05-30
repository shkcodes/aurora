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
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationGroup.isVisible = bottomNavScreens.contains(destination.id)
        }
    }
}
