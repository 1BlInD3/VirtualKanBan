package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_menu.view.*

private const val ARG_PARAM1 = "param1"

class MenuFragment : Fragment() {
    private var param1: Boolean? = null
    private lateinit var polcHelyezes : Button
    private lateinit var igenyOssze : Button
    private lateinit var igenyLezar : Button
    private lateinit var igenyKiszed : Button
    private lateinit var igenyKihelyez : Button
    private lateinit var kiszedesreVar : Button
    private lateinit var tobbletOssze : Button
    private lateinit var tobbletKihelyez : Button
    private lateinit var cikkLekerdezes : Button
    private lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getBoolean(ARG_PARAM1)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        mainActivity = activity as MainActivity
        polcHelyezes = view.polcraHelyezes
        igenyOssze = view.kontenerOsszelaalitas
        igenyLezar = view.kontenerLezaras
        igenyKiszed = view.konetnerKiszedes
        igenyKihelyez = view.kontenerKihelyezes
        kiszedesreVar = view.kontenerVar
        tobbletOssze = view.tobbletOsszeallitas
        tobbletKihelyez = view.tobbletKihelyezes
        cikkLekerdezes = view.cikkLekerdezes

        if(!param1!!)
        {
           // polcHelyezes.isEnabled = false
            //polcHelyezes.setBackgroundResource(R.drawable.disabled)
            igenyOssze.isEnabled = false
            igenyOssze.setBackgroundResource(R.drawable.disabled)
            igenyLezar.isEnabled = false
            igenyLezar.setBackgroundResource(R.drawable.disabled)
            igenyKiszed.isEnabled = false
            igenyKiszed.setBackgroundResource(R.drawable.disabled)
            igenyKihelyez.isEnabled = false
            igenyKihelyez.setBackgroundResource(R.drawable.disabled)
            kiszedesreVar.isEnabled = false
            kiszedesreVar.setBackgroundResource(R.drawable.disabled)
            tobbletOssze.isEnabled = false
            tobbletOssze.setBackgroundResource(R.drawable.disabled)
            tobbletKihelyez.isEnabled = false
            tobbletKihelyez.setBackgroundResource(R.drawable.disabled)
        }

        polcHelyezes.setOnClickListener {
            mainActivity.loadPolcHelyezesFragment()
        }
        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: Boolean?) =
            MenuFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, param1!!)
                }
            }
    }
}