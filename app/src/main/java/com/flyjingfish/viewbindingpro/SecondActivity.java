package com.flyjingfish.viewbindingpro;

import android.util.Log;

import com.flyjingfish.viewbindingpro.databinding.ActivityMainBinding;

public class SecondActivity extends BaseVMActivity<ActivityMainBinding,ExampleViewModel>{

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("mViewModel", "mViewModel="+mViewModel);
    }
}
