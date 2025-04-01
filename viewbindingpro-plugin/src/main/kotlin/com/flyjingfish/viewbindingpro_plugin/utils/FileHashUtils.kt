package com.flyjingfish.viewbindingpro_plugin.utils

import java.util.concurrent.ConcurrentHashMap


object FileHashUtils {

    private var classScanRecord1: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun isScanFile(className: String): Boolean {
        return classScanRecord1.add(className)
    }

    fun clearScanRecord() {
        classScanRecord1.clear()
    }
}