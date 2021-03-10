package com.fusetech.virtualkanban.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.Fragments.LoginFragment
import com.fusetech.virtualkanban.Fragments.MenuFragment
import com.fusetech.virtualkanban.R

class MainActivity : AppCompatActivity() {

    private val URL = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, loginFragment,"LOGIN").commit()

    }
    fun loadMenuFragment(hasRight : Boolean?){
        val menuFragment : MenuFragment = MenuFragment.newInstance(hasRight)
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, menuFragment,"MENU").commit()
    }
   /* private val myFristRunnable = Runnable {
        sql()
    }
    private fun startRunnable()
    {
        Thread(myFristRunnable).start()
    }
    private fun sql()
    {
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        val connection = DriverManager.getConnection(URL)
        if(connection!=null){
            val statement : Statement = connection.createStatement()
            var resultSet : ResultSet = statement.executeQuery(resources.getString(R.string.allData))
            while (resultSet.next())
            {
                var a = resultSet.getString("Cikkszam")
                var b = resultSet.getString("Mennyiseg")
                var c = resultSet.getString("Dolgozo")
                var d = resultSet.getString("RaktHely")
                myList.add(
                    ProbaClass(
                        a,
                        b,
                        c,
                        d
                    )
                )

            }
            var bundle = Bundle()
            bundle.putSerializable("Lista",myList)
            val firstKotlinFragment  = FirstKotlinFragment()
            firstKotlinFragment.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.frame_container,firstKotlinFragment).commit()
        }
    }*/
}