package com.flyjingfish.viewbindingpro_plugin.bean

data class BindingBean(
    val className:String,
    val fieldName:String,
    val position:Int,
    val methodName:String,
    val methodDesc:String,
    val bindingType:String,
    val isProtected:Boolean = false
)