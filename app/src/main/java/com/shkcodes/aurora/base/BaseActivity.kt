package com.shkcodes.aurora.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<S, I> : AppCompatActivity(), ViewModelOwner<S, I> {

    abstract val binding: ViewBinding

    override val screenLifecycle: Lifecycle
        get() = lifecycle

    open fun setUpActivity(savedInstanceState: Bundle?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        observeViewState()
        observeSideEffects()

        setUpActivity(savedInstanceState)
    }
}
