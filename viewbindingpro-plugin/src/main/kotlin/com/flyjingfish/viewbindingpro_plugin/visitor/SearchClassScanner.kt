package com.flyjingfish.viewbindingpro_plugin.visitor

import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import com.flyjingfish.viewbindingpro_plugin.bean.BindingClassBean
import com.flyjingfish.viewbindingpro_plugin.utils.AsmUtils
import com.flyjingfish.viewbindingpro_plugin.utils.BindingUtils
import com.flyjingfish.viewbindingpro_plugin.utils.CancelBindClass
import com.flyjingfish.viewbindingpro_plugin.utils.CancelBindViewBinding
import com.flyjingfish.viewbindingpro_plugin.utils.Joined
import com.flyjingfish.viewbindingpro_plugin.utils.ViewBindingName
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
    private var bindingClassInfo: BindingClassBean ?= null
    private var superName: String? =null
    private var signature: String? =null
    private var isSetBindingInfo: Boolean = false
    private var isSetBindingClassInfo: Boolean = false
    private lateinit var viewBindingClass: String
    private lateinit var bindingClass: String
    private var cancelBindViewBinding: Boolean = false
    private var cancelBindClass: Boolean = false
    interface OnBackNotWovenMethod{
        fun onBack(bindingInfo: BindingBean,superName: String?,viewBindingClass:String)
        fun onBack(bindingInfo: BindingClassBean,superName: String?,bindingClass:String)
        fun onModify()
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        if (descriptor == CancelBindViewBinding){
            cancelBindViewBinding = true
        }
        if (descriptor == CancelBindClass){
            cancelBindClass = true
        }
        if (cancelBindViewBinding){
            bindingInfo = null
        }
        if (cancelBindClass){
            bindingClassInfo = null
        }
        return super.visitAnnotation(descriptor, visible)
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
        this.signature = signature
        val bindingBean = BindingUtils.isExtendBaseClass(superName)
        if (superName != null && bindingBean != null){
            parseGenericSignature(signature,bindingBean)
        }

        val bindingClassBean = BindingUtils.isExtendBaseBindClass(superName)
        if (superName != null && bindingClassBean != null){
            parseGenericSignature(signature,bindingClassBean)
        }
        interfaces?.let {
            var isViewBinding = false
            for (s in it) {
                if (slashToDot(s) == ViewBindingName){
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
        var isRegister = false
        // 使用 ASM 提供的 SignatureReader 解析签名
        if (signature != null){
            val signatureReader = SignatureReader(signature)
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
        }
        if (!isRegister){
//            BindingUtils.addBindingInfo4Extends(className,BindingBean(className,bindingBean.fieldName,bindingBean.position,bindingBean.methodName,bindingBean.methodDesc,bindingBean.bindingType))
            BindingUtils.addBindingInfo4Extends(className,bindingBean)
        }
    }

    private fun parseGenericSignature(signature: String?,bindingBean: BindingClassBean) {
        var isRegister = false
        // 使用 ASM 提供的 SignatureReader 解析签名
        if (signature != null){
            val signatureReader = SignatureReader(signature)
            signatureReader.accept(object : SignatureVisitor(Opcodes.ASM9) {
                private var index = 0
                override fun visitClassType(name: String?) {
                    if (index >0 && name != null && bindingBean.position == index-1){
                        isRegister = true
                        bindingClassInfo = bindingBean
                        bindingClass = name
                    }
                    super.visitClassType(name)
                    index++
                }

            })
        }
        if (!isRegister){
            BindingUtils.addBindClassInfo4Extends(className,bindingBean)
        }
    }


    override fun visitMethod(
        access: Int, name: String, descriptor: String,
        signature: String?, exceptions: Array<String?>?
    ): MethodVisitor? {
        val bindingBean = bindingInfo
        val bindingClassBean = bindingClassInfo
        return if ((bindingBean != null && bindingBean.methodName == name && bindingBean.methodDesc == descriptor)||
            (bindingClassBean != null && bindingClassBean.insertMethodName == name && bindingClassBean.insertMethodDesc == descriptor)){
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

        val bindingClassBean = bindingClassInfo
        if (!isSetBindingClassInfo && bindingClassBean != null && ::bindingClass.isInitialized){
            onBackNotWovenMethod?.onBack(bindingClassBean,superName,bindingClass)
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
            val bindingClassBean = bindingClassInfo
            val join = isJoined
            if (!join && (bindingBean != null || bindingClassBean != null)){
                val av = mv.visitAnnotation(Joined, false)
                av?.visitEnd()
            }
            super.visitCode()
            if (!join && bindingBean != null){
                isSetBindingInfo = AsmUtils.addBindingCode(bindingBean,viewBindingClass, mv)
                onBackNotWovenMethod?.onModify()
            }

            if (!join && bindingClassBean != null){
                isSetBindingClassInfo = AsmUtils.addBindingClassCode(bindingClassBean,bindingClass, mv)
                onBackNotWovenMethod?.onModify()
            }

        }

    }


    internal inner class FieldAnnoVisitor(private val fieldName :String,private val type :AnnoType,annotationVisitor:AnnotationVisitor?) : AnnotationVisitor(Opcodes.ASM9,annotationVisitor) {
        private var position: Int? = null
        private var methodName: String? = null
        private var methodDesc: String? = null
        private var callMethodName: String? = null
        private var callMethodDesc: String? = null
        private var bindingType: String? = null
        private var isProtected: Boolean? = null
        override fun visit(name: String, value: Any) {
            when (name) {
                "position" -> {
                    position = value.toString().toInt()
                }
                "methodName","insertMethodName" -> {
                    val method = Method.getMethod(value.toString())
                    methodName = method.name
                    methodDesc = method.descriptor
                }
                "callMethodName" -> {
                    val method = Method.getMethod(value.toString())
                    callMethodName = method.name
                    callMethodDesc = method.descriptor
                }
                "isProtected" -> {
                    isProtected = value.toString().toBoolean()
                }
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
            if (type == AnnoType.BindViewBinding && position != null && methodName != null && methodDesc != null && bindingType != null && isProtected!= null){
                BindingUtils.addBindingInfo(BindingBean(className,fieldName,position!!,methodName!!,methodDesc!!,bindingType!!,isProtected!!))
            }

            if (type == AnnoType.BindClass && position != null && methodName != null && methodDesc != null && callMethodName != null && callMethodDesc!= null && isProtected!= null){
                var baseClass :String ?= null
                if (signature != null){
                    val signatureReader = SignatureReader(signature)
                    signatureReader.accept(object : SignatureVisitor(Opcodes.ASM9) {
                        private var index = 0
                        override fun visitClassType(name: String?) {
                            if (name != null && position == index){
                                baseClass = name
                            }
                            super.visitClassType(name)
                            index++
                        }
                    })
                }

                baseClass?.let {
                    BindingUtils.addBaseBindClass(it)
                    BindingUtils.addBindClassInfo(BindingClassBean(className,it,fieldName,position!!,methodName!!,methodDesc!!,callMethodName!!,callMethodDesc!!,isProtected!!))
                }
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
            return when (Type.getType(descriptor).className) {
                "com.flyjingfish.viewbindingpro_core.BindViewBinding" -> {
                    FieldAnnoVisitor(fieldName,
                        AnnoType.BindViewBinding,super.visitAnnotation(descriptor, visible))
                }
                "com.flyjingfish.viewbindingpro_core.BindClass" -> {
                    FieldAnnoVisitor(fieldName,
                        AnnoType.BindClass,super.visitAnnotation(descriptor, visible))
                }
                else -> {
                    super.visitAnnotation(descriptor, visible)
                }
            }
        }

    }

    enum class AnnoType{
        BindViewBinding,BindClass
    }


}