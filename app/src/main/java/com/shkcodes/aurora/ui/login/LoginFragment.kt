package com.shkcodes.aurora.ui.login

import androidx.fragment.app.viewModels
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentLoginBinding
import com.shkcodes.aurora.ui.login.LoginContract.Intent
import com.shkcodes.aurora.ui.login.LoginContract.State
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<State, Intent>() {

    override val viewModel by viewModels<LoginViewModel>()

    override val binding by viewBinding(FragmentLoginBinding::inflate)
}
