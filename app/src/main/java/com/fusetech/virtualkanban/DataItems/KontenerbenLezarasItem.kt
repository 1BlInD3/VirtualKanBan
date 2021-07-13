package com.fusetech.virtualkanban.dataItems

import java.io.Serializable

data class KontenerbenLezarasItem(val cikkszam: String?, val megjegyzes1: String?, val megjegyzes2: String?, val intrem: String?, val igeny: String?, val kiadva: String?, val statusz: Int, val unit: String?, val id: Int, val kontener_id: Int): Serializable {
}