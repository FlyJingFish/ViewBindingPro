package com.flyjingfish.viewbindingpro_core

/**
 * 为子类设置本注解以后，表示当前类不注入 [BindClass] 的相关设置
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class CancelBindClass