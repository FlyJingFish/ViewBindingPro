package com.flyjingfish.viewbindingpro_core

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class BindClass(
    /**
     * 当前类的泛型第几个是ViewBinding
     */
    val position:Int = 0,
    /**
     * 要注入的方法名，包含参数类型和返回类型
     */
    val insertMethodName:String,

    val callMethodName:String,

    /**
     * 要注入的方法是否是 protected
     * true : protected
     * false : public
     */
    val isProtected:Boolean
)