package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.activities.MainActivity.Companion.kihelyezesItems
import com.fusetech.virtualkanban.activities.MainActivity.Companion.sz0x
import com.fusetech.virtualkanban.dataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity.Companion.sz01KiszedesDate
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedese.view.*
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontenerKiszedese : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private  var kilep : Button? = null
    private  var szallitoText: EditText? = null
    private  var mainActivity: MainActivity? = null
    private  var szerelohely: EditText? = null
    private  var progress : ProgressBar? = null
    private var myView : View? = null
    var frame: FrameLayout? = null


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
        myView = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedese, container, false)
        mainActivity = activity as MainActivity
        frame = myView!!.kihelyezesFrame
        frame?.isFocusable = false
        frame?.isFocusableInTouchMode = false
        //frame.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        progress = myView!!.kihelyezesProgressBar
        progressBarOff()
        kilep = myView!!.exit5Btn
        szallitoText = myView!!.szallitoJarmuText
        szerelohely = myView!!.szereloText
        szerelohely?.isFocusable = false
        szerelohely?.isFocusableInTouchMode = false
       /* szallitoText?.isFocusable = false
        szallitoText?.isFocusableInTouchMode = true*/
        szallitoText?.requestFocus()
        kilep?.setOnClickListener {
            when{
                mainActivity!!.getFragment("KIHELYEZESLISTA") -> {
                    exit()
                    mainActivity?.removeFragment("KIHELYEZESLISTA")
                    mainActivity!!.loadMenuFragment(true)
                }
                mainActivity!!.getFragment("KIHELYEZESITEMS") -> {
                    onBack()
                    mainActivity?.kihelyezesFragmentLista = null
                    mainActivity?.removeFragment("KIHELYEZESITEMS")
                    mainActivity!!.getContainerList(sz0x)
                }
                else -> {
                    exit()
                    mainActivity!!.loadMenuFragment(true)
                }
            }
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        return myView
    }

    @SuppressLint("SimpleDateFormat")
    fun setCode(code: String){
        if(mainActivity?.isWifiConnected()!!){
            MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
        }
        if(szallitoText!!.text.isEmpty()){
            sz01KiszedesDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            szallitoText?.setText(code)
            szallitoText?.isFocusable = false
            szallitoText?.isFocusableInTouchMode = false
            szerelohely?.isFocusable = true
            szerelohely?.isFocusableInTouchMode = true
            szerelohely?.requestFocus()
            mainActivity?.getContainerList(code.uppercase())
        }else{
            if(isCodeInList(code.uppercase())){
                szerelohely?.setText(code)
                szerelohely?.isFocusable = false
                szerelohely?.isFocusableInTouchMode = false
                mainActivity?.loadKihelyezesItems(code.uppercase())
            }else{
                mainActivity?.setAlert("A $code vonalkód nincs a listában!")
            }
        }
    }
    fun onButtonPressed(){
        kilep?.performClick()
    }
    fun mindentVissza(){
        szallitoText?.setText("")
        szallitoText?.isFocusable = true
        szallitoText?.isFocusableInTouchMode = true
        szallitoText?.requestFocus()
        szerelohely?.isFocusable = false
        szerelohely?.isFocusableInTouchMode = false
    }
    private fun isCodeInList(code: String): Boolean {
        return kihelyezesItems.contains(SzerelohelyItem(code))
    }
    fun exit(){
        kilep?.requestFocus()
        szallitoText?.isFocusable = false
        szallitoText?.isFocusableInTouchMode = false
        szerelohely?.isFocusable = false
        szerelohely?.isFocusableInTouchMode = false
        szallitoText?.setText("")
        szerelohely?.setText("")
    }
    fun onBack(){
        szerelohely?.isFocusable = true
        szerelohely?.isFocusableInTouchMode = true
        szerelohely?.requestFocus()
        szerelohely?.setText("")
    }
    fun progressBarOn(){
        progress?.visibility = View.VISIBLE
    }
    fun progressBarOff(){
        progress?.visibility = View.INVISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        myView = null
        frame = null
        kilep = null
        szallitoText = null
        mainActivity = null
        szerelohely = null
        progress = null
    }
    fun setFocusToBin(){
        szerelohely?.isFocusable = true
        szerelohely?.isFocusableInTouchMode = true
        szerelohely?.requestFocus()
        szerelohely?.selectAll()

    }
    fun deleteFocused(){
        if(szallitoText?.hasFocus()!!){
            szallitoText?.setText("")
        }else if(szerelohely?.hasFocus()!!){
            szerelohely?.setText("")
        }
    }
    /*fun afterOnPause(){
        szerelohely?.setText("")
        szerelohely?.isFocusable = true
        szerelohely?.isFocusableInTouchMode = true
        szerelohely?.requestFocus()
    }*/

    override fun onPause() {
        MainActivity.szallito = szallitoText?.text?.trim().toString()
        super.onPause()
    }
}