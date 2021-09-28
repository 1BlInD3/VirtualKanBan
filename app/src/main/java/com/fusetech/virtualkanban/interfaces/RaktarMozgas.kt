package com.fusetech.virtualkanban.interfaces

interface RaktarMozgas {
    fun sendCode(message: String)
    fun setProgressOn()
    fun setProgressOff()
    fun setText(text: String)
    fun init(code: String)
}