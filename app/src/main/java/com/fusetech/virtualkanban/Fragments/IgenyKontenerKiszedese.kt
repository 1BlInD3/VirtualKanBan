package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.kihelyezesItems
import com.fusetech.virtualkanban.DataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedese.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontenerKiszedese : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var kilep : Button
    private lateinit var szallitoText: EditText
    private lateinit var mainActivity: MainActivity
    private lateinit var szerelohely: EditText


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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedese, container, false)
        mainActivity = activity as MainActivity
        kilep = view.exit5Btn
        szallitoText = view.szallitoJarmuText
        szerelohely = view.szereloText
        szerelohely.isEnabled = false
        szallitoText.requestFocus()
        kilep.setOnClickListener {
            mainActivity.setAlert("Megnyomtam a kilépést")
        }
        return view
    }

    fun setCode(code: String){
        if(szallitoText.text.isEmpty()){
            szallitoText.setText(code)
            szallitoText.isEnabled = false
            szerelohely.isEnabled = true
            szerelohely.requestFocus()
            mainActivity.getContainerList(code)
        }else{
            if(isCodeInList(code)){
                szerelohely.setText(code)
                szerelohely.isEnabled = false
                mainActivity.loadKihelyezesItems(code)

            }else{
                mainActivity.setAlert("Nincs a vonalkód a listában!")
            }
        }
    }
    fun mindentVissza(){
        szallitoText.setText("")
        szallitoText.isEnabled = true
        szallitoText.requestFocus()
        szerelohely.isEnabled = false
    }
    fun isCodeInList(code: String): Boolean{
        val bool = kihelyezesItems.contains(SzerelohelyItem(code))
        return bool
    }
    fun exit(){
        kilep.requestFocus()
        szallitoText.setText("")
        szerelohely.setText("")
    }
    fun onBack(){
        szerelohely.isEnabled = true
        szerelohely.requestFocus()
        szerelohely.setText("")
    }
}