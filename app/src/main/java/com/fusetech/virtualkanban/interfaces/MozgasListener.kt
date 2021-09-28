package com.fusetech.virtualkanban.interfaces

interface MozgasListener {
    fun message(message: String)
    fun setSend()
}