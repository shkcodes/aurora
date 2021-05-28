package com.shkcodes.aurora.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shkcodes.aurora.databinding.FragmentTempBinding

class TempFragment : Fragment() {

    private val binding by viewBinding(FragmentTempBinding::inflate)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}
