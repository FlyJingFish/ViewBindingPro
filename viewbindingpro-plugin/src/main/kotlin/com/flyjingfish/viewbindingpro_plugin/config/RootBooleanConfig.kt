package com.flyjingfish.viewbindingpro_plugin.config

enum class RootBooleanConfig(
    val propertyName: String,
    val defaultValue: Boolean,
) {
    /**
     * 是否启动当前插件
     */
    ENABLE("viewbindingpro.enable", true)
}