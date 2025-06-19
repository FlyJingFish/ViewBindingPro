package com.flyjingfish.viewbindingpro_plugin.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.flyjingfish.viewbindingpro_plugin.bean.VariantBean
import com.flyjingfish.viewbindingpro_plugin.tasks.SearchRegisterClassesTask
import com.flyjingfish.viewbindingpro_plugin.utils.adapterOSPath
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool
import java.io.File

/**
 * root 是指通过根目录build.gradle 设置的
 */
class SearchCodePlugin(private val fromRootSet: Boolean) : Plugin<Project> {
    companion object {
        const val ANDROID_EXTENSION_NAME = "android"
        private val kotlinCompileFilePathMap = mutableMapOf<String, File>()
    }

    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!fromRootSet && project.rootProject == project){
            project.rootProject.gradle.taskGraph.addTaskExecutionGraphListener(object :
                TaskExecutionGraphListener {
                override fun graphPopulated(it: TaskExecutionGraph) {
                    try {
                        for (task in it.allTasks) {
                            if (task is KotlinCompileTool){
                                val destinationDirectory = task.destinationDirectory.get().asFile
                                val key = task.project.buildDir.absolutePath+"@"+task.name
                                val oldDirectory = kotlinCompileFilePathMap[key]
                                if (oldDirectory == null || oldDirectory.absolutePath != destinationDirectory.absolutePath){
                                    kotlinCompileFilePathMap[key] = destinationDirectory
                                }
                            }
                        }
                    } catch (_: Throwable) {
                    }
                    project.rootProject.gradle.taskGraph.removeTaskExecutionGraphListener(this)
                }

            })
        }

        val isDynamicLibrary = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
        val androidObject: Any = project.extensions.findByName(ANDROID_EXTENSION_NAME) ?: return


        val kotlinCompileFilePathMap = mutableMapOf<String, Task>()
        val kotlinCompileVariantMap = mutableMapOf<String, VariantBean>()
        val android = androidObject as BaseExtension
        val variants = if (isApp or isDynamicLibrary) {
            (android as AppExtension).applicationVariants
        } else {
            (android as LibraryExtension).libraryVariants
        }
        try {
            project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
                kotlinCompileFilePathMap[task.name] = task
                task.doLast {
                    val variantBean = kotlinCompileVariantMap[it.name]
                    if (variantBean != null) {
                        doKotlinSearchTask(project, variantBean.variantName, task, isApp)
                    }
                }
            }
        } catch (_: Throwable) {
        }
        variants.all { variant ->

            val javaCompile: AbstractCompile =
                if (DefaultGroovyMethods.hasProperty(variant, "javaCompileProvider") != null) {
                    //gradle 4.10.1 +
                    variant.javaCompileProvider.get()
                } else if (DefaultGroovyMethods.hasProperty(variant, "javaCompiler") != null) {
                    variant.javaCompiler as AbstractCompile
                } else {
                    variant.javaCompile as AbstractCompile
                }
            val variantName = variant.name
            val buildTypeName = variant.buildType.name

            val kotlinBuildPath = File(project.buildDir.path + "/tmp/kotlin-classes/".adapterOSPath() + variantName)
            javaCompile.doLast {
                doSearchTask(project, variantName, javaCompile, kotlinBuildPath, isApp)
            }
        }
    }

    private fun doKotlinSearchTask(
        project: Project,
        variantName: String,
        kotlinCompile: KotlinCompileTool,
        isApp: Boolean
    ) {


        val localInput = mutableListOf<String>()
        val javaPath = kotlinCompile.destinationDirectory.get().asFile
        if (javaPath.exists()) {
            localInput.add(javaPath.absolutePath)
        }

        val jarInput = mutableListOf<String>()
        val bootJarPath = mutableSetOf<String>()
        for (file in localInput) {
            bootJarPath.add(file)
        }
        for (file in kotlinCompile.libraries) {
            if (file.absolutePath !in bootJarPath && file.exists()) {
                if (file.isDirectory) {
                    localInput.add(file.absolutePath)
                } else {
                    jarInput.add(file.absolutePath)
                }
            }
        }
        if (localInput.isNotEmpty()) {
            val task = SearchRegisterClassesTask(
                jarInput.map(::File),
                localInput.map(::File),
                project,
                variantName,
                isApp
            )
            task.taskAction()
        }
    }


    private fun doSearchTask(
        project: Project,
        variantName: String,
        javaCompile: AbstractCompile,
        kotlinDefaultPath: File,
        isApp: Boolean
    ) {
        val cacheDir = kotlinCompileFilePathMap[project.buildDir.absolutePath+"@"+"compile${variantName.capitalized()}Kotlin"]
        val kotlinPath = cacheDir
            ?:kotlinDefaultPath
        val localInput = mutableListOf<String>()
        val javaPath = File(javaCompile.destinationDirectory.asFile.orNull.toString())
        if (javaPath.exists()) {
            localInput.add(javaPath.absolutePath)
        }

        if (kotlinPath.exists()) {
            localInput.add(kotlinPath.absolutePath)
        }
        val jarInput = mutableListOf<String>()
        val bootJarPath = mutableSetOf<String>()
        for (file in localInput) {
            bootJarPath.add(file)
        }
        for (file in javaCompile.classpath) {
            if (file.absolutePath !in bootJarPath && file.exists()) {
                if (file.isDirectory) {
                    localInput.add(file.absolutePath)
                } else {
                    jarInput.add(file.absolutePath)
                }
            }
        }
        if (localInput.isNotEmpty()) {
            val task = SearchRegisterClassesTask(
                jarInput.map(::File),
                localInput.map(::File),
                project,
                variantName,
                isApp
            )
            task.taskAction()
        }
    }

}