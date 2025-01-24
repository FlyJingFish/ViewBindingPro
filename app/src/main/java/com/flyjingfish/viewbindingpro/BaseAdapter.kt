package com.flyjingfish.viewbindingpro

import android.view.View
import androidx.viewbinding.ViewBinding
import com.flyjingfish.viewbindingpro_core.BindViewBinding
import com.flyjingfish.viewbindingpro_core.BingType

abstract class BaseAdapter<VB : ViewBinding> {
    @BindViewBinding(position = 0, methodName = "android.view.View bindView(android.view.View)", isProtected = false, bindingType = BingType.BIND)
    protected lateinit var binding :VB

    open fun bindView(view: View): View{
        return binding.root
    }
}