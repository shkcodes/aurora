package com.shkcodes.aurora.ui.profile

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentProfileBinding
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent
import com.shkcodes.aurora.ui.profile.ProfileContract.State
import com.shkcodes.aurora.util.applySharedAxisEnterTransition
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<State, Intent>() {

    private val args by navArgs<ProfileFragmentArgs>()

    override val viewModel by viewModels<ProfileViewModel>()

    override val binding by viewBinding(FragmentProfileBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySharedAxisEnterTransition()
    }

    override fun setupView() {
        binding.name.text = args.userHandle
    }
}
