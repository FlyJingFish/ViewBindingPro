package com.flyjingfish.viewbindingpro

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.flyjingfish.viewbindingpro_core.BindClass
import com.flyjingfish.viewbindingpro_core.CancelBindClass
import com.flyjingfish.viewbindingpro_core.CancelBindViewBinding


@CancelBindClass
@CancelBindViewBinding
abstract class BaseVMActivity<VB :ViewBinding,VM: ViewModel>:BaseActivity<VB>() {
    @BindClass(position = 1, insertMethodName = "void onCreate(android.os.Bundle)", callMethodName = "androidx.lifecycle.ViewModel initViewModel(java.lang.Class)",isProtected = true)
    protected lateinit var mViewModel : VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    fun initViewModel(clazz: Class<out ViewModel>):ViewModel {
        return ViewModelProvider(this)[clazz]
    }
}