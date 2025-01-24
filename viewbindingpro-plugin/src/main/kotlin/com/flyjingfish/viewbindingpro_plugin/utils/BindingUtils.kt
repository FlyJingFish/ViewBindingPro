package com.flyjingfish.viewbindingpro_plugin.utils

import com.flyjingfish.viewbindingpro_plugin.bean.BindingBean
import java.util.concurrent.ConcurrentHashMap

object BindingUtils {
//    private val baseClassSet = mutableSetOf<String>()
    private val classMap = ConcurrentHashMap<String,BindingBean>()
    private val viewBindingSet = mutableSetOf<String>()
    fun addBindingInfo(bindingBean: BindingBean){
//        synchronized(baseClassSet){
//            baseClassSet.add(bindingBean.className)
//        }
        classMap[bindingBean.className] = bindingBean
    }

    fun addBindingInfo4Extends(className: String,bindingBean: BindingBean){
//        synchronized(baseClassSet){
//            baseClassSet.add(bindingBean.className)
//        }
        classMap[className] = BindingBean(bindingBean.className,bindingBean.fieldName,bindingBean.position,bindingBean.methodName,bindingBean.methodDesc,bindingBean.bindingType,bindingBean.isProtected)
    }

    fun isExtendBaseClass(className: String?): BindingBean? {
        if (className == null)return null
        return classMap[className]
    }

    fun addViewBindingClass(className: String){
        viewBindingSet.add(className)
    }

    fun isViewBindingClass(className: String):Boolean{
        return className in viewBindingSet
    }
}