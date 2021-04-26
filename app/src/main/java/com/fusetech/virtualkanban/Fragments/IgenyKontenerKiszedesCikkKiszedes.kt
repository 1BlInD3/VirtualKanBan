package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_cikk_kiszedes.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_cikk_kiszedes.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "IgenyKontenerKiszedesCi"

class IgenyKontenerKiszedesCikkKiszedes : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var cikkEdit: EditText
    private lateinit var meg1: TextView
    private lateinit var meg2: TextView
    private lateinit var intrem: TextView
    private lateinit var unit: TextView
    private lateinit var igeny: EditText
    private lateinit var polc: EditText
    private lateinit var mennyiseg: EditText
    private lateinit var feltolt: Button
    private lateinit var vissza: Button
    private lateinit var progress: ProgressBar
    private lateinit var mainActivity: MainActivity
    private lateinit var kontenerNumber: TextView
    private lateinit var cikkNumber: TextView
    private var igenyeltMennyiseg: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes_cikk_kiszedes,container,false)
        mainActivity = activity as MainActivity
        cikkEdit = view.kiszedesCikkEdit
        meg1 = view.kiszedesMegj1
        meg2 = view.kiszedesMegj2
        intrem = view.intrem
        unit = view.kiszedesUnit
        unit.isAllCaps = true
        igeny = view.kiszedesIgenyEdit
        polc = view.kiszedesPolc
        mennyiseg = view.kiszedesMennyiseg
        feltolt = view.kiszedesFeltolt
        vissza = view.kiszedesVissza
        progress = view.kihelyezesProgress
        kontenerNumber = view.kontenerIDKiszedes
        cikkNumber = view.cikkIDKiszedes
        setProgressBarOff()
        cikkEdit.isEnabled = false
        igeny.isFocusable = false
        igeny.isFocusableInTouchMode = false
        mennyiseg.isFocusable = false
        mennyiseg.isFocusableInTouchMode = false
        polc.requestFocus()

        feltolt.setOnClickListener{
            if(mennyiseg.text.isEmpty()){
                mainActivity.setAlert("Nincs kitöltve minden rendesen")
                mennyiseg.requestFocus()
            }
        }
        vissza.setOnClickListener{
            mainActivity.cikkUpdate(cikkIDKiszedes.text.trim().toString().toInt())
            mainActivity.loadMenuFragment(true)
            mainActivity.loadKiszedesFragment()
            mainActivity.checkIfContainerStatus(kontenerIDKiszedes.text.trim().toString())
        }
        mennyiseg.setOnClickListener {
            if(mennyiseg.text.trim().toString().toDouble().equals(igenyeltMennyiseg)){
                //itt akkor le kell zárni 3as státuszúra
                mainActivity.setAlert("Megegyzik, mehet 3as státuszra")
            }else if(mennyiseg.text.toString().toDouble()> igenyeltMennyiseg && mennyiseg.text.toString().toDouble() < szazalek(10)){
                mainActivity.setAlert("Kivehetsz annyival többet és 3as státusz")
            }else if (mennyiseg.text.trim().toString().toDouble() == 0.0){
                mainActivity.setAlert("Nullával ki van ütve 3as státusz")
            }else if(mennyiseg.text.toString().toDouble() > szazalek(10)){
                mainActivity.setAlert("Túl sok ennyit nem vehetsz ki")
            }else{
                //itt kell átírni a dolgokat, hogy vigye le a polcról és csökkentse az igényt és beírja a másik táblába
            }
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerKiszedesCikkKiszedes().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun setProgressBarOff(){
        progress.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }
    fun performButton(){
        vissza.performClick()
    }

    override fun onResume() {
        super.onResume()
        cikkEdit.setText(arguments?.getString("K_CIKK"))
        Log.d(TAG, "onCreateView: ${arguments?.getString("K_CIKK")}")
        meg1.text = arguments?.getString("K_MEGJ1")
        meg2.text = arguments?.getString("K_MEGJ2")
        intrem.text = arguments?.getString("K_INT")
        igenyeltMennyiseg = arguments?.getDouble("K_IGENY")!!
        //igeny.setText(arguments?.getDouble("K_IGENY").toString())
        igeny.setText(igenyeltMennyiseg.toString())
        Log.d(TAG, "onCreateView: ${arguments?.getString("K_IGENY").toString()}")
        unit.text = arguments?.getString("K_UNIT")
        kontenerNumber.text = arguments?.getInt("K_KONTENER").toString()
        cikkNumber.text = arguments?.getInt("K_ID").toString()

    }
    fun szazalek(x : Int): Double{
        var ceiling: Int
        ceiling = ((igenyeltMennyiseg/mennyiseg.text.toString().toDouble()) * x).toInt()
        return igenyeltMennyiseg+ceiling
    }
}