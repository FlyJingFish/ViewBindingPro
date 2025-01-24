package com.flyjingfish.viewbindingpro_plugin

import com.flyjingfish.viewbindingpro_plugin.config.RootBooleanConfig
import com.flyjingfish.viewbindingpro_plugin.plugin.SearchCodePlugin
import com.flyjingfish.viewbindingpro_plugin.utils.printLog
import org.gradle.api.Plugin
import org.gradle.api.Project

class ViewBindingProPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val enableStr = project.properties[RootBooleanConfig.ENABLE.propertyName]?: RootBooleanConfig.ENABLE.defaultValue.toString()
        val enable = enableStr == "true"
        if (!enable){
            return
        }
        if (project.rootProject == project){
            deepSetDebugMode(project.rootProject)
        }
        SearchCodePlugin(false).apply(project)
    }

    private fun deepSetDebugMode(project: Project){
        val childProjects = project.childProjects
        if (childProjects.isEmpty()){
            return
        }
        childProjects.forEach { (_,value)->
            value.afterEvaluate {
                if (it.hasProperty("android")){
                    SearchCodePlugin(true).apply(it)
                }
            }
            deepSetDebugMode(value)
        }
    }
}