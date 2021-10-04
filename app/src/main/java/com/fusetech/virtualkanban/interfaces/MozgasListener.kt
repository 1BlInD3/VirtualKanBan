package com.fusetech.virtualkanban.interfaces

interface MozgasListener {
    fun message(message: String)
    fun setSend()
    fun sendOneByOne()
    fun setPolcText(code: String)
    fun whenButtonIsClicked()
    fun setProgressOn()
    fun setProgressOff()
    fun highlightText()
}