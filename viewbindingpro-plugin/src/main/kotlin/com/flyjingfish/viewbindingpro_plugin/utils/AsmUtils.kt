package com.flyjingfish.viewbindingpro_plugin.utils

import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import com.flyjingfish.viewbindingpro_plugin.bean.BindingClassBean
import com.flyjingfish.viewbindingpro_plugin.tasks.SearchRegisterClassesTask
import com.flyjingfish.viewbindingpro_plugin.visitor.SearchClassScanner
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarFile

object AsmUtils {
    fun processFileForConfig(directory: File,file: File) {
        if (file.isFile) {
            val className = file.getFileClassname(directory)
            if (file.absolutePath.endsWith(_CLASS)) {
                if (!FileHashUtils.isScanFile(className)){
                    return
                }
                FileInputStream(file).use { inputs ->
                    val bytes = inputs.readAllBytes()
                    if (bytes.isNotEmpty()) {
                        val classReader = ClassReader(bytes)
                        classReader.accept(
                            SearchClassScanner(),
                            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                        )
                    }
                }
            }

        }
    }

    fun processJarForConfig(file: File) {
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            try {
                val entryName = jarEntry.name
                if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                    continue
                }
                if (!FileHashUtils.isScanFile(entryName)){
                    continue
                }
                if (entryName.endsWith(_CLASS)) {
                    jarFile.getInputStream(jarEntry).use { inputs ->
                        val bytes = inputs.readAllBytes()
                        if (bytes.isNotEmpty()) {
                            val classReader = ClassReader(bytes)
                            classReader.accept(
                                SearchClassScanner(),
                                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        jarFile.close()
    }


    fun addBindingCode(bindingBean:BindingBean,viewBindingClass:String,mv: MethodVisitor):Boolean{
        var isSetBindingInfo = false
        when (bindingBean.bindingType) {
            "INFLATE" -> {
                // 插入字节码：binding = ActivitySecondBinding.inflate(getLayoutInflater());
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/app/Activity", "getLayoutInflater", "()Landroid/view/LayoutInflater;", false); // 调用 getLayoutInflater()
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, viewBindingClass, "inflate", "(Landroid/view/LayoutInflater;)L$viewBindingClass;", false); // 调用 ActivitySecondBinding.inflate
                mv.visitFieldInsn(Opcodes.PUTFIELD, bindingBean.className, bindingBean.fieldName, "Landroidx/viewbinding/ViewBinding;"); // 将结果赋值给 binding 字段
                isSetBindingInfo = true
            }
            "INFLATE_FALSE" -> {
                // 插入字节码：binding = ActivityMainBinding.inflate(inflater, container, false);
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ALOAD, 1)
                mv.visitVarInsn(Opcodes.ALOAD, 2)
                mv.visitInsn(Opcodes.ICONST_0)
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    viewBindingClass,
                    "inflate",
                    "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Z)L$viewBindingClass;",
                    false)
                mv.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    bindingBean.className, bindingBean.fieldName,
                    "Landroidx/viewbinding/ViewBinding;")
                isSetBindingInfo = true
            }
            "INFLATE_TRUE" -> {
                // 插入字节码：binding = ActivityMainBinding.inflate(inflater, container, true);
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ALOAD, 1)
                mv.visitVarInsn(Opcodes.ALOAD, 2)
                mv.visitInsn(Opcodes.ICONST_1)    // 加载布尔值 true
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    viewBindingClass,
                    "inflate",
                    "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Z)L$viewBindingClass;",
                    false)
                mv.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    bindingBean.className, bindingBean.fieldName,
                    "Landroidx/viewbinding/ViewBinding;")
                isSetBindingInfo = true
            }
            "BIND" -> {
                // 插入字节码：binding = ActivityMainBinding.bind(view);
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitVarInsn(Opcodes.ALOAD, 1); // 加载 view 参数
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    viewBindingClass,
                    "bind",
                    "(Landroid/view/View;)L$viewBindingClass;",
                    false)
                mv.visitFieldInsn(
                    Opcodes.PUTFIELD,
                    bindingBean.className, bindingBean.fieldName,
                    "Landroidx/viewbinding/ViewBinding;")
                isSetBindingInfo = true
            }
        }
        return isSetBindingInfo
    }

    fun addBindingClassCode(bindingBean:BindingClassBean,viewBindingClass:String,mv: MethodVisitor):Boolean {
        try {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitLdcInsn(Type.getType("L" + dotToSlash(viewBindingClass) + ";"));
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                bindingBean.className,
                bindingBean.callMethodName,
                bindingBean.callMethodDesc,
                false)
            mv.visitTypeInsn(Opcodes.CHECKCAST, dotToSlash(viewBindingClass))

            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitInsn(Opcodes.SWAP);  // 交换栈顶，使 this 在前
            mv.visitFieldInsn(
                Opcodes.PUTFIELD,
                bindingBean.className, bindingBean.fieldName,
                "L"+ dotToSlash(bindingBean.baseClassName) +";")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }
}