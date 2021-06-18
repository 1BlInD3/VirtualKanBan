package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_cikkek_polcra.view.*

class TobbletCikkekPolcraFragment : Fragment() {

    private lateinit var kontenerID : TextView
    private lateinit var cikkID : TextView
    private lateinit var cikkNumber : EditText
    private lateinit var megjegyzes1: TextView
    private lateinit var megjegyzes2 : TextView
    private lateinit var intrem : TextView
    private lateinit var unit : TextView
    private lateinit var igeny : EditText
    private lateinit var polc : EditText
    private lateinit var mennyiseg : EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar : ProgressBar
    private lateinit var lezarasBtn : Button
    private lateinit var visszaBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tobblet_cikkek_polcra, container, false)
        kontenerID = view.tkontenerIDKiszedes
        cikkID = view.tcikkIDKiszedes
        cikkNumber = view.tkiszedesCikkEdit
        megjegyzes1 = view.tkiszedesMegj1
        megjegyzes2 = view.tkiszedesMegj2
        intrem = view.tintrem
        unit = view.tkiszedesUnit
        igeny = view.tkiszedesIgenyEdit
        polc = view.tkiszedesPolc
        mennyiseg = view.tkiszedesMennyiseg
        recyclerView = view.tlocationRecycler
        progressBar = view.tkihelyezesProgress
        lezarasBtn = view.tkiszedesLezar
        visszaBtn = view.tkiszedesVissza

        return view
    }

}