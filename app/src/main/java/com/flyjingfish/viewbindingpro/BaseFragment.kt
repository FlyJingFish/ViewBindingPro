package com.flyjingfish.viewbindingpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.flyjingfish.viewbindingpro_core.BindViewBinding
import com.flyjingfish.viewbindingpro_core.BingType

abstract class BaseFragment<VB : ViewBinding>: Fragment() {
    @BindViewBinding(position = 0, methodName = "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)",  isProtected = false,bindingType = BingType.INFLATE_FALSE)
    protected lateinit var binding :VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
}