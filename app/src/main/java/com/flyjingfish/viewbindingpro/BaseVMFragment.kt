package com.flyjingfish.viewbindingpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.flyjingfish.viewbindingpro_core.BindClass


abstract class BaseVMFragment<VB :ViewBinding,VM: ViewModel>:BaseFragment<VB>() {
    @BindClass(position = 1, insertMethodName = "android.view.View onCreateView(android.view.LayoutInflater,android.view.ViewGroup,android.os.Bundle)", callMethodName = "androidx.lifecycle.ViewModel initViewModel(java.lang.Class)",isProtected = false)
    protected lateinit var mViewModel : VM

    protected fun initViewModel(clazz: Class<out ViewModel>):ViewModel {
        return ViewModelProvider(this)[clazz]
    }
}