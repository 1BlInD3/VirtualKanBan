package com.fusetech.virtualkanban.viewmodels

import androidx.lifecycle.ViewModel
import com.fusetech.virtualkanban.interfaces.RaktarMozgas
import com.fusetech.virtualkanban.utils.SqlLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RaktarkoziViewModel
@Inject
constructor(private val sql: SqlLogic) : ViewModel() {
    var raktar = ""
    var raktarMozgas: RaktarMozgas? = null

    fun getData(code: String) {
        raktarMozgas?.setProgressOn()
        CoroutineScope(IO).launch {
            if (sql.isPolc(code)) {
                CoroutineScope(Main).launch {
                    raktarMozgas?.setText(code)
                    raktarMozgas?.setProgressOff()
                    raktarMozgas?.init(code)
                }
            } else {
                CoroutineScope(Main).launch {
                    raktarMozgas?.sendCode("Polcot olvass be!")
                    raktarMozgas?.setProgressOff()
                }
            }
        }

    }
}