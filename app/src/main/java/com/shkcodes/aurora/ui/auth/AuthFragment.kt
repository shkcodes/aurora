package com.shkcodes.aurora.ui.auth

import androidx.fragment.app.viewModels
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentAuthBinding
import com.shkcodes.aurora.ui.auth.AuthContract.Intent
import com.shkcodes.aurora.ui.auth.AuthContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : BaseFragment<State, Intent>() {

    override val viewModel by viewModels<AuthViewModel>()

    override val binding by viewBinding(FragmentAuthBinding::inflate)
}
