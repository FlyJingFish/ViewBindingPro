package com.flyjingfish.viewbindingpro_plugin.visitor

import androidx.viewbinding.ViewBinding
import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import com.flyjingfish.viewbindingpro_plugin.utils.AsmUtils
import com.flyjingfish.viewbindingpro_plugin.utils.BindingUtils
import com.flyjingfish.viewbindingpro_plugin.utils.Joined
import com.flyjingfish.viewbindingpro_plugin.utils.slashToDot
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.Method
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor

class SearchClassScanner(classVisitor: ClassVisitor? = null,private val onBackNotWovenMethod: OnBackNotWovenMethod?=null) : ClassVisitor(Opcodes.ASM9,classVisitor) {
    private lateinit var className:String
    private var bindingInfo: BindingBean ?= null
    private var superName: String? =null
    private var isSetBindingInfo: Boolean = false
    private lateinit var viewBindingClass: String
    fun interface OnBackNotWovenMethod{
        fun onBack(bindingInfo: BindingBean,superName: String?,viewBindingClass:String)
    }
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        this.superName = superName
        val bindingBean = BindingUtils.isExtendBaseClass(superName)
        if (superName != null && bindingBean != null){
            parseGenericSignature(signature,bindingBean)
        }
        interfaces?.let {
            var isViewBinding = false
            for (s in it) {
                if (slashToDot(s) == ViewBinding::class.java.name){
                    isViewBinding = true
                    break
                }
            }
            if (isViewBinding){
                BindingUtils.addViewBindingClass(name)
            }
        }
    }

    private fun parseGenericSignature(signature: String?,bindingBean: BindingBean) {
        // 使用 ASM 提供的 SignatureReader 解析签名
        val signatureReader = SignatureReader(signature)
        var isRegister = false
        signatureReader.accept(object : SignatureVisitor(Opcodes.ASM9) {
            private var index = 0
            override fun visitClassType(name: String?) {
                if (index >0 && name != null && BindingUtils.isViewBindingClass(name) && bindingBean.position == index-1){
                    isRegister = true
                    bindingInfo = bindingBean
                    viewBindingClass = name
                }
                super.visitClassType(name)
                index++
            }

        })
        if (!isRegister){
//            BindingUtils.addBindingInfo4Extends(className,BindingBean(className,bindingBean.fieldName,bindingBean.position,bindingBean.methodName,bindingBean.methodDesc,bindingBean.bindingType))
            BindingUtils.addBindingInfo4Extends(className,bindingBean)
        }
    }


    override fun visitMethod(
        access: Int, name: String, descriptor: String,
        signature: String?, exceptions: Array<String?>?
    ): MethodVisitor? {
        val bindingBean = bindingInfo
        return if (bindingBean != null && bindingBean.methodName == name && bindingBean.methodDesc == descriptor){
            MyMethodVisitor(
                super.visitMethod(access, name, descriptor, signature, exceptions)
            )
        }else{
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }

    }

    override fun visitEnd() {
        super.visitEnd()
        val bindingBean = bindingInfo
        if (!isSetBindingInfo && bindingBean != null && ::viewBindingClass.isInitialized){
            onBackNotWovenMethod?.onBack(bindingBean,superName,viewBindingClass)
        }

    }


    open inner class MyMethodVisitor(
        methodVisitor: MethodVisitor?
    ) : MethodVisitor(
        Opcodes.ASM9,methodVisitor
    ) {
        private var isJoined = false
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            // 检查是否已有目标注解
            if (Joined == descriptor) {
                isJoined = true // 标记为已存在
            }
            return super.visitAnnotation(descriptor, visible)
        }

        override fun visitCode() {
            val bindingBean = bindingInfo
            if (!isJoined && bindingBean != null){
                val av = mv.visitAnnotation(Joined, true)
                av?.visitEnd()
            }
            super.visitCode()
            if (!isJoined && bindingBean != null){
                isSetBindingInfo = AsmUtils.addBindingCode(bindingBean,viewBindingClass, mv)
            }
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {


            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }

        override fun visitEnd() {
            super.visitEnd()
        }
    }


    internal inner class FieldAnnoVisitor(private val fieldName :String,annotationVisitor:AnnotationVisitor?) : AnnotationVisitor(Opcodes.ASM9,annotationVisitor) {
        private var position: Int? = null
        private var methodName: String? = null
        private var methodDesc: String? = null
        private var bindingType: String? = null
        private var isProtected: Boolean? = null
        override fun visit(name: String, value: Any) {
            if (name == "position") {
                position = value.toString().toInt()
            }else if (name == "methodName") {
                val method = Method.getMethod(value.toString())
                methodName = method.name
                methodDesc = method.descriptor
            }else if (name == "isProtected"){
                isProtected = value.toString().toBoolean()
            }
            super.visit(name, value)
        }

        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            super.visitEnum(name, descriptor, value)
            if (name == "bindingType") {
                bindingType = value
            }
        }

        override fun visitEnd() {
            super.visitEnd()
            if (position != null && methodName != null && methodDesc != null && bindingType != null && isProtected!= null){
                BindingUtils.addBindingInfo(BindingBean(className,fieldName,position!!,methodName!!,methodDesc!!,bindingType!!,isProtected!!))
            }

        }
    }


    override fun visitField(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        return MyFieldVisitor(name,
            super.visitField(access, name, descriptor, signature, value)
        )
    }

    open inner class MyFieldVisitor(
        private val fieldName :String,
        fieldVisitor: FieldVisitor?
    ) : FieldVisitor(
        Opcodes.ASM9,fieldVisitor
    ) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            return if (Type.getType(descriptor).className == "com.flyjingfish.viewbindingpro_core.BindViewBinding"){
                FieldAnnoVisitor(fieldName,super.visitAnnotation(descriptor, visible))
            }else{
                super.visitAnnotation(descriptor, visible)
            }
        }

    }


}