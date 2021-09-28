package com.fusetech.virtualkanban.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fusetech.virtualkanban.dataItems.PolcItems
import com.fusetech.virtualkanban.utils.SqlLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RaktarMozgasViewModel
@Inject
constructor(private val sql: SqlLogic) : ViewModel() {
    var celRaktar = ""
    var kiinduloRakhely = ""
    private var adatok =  MutableLiveData<ArrayList<PolcItems>>()

    fun getItems(): LiveData<ArrayList<PolcItems>> {
        return adatok
    }
    fun loadItems(code: String){
        adatok = sql.polcResultQuery(code)
    }
    fun scalaSend(){
        for (i in 0 until getItems().value!!.size){

        }
    }
}