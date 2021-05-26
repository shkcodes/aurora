package com.shkcodes.aurora.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
abstract class BaseFragment<S, I> : Fragment() {

    abstract val viewModel: BaseViewModel<S, I>

    abstract val binding: ViewBinding

    open fun setupView() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeViewState()
        observeSideEffects()
        setupView()
    }

    private fun observeViewState() {
        viewModel.getState().flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::renderState)
            .launchIn(lifecycleScope)
    }

    private fun observeSideEffects() {
        viewModel.getSideEffects().flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleSideEffects)
            .launchIn(lifecycleScope)
    }

    fun dispatchIntent(intent: I) {
        viewModel.handleIntent(intent)
    }

    private fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is SideEffect.Action<*> -> handleAction(sideEffect)
            is SideEffect.DisplayScreen<*> -> handleNavigation(sideEffect)
        }
    }

    open fun handleAction(sideEffect: SideEffect.Action<*>) {}

    open fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {}

    open fun renderState(state: S) {}

    fun navigate(navDirections: NavDirections) {
        findNavController().navigate(navDirections)
    }
}
