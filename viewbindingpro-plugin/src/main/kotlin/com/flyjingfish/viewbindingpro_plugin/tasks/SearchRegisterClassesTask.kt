package com.flyjingfish.viewbindingpro_plugin.tasks

import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import com.flyjingfish.viewbindingpro_plugin.bean.BindingClassBean
import com.flyjingfish.viewbindingpro_plugin.utils.AsmUtils
import com.flyjingfish.viewbindingpro_plugin.utils.Joined
import com.flyjingfish.viewbindingpro_plugin.utils.checkExist
import com.flyjingfish.viewbindingpro_plugin.utils.getRelativePath
import com.flyjingfish.viewbindingpro_plugin.utils.registerCompileTempDir
import com.flyjingfish.viewbindingpro_plugin.utils.saveEntry
import com.flyjingfish.viewbindingpro_plugin.utils.saveFile
import com.flyjingfish.viewbindingpro_plugin.visitor.SearchClassScanner
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
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
        val wovenCodeJobs = mutableListOf<Deferred<Any>>()
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
                                    var bindingClassBean : BindingClassBean ?= null
                                    var bindingClassname : String ?= null
                                    cr.accept(
                                        SearchClassScanner(cw,object :SearchClassScanner.OnBackNotWovenMethod{
                                            override fun onBack(
                                                bindingInfo: BindingBean,
                                                superName: String?,
                                                viewBindingClass: String
                                            ) {
                                                bindingBean = bindingInfo
                                                superClassname = superName
                                                viewBindingClassname = viewBindingClass
                                            }

                                            override fun onBack(
                                                bindingInfo: BindingClassBean,
                                                superName: String?,
                                                bindingClass: String
                                            ) {
                                                bindingClassBean = bindingInfo
                                                superClassname = superName
                                                bindingClassname = bindingClass
                                            }

                                        }
                                        ) ,
                                        0
                                    )
                                    val bindingInfo = bindingBean
                                    val viewBindingClass = viewBindingClassname
                                    val bindClassInfo = bindingClassBean
                                    val bindingClass = bindingClassname
                                    val isSingle = bindingInfo != null && bindClassInfo != null
                                            && bindingInfo.methodName == bindClassInfo.insertMethodName
                                            && bindingInfo.methodDesc == bindClassInfo.insertMethodDesc
                                    var mv:MethodVisitor ?= null
                                    if (bindingInfo != null && viewBindingClass != null){
                                        mv = wovenMethodCode(bindingInfo,viewBindingClass,cw,superClassname!!, bindingInfo.methodName,bindingInfo.methodName,bindingInfo.methodDesc,if (bindingInfo.isProtected) Opcodes.ACC_PROTECTED else Opcodes.ACC_PUBLIC,!isSingle)
                                    }


                                    if (bindClassInfo != null && bindingClass != null){
                                        mv = wovenMethodCode(mv,bindClassInfo,bindingClass,cw,superClassname!!, bindClassInfo.insertMethodName,bindClassInfo.insertMethodName,bindClassInfo.insertMethodDesc,if (bindClassInfo.isProtected) Opcodes.ACC_PROTECTED else Opcodes.ACC_PUBLIC,!isSingle)
                                    }

                                    if (isSingle && mv != null && bindClassInfo != null){
                                        wovenMethodCodeEnd(mv,superClassname!!, bindClassInfo.insertMethodName,bindClassInfo.insertMethodDesc)
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

        val tmpCompileDir = File(registerCompileTempDir(project,variantName))
        tmpCompileDir.deleteRecursively()
    }


    private fun wovenMethodCode(bindingBean : BindingBean,viewBindingClass:String,cw: ClassWriter, superClassName:String, superMethodName:String, methodName:String, methodDescriptor:String, methodAccess:Int,isEnd:Boolean):MethodVisitor{
        val mv = cw.visitMethod(methodAccess, methodName, methodDescriptor, null, null)
        mv.visitCode()
        val av = mv.visitAnnotation(Joined, false)
        av.visitEnd()
        AsmUtils.addBindingCode(bindingBean,viewBindingClass, mv)
        // 调用 super.someMethod() 的字节码指令
        if (isEnd){
            wovenMethodCodeEnd(mv, superClassName, superMethodName, methodDescriptor)
        }
        return mv
    }

    private fun wovenMethodCode(oldMv: MethodVisitor?,bindingBean : BindingClassBean,viewBindingClass:String,cw: ClassWriter, superClassName:String, superMethodName:String, methodName:String, methodDescriptor:String, methodAccess:Int,isEnd:Boolean):MethodVisitor{
        val mv = oldMv ?: cw.visitMethod(methodAccess, methodName, methodDescriptor, null, null)
        if (oldMv == null){
            mv.visitCode()
            val av = mv.visitAnnotation(Joined, false)
            av.visitEnd()
        }
        AsmUtils.addBindingClassCode(bindingBean,viewBindingClass, mv)
        // 调用 super.someMethod() 的字节码指令
        if (isEnd){
            wovenMethodCodeEnd(mv, superClassName, superMethodName, methodDescriptor)
        }
        return mv
    }

    private fun wovenMethodCodeEnd(mv: MethodVisitor, superClassName:String, superMethodName:String,methodDescriptor:String){
        // 调用 super.someMethod() 的字节码指令
        mv.visitVarInsn(Opcodes.ALOAD, 0) // 加载 `this` 引用到操作数栈
        var localVarIndex = 1
        var maxStack = 1
        val argTypes = Type.getArgumentTypes(methodDescriptor)
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
        val isVoid = Type.getReturnType(methodDescriptor).className == "void"
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