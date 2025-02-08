package com.flyjingfish.viewbindingpro

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.flyjingfish.viewbindingpro.databinding.ActivityMainBinding

class MainActivity:BaseVMActivity<ActivityMainBinding,ExampleViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("mViewModel", "mViewModel=$mViewModel")
        binding.btnSingleClick.setOnClickListener {
            val packageManager = application.packageManager
            @SuppressLint("QueryPermissionsNeeded") val list = packageManager.getInstalledPackages(0)
            for (i in list.indices) {
                Log.e("packageInfo", "info=${list[i]}")
            }
        }

    }
}