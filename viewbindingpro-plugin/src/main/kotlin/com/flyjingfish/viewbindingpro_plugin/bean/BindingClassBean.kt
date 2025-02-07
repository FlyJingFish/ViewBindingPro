package com.flyjingfish.viewbindingpro_plugin.bean

data class BindingClassBean(
    val className:String,
    val baseClassName:String,
    val fieldName:String,
    val position:Int,
    val insertMethodName:String,
    val insertMethodDesc:String,
    val callMethodName:String,
    val callMethodDesc:String,
    val isProtected:Boolean = false
)