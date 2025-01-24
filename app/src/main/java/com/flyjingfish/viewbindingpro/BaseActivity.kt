package com.flyjingfish.viewbindingpro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.flyjingfish.viewbindingpro_core.BindViewBinding
import com.flyjingfish.viewbindingpro_core.BingType


abstract class BaseActivity<VB :ViewBinding>:AppCompatActivity() {
    @BindViewBinding(position = 0, methodName = "void onCreate(android.os.Bundle)", isProtected = true, bindingType = BingType.INFLATE)
    protected lateinit var binding :VB


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setBinding()
        setContentView(binding.root)
    }


//    protected fun setBinding() {
//        binding = ViewBindingPro.inflate(layoutInflater) as VB
//        setContentView(binding.root)
//    }




}