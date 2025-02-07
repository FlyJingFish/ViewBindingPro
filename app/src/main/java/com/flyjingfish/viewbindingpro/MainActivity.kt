package com.flyjingfish.viewbindingpro

import android.os.Bundle
import android.util.Log
import com.flyjingfish.viewbindingpro.databinding.ActivityMainBinding


class MainActivity:BaseVMActivity<ActivityMainBinding,ExampleViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("mViewModel", "mViewModel=$mViewModel")
    }
}