package com.flyjingfish.viewbindingpro_core

enum class BingType {
    /**
     * 调用 ViewBinding的 inflate(LayoutInflater inflater) 方法
     */
    INFLATE,
    /**
     * 调用 ViewBinding的 inflate(LayoutInflater inflater, ViewGroup parent, true) 方法
     */
    INFLATE_TRUE,
    /**
     * 调用 ViewBinding的 inflate(LayoutInflater inflater, ViewGroup parent, false) 方法
     */
    INFLATE_FALSE,
    /**
     * 调用 ViewBinding的 bind(View rootView) 方法
     */
    BIND
}