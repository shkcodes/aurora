package com.shkcodes.aurora.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<S, I> : Fragment(), ViewModelOwner<S, I> {

    abstract val binding: ViewBinding

    override val screenLifecycle: Lifecycle
        get() = viewLifecycleOwner.lifecycle

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

    fun navigate(navDirections: NavDirections, extras: Navigator.Extras? = null) {
        if (extras == null) {
            findNavController().navigate(navDirections)
        } else {
            findNavController().navigate(navDirections, extras)
        }
    }
}
