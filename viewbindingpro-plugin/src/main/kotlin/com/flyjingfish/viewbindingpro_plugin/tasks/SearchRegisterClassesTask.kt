package com.flyjingfish.viewbindingpro_plugin.tasks

import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import com.flyjingfish.viewbindingpro_plugin.utils.AsmUtils
import com.flyjingfish.viewbindingpro_plugin.utils.checkExist
import com.flyjingfish.viewbindingpro_plugin.utils.getRelativePath
import com.flyjingfish.viewbindingpro_plugin.utils.registerCompileTempDir
import com.flyjingfish.viewbindingpro_plugin.utils.saveEntry
import com.flyjingfish.viewbindingpro_plugin.utils.saveFile
import com.flyjingfish.viewbindingpro_plugin.visitor.MethodParamNamesScanner
import com.flyjingfish.viewbindingpro_plugin.visitor.SearchClassScanner
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.FileInputStream
import kotlin.system.measureTimeMillis

class SearchRegisterClassesTask(
    private val allJars: MutableList<File>,
    private val allDirectories: MutableList<File>,
    private val output: File,
    private val project: Project,
    private val isApp:Boolean,
    private val variantName:String,
    private val isJava:Boolean = true
) {
    companion object{
        const val _CLASS = ".class"

    }

    fun taskAction() {
        println("ViewBindingPro:search code start")
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        println("ViewBindingPro:search code finish, current cost time ${scanTimeCost}ms")

    }

    private fun scanFile() = runBlocking {
        searchJoinPointLocation()
        wovenCode()


    }



    private fun searchJoinPointLocation(){
        //第一遍找配置文件
        allDirectories.forEach { directory ->
            directory.walk().forEach { file ->
                AsmUtils.processFileForConfig(project,file)
            }

        }

        allJars.forEach { file ->
            AsmUtils.processJarForConfig(file)
        }
    }


    private fun wovenCode() = runBlocking{
        val wovenCodeJobs = mutableListOf<Deferred<Unit>>()
        allDirectories.forEach { directory ->
            directory.walk().forEach { file ->
                if (file.isFile) {
                    if (file.absolutePath.endsWith(_CLASS)) {
                        val job = async(Dispatchers.IO) {
                            FileInputStream(file).use { inputs ->
                                val bytes = inputs.readAllBytes()
                                if (bytes.isNotEmpty()) {
                                    val relativePath = file.getRelativePath(directory)
                                    val tmpCompileDir = registerCompileTempDir(project,variantName)
                                    val outFile = File(tmpCompileDir+File.separatorChar+relativePath)
                                    outFile.checkExist()
                                    val cr = ClassReader(bytes)
                                    val cw = ClassWriter(cr,0)
                                    var bindingBean : BindingBean ?= null
                                    var superClassname : String ?= null
                                    var viewBindingClassname : String ?= null
                                    cr.accept(
                                        SearchClassScanner(cw
                                        ) { bindingInfo,superClass,viewBindingClass ->
                                            bindingBean = bindingInfo
                                            superClassname = superClass
                                            viewBindingClassname = viewBindingClass
                                        },
                                        0
                                    )
                                    val bindingInfo = bindingBean
                                    val viewBindingClass = viewBindingClassname
                                    if (bindingInfo != null && viewBindingClass != null){
                                        wovenMethodCode(bindingInfo,viewBindingClass,cw,superClassname!!, bindingInfo.methodName,bindingInfo.methodName,bindingInfo.methodDesc,if (bindingInfo.isProtected) Opcodes.ACC_PROTECTED else Opcodes.ACC_PUBLIC)
                                    }

                                    cw.toByteArray().saveFile(outFile)
                                    outFile.inputStream().use {
                                        file.saveEntry(it)
                                    }


                                }
                            }
                        }
                        wovenCodeJobs.add(job)

                    }

                }
            }

        }
        wovenCodeJobs.awaitAll()
    }


    private fun wovenMethodCode(bindingBean : BindingBean,viewBindingClass:String,cw: ClassWriter, superClassName:String, superMethodName:String, methodName:String, methodDescriptor:String, methodAccess:Int){
        val mv = cw.visitMethod(methodAccess, methodName, methodDescriptor, null, null)
        val isVoid = Type.getReturnType(methodDescriptor).className == "void"
        val argTypes = Type.getArgumentTypes(methodDescriptor)
        mv.visitCode()

        AsmUtils.addBindingCode(bindingBean,viewBindingClass, mv)
        // 调用 super.someMethod() 的字节码指令
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 加载 `this` 引用到操作数栈
        var localVarIndex = 1
        var maxStack = 1
        for (argType in argTypes) {
            maxStack += when (argType.sort) {
                Type.LONG , Type.DOUBLE -> {
                    2
                }
                else -> {
                    1
                }
            }
            when (argType.sort) {
                Type.INT -> mv.visitVarInsn(Opcodes.ILOAD, localVarIndex) // 加载 int 类型参数
                Type.LONG -> mv.visitVarInsn(Opcodes.LLOAD, localVarIndex) // 加载 long 类型参数
                Type.FLOAT -> mv.visitVarInsn(Opcodes.FLOAD, localVarIndex) // 加载 float 类型参数
                Type.DOUBLE -> mv.visitVarInsn(Opcodes.DLOAD, localVarIndex) // 加载 double 类型参数
                Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT -> mv.visitVarInsn(
                    Opcodes.ILOAD,
                    localVarIndex
                ) // 加载 boolean, byte, char, short，这些类型都使用 ILOAD
                Type.OBJECT, Type.ARRAY -> mv.visitVarInsn(
                    Opcodes.ALOAD,
                    localVarIndex
                ) // 加载引用类型（如 Object 和数组）
                else -> throw IllegalArgumentException("Unsupported parameter type: $argType")
            }
            localVarIndex += argType.size // 更新下一个局部变量的索引

        }

        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            superClassName,
            superMethodName,
            methodDescriptor,
            false
        ) // 调用父类的 someMethod()
        if (isVoid) {
            mv.visitInsn(Opcodes.RETURN) // 返回
        } else {
            mv.visitInsn(Opcodes.IRETURN) // 返回
        }
        mv.visitMaxs(maxStack, localVarIndex) // 设置操作数栈和局部变量表的大小
        mv.visitEnd()

    }
}