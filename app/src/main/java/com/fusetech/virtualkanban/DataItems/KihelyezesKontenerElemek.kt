package com.fusetech.virtualkanban.dataItems

data class KihelyezesKontenerElemek(
    val id: Int,
    val vonalkod: String,
    val megjegyzes1: String,
    val megjegyzes2: String,
    val intrem: String,
    val igenyelve: String,
    val kiadva: Int,
    val kontenerID: Int
)
