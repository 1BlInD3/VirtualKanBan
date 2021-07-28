package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_szallito_jartmu.view.*


class SzallitoJartmuFragment : Fragment() {

    private var szallitoEdit: EditText? = null
    private var exitSzallito: Button? = null
    private var mainActivity: MainActivity? = null
    private var myView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_szallito_jartmu, container, false)
        mainActivity = activity as MainActivity
        //mainActivity?.igenyKiszedesFragment = null
        szallitoEdit = myView?.szallitoEdit
        exitSzallito = myView?.exitSzallitoButton
        szallitoEdit?.requestFocus()
        exitSzallito?.setOnClickListener {
            //mainActivity?.loadMenuFragment(true)
            mainActivity?.szallitoJarmuFragment = null
            mainActivity?.igenyKontenerKiszedes()
        }

        return myView
    }

    fun setJarmu(jarmu: String) {
        szallitoEdit?.setText(jarmu)
        szallitoEdit?.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myView = null
        exitSzallito = null
        mainActivity = null
        szallitoEdit = null
    }
}