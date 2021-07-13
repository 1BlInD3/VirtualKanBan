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

    private lateinit var szallitoEdit: EditText
    private lateinit var exitSzallito: Button
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_szallito_jartmu, container, false)
        mainActivity = activity as MainActivity
        szallitoEdit = view.szallitoEdit
        exitSzallito = view.exitSzallitoButton
        szallitoEdit.requestFocus()
        exitSzallito.setOnClickListener {
            mainActivity.loadMenuFragment(true)
            mainActivity.igenyKontenerKiszedes()
        }

        return view
    }

    fun setJarmu(jarmu: String){
        szallitoEdit.setText(jarmu)
        szallitoEdit.isEnabled = false
    }
}