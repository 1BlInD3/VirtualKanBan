package com.fusetech.virtualkanban.utils

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.dataItems.PolcItems
import java.sql.Connection
import java.sql.DriverManager

class SqlLogic {
    fun isPolc(code: String): Boolean {
        var polc = false
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(MainActivity.url)
            val statement =
                connection.prepareStatement("SELECT WarehouseID, BinNumber, InternalName, BinDescript2 FROM [ScaCompDB].[dbo].VF_SC360300_StockBinNo left outer join [ScaCompDB].[dbo].VF_SC230300_WarehouseInfo ON WarehouseID = Warehouse where BinNumber = ?")
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            polc = resultSet.next()
        } catch (e: Exception) {
            Log.d("sql", "isPolc: ")
        }
        return polc
    }

    fun polcResultQuery(code: String): MutableLiveData<ArrayList<PolcItems>> {
        MainActivity.zarolt = false
        val myList = MutableLiveData<ArrayList<PolcItems>>()
        val polc: ArrayList<PolcItems> = ArrayList()
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(MainActivity.url)
            val statement =
                connection.prepareStatement("SELECT SC33001 as [StockItem],SUM(SC33005) as [BalanceQty],SUM(SC33008) as [ReceivedQty],MAX(case when SC33038 = '' then 'Szabad' else VF_SY240300_QTCategory.TextDescription end) as QcCategory,MAX([SC01002]) as Description1,MAX([SC01003]) as Description2,MAX([SC01093]) as IntRem,MAX([SC01094]) as IntRem2,rtrim(MAX([Description])) as Unit ,MAX(WarehouseID)as WarehouseID,MAX(InternalName)as Warehouse\tFROM [ScaCompDB].[dbo].[VF_SC360300_StockBinNo] left outer join [ScaCompDB].[dbo].SC330300 on BinNumber = SC33004 left outer join [ScaCompDB].[dbo].[SC010300] on SC33001 = SC01001 left join [ScaCompDB].[dbo].[VF_SCUN0300_UnitCode] on SC01133 = UnitCode LEFT OUTER JOIN [ScaCompDB].[dbo].VF_SY240300_QTCategory ON  SC33038 = VF_SY240300_QTCategory.Key1 left outer join [ScaCompDB].[dbo].VF_SC230300_WarehouseInfo as wi ON wi.Warehouse = WarehouseID\twhere SC33005 > 0 and BinNumber= ? group by SC33001, SC33010")
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                Log.d("sql", "polcResultQuery: ")
            } else {
                do {
                    polc.add(
                        PolcItems(
                            resultSet.getDouble("BalanceQty"),
                            resultSet.getString("Unit"),
                            resultSet.getString("Description1"),
                            resultSet.getString("Description2"),
                            resultSet.getString("IntRem"),
                            resultSet.getString("QcCategory"),
                            resultSet.getString("StockItem")
                        )
                    )
                } while (resultSet.next())
                myList.postValue(polc)
                for(i in 0 until polc.size){
                    Log.d("SQLlogic", "polcResultQuery: ${polc[i].mAllapot}")
                    if(polc[i].mAllapot != "Szabad"){
                        MainActivity.zarolt = true
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("sql", "polcResultQuery: $e")
        }
        return myList
    }
}