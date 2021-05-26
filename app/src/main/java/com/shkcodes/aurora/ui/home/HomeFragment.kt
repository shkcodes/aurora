package com.shkcodes.aurora.ui.home

import androidx.fragment.app.viewModels
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentHomeBinding
import com.shkcodes.aurora.ui.home.HomeContract.Intent
import com.shkcodes.aurora.ui.home.HomeContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<State, Intent>() {

    override val viewModel by viewModels<HomeViewModel>()

    override val binding by viewBinding(FragmentHomeBinding::inflate)
}
