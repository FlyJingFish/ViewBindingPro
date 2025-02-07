package com.flyjingfish.viewbindingpro_plugin.utils

import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import com.flyjingfish.viewbindingpro_plugin.bean.BindingClassBean
import java.util.concurrent.ConcurrentHashMap

object BindingUtils {
//    private val baseClassSet = mutableSetOf<String>()
    private val viewBindingMap = ConcurrentHashMap<String,BindingBean>()
    private val bindClassMap = ConcurrentHashMap<String,BindingClassBean>()
    private val viewBindingSet = mutableSetOf<String>()
    private val baseBindClassSet = mutableSetOf<String>()
    fun addBindingInfo(bindingBean: BindingBean){
//        synchronized(baseClassSet){
//            baseClassSet.add(bindingBean.className)
//        }
        viewBindingMap[bindingBean.className] = bindingBean
    }

    fun addBindClassInfo(bindingBean: BindingClassBean){
//        synchronized(baseClassSet){
//            baseClassSet.add(bindingBean.className)
//        }
        bindClassMap[bindingBean.className] = bindingBean
    }

    fun addBindingInfo4Extends(className: String,bindingBean: BindingBean){
//        synchronized(baseClassSet){
//            baseClassSet.add(bindingBean.className)
//        }
        viewBindingMap[className] = BindingBean(bindingBean.className,bindingBean.fieldName,bindingBean.position,bindingBean.methodName,bindingBean.methodDesc,bindingBean.bindingType,bindingBean.isProtected)
    }

    fun addBindClassInfo4Extends(className: String,bindingBean: BindingClassBean){
//        synchronized(baseClassSet){
//            baseClassSet.add(bindingBean.className)
//        }
        bindClassMap[className] = BindingClassBean(bindingBean.className,bindingBean.baseClassName,bindingBean.fieldName,bindingBean.position,bindingBean.insertMethodName,bindingBean.insertMethodDesc,bindingBean.callMethodName,bindingBean.callMethodDesc,bindingBean.isProtected)
    }

    fun isExtendBaseClass(className: String?): BindingBean? {
        if (className == null)return null
        return viewBindingMap[className]
    }

    fun addViewBindingClass(className: String){
        viewBindingSet.add(className)
    }

    fun isViewBindingClass(className: String):Boolean{
        return className in viewBindingSet
    }

    fun isExtendBaseBindClass(className: String?): BindingClassBean? {
        if (className == null)return null
        return bindClassMap[className]
    }

    fun addBaseBindClass(className: String){
        baseBindClassSet.add(className)
    }

    fun isBaseBindClass(className: String):Boolean{
        return className in baseBindClassSet
    }
}