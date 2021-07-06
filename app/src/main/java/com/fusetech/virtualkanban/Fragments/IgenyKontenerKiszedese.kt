package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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
    private lateinit var progress : ProgressBar


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
        progress = view.kihelyezesProgressBar
        progressBarOff()
        kilep = view.exit5Btn
        szallitoText = view.szallitoJarmuText
        szerelohely = view.szereloText
        szerelohely.isFocusable = false
        szerelohely.isFocusableInTouchMode = false
        szallitoText.requestFocus()
        kilep.setOnClickListener {
            when{
                mainActivity.getFragment("KIHELYEZESLISTA") -> {
                    exit()
                    mainActivity.loadMenuFragment(true)
                }
                mainActivity.getFragment("KIHELYEZESITEMS") -> {
                    onBack()
                    mainActivity.getContainerList("SZ01")
                }
                else -> {
                    exit()
                    mainActivity.loadMenuFragment(true)
                }
            }
        }
        return view
    }

    fun setCode(code: String){
        if(szallitoText.text.isEmpty()){
            szallitoText.setText(code)
            szallitoText.isFocusable = false
            szallitoText.isFocusableInTouchMode = false
            szerelohely.isFocusable = true
            szerelohely.isFocusableInTouchMode = true
            szerelohely.requestFocus()
            mainActivity.getContainerList(code)
        }else{
            if(isCodeInList(code)){
                szerelohely.setText(code)
                szerelohely.isFocusable = false
                szerelohely.isFocusableInTouchMode = false
                mainActivity.loadKihelyezesItems(code)
            }else{
                mainActivity.setAlert("Nincs a vonalkód a listában!")
            }
        }
    }
    fun mindentVissza(){
        szallitoText.setText("")
        szallitoText.isFocusable = true
        szallitoText.isFocusableInTouchMode = true
        szallitoText.requestFocus()
        szerelohely.isFocusable = false
        szerelohely.isFocusableInTouchMode = false
    }
    fun isCodeInList(code: String): Boolean{
        val bool = kihelyezesItems.contains(SzerelohelyItem(code))
        return bool
    }
    fun exit(){
        kilep.requestFocus()
        szallitoText.isFocusable = false
        szallitoText.isFocusableInTouchMode = false
        szerelohely.isFocusable = false
        szerelohely.isFocusableInTouchMode = false
        szallitoText.setText("")
        szerelohely.setText("")
    }
    fun onBack(){
        szerelohely.isFocusable = true
        szerelohely.isFocusableInTouchMode = true
        szerelohely.requestFocus()
        szerelohely.setText("")
    }
    fun progressBarOn(){
        progress.visibility = View.VISIBLE
    }
    fun progressBarOff(){
        progress.visibility = View.INVISIBLE
    }
}