package com.shkcodes.aurora.util

import android.app.Activity
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ActivityBindingDelegate<T : ViewBinding>(
    val bindingFactory: (LayoutInflater) -> T
) : ReadOnlyProperty<Activity, T> {

    private var value: T? = null

    override fun getValue(thisRef: Activity, property: KProperty<*>): T {
        if (value == null) {
            value = bindingFactory(thisRef.layoutInflater)
        }
        return value!!
    }
}

fun <T : ViewBinding> viewBinding(bindingInflater: (LayoutInflater) -> T) =
    ActivityBindingDelegate(bindingInflater)
