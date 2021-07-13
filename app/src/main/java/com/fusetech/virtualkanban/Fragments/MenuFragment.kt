package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_menu.view.*

private const val ARG_PARAM1 = "param1"

class MenuFragment : Fragment() {
    private var param1: Boolean? = null
    private lateinit var polcHelyezes: Button
    private lateinit var igenyOssze: Button
    private lateinit var igenyLezar: Button
    private lateinit var igenyKiszed: Button
    private lateinit var igenyKihelyez: Button
    private lateinit var kiszedesreVar: Button
    private lateinit var tobbletOssze: Button
    private lateinit var tobbletKihelyez: Button
    private lateinit var cikkLekerdezes: Button
    private lateinit var mainActivity: MainActivity
    private lateinit var menuProgress: ProgressBar
    private lateinit var kilepes: Button

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
        menuProgress = view.menu_progress
        kilepes = view.kilepesMenuButton
        mainActivity.cancelExitTimer()
        setMenuProgressOff()
        if (!param1!!) {
            polcHelyezes.isEnabled = false
            polcHelyezes.setBackgroundResource(R.drawable.disabled)
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
            if (polcHelyezes.isEnabled) {
                mainActivity.loadPolcHelyezesFragment()
            }
        }
        igenyOssze.setOnClickListener {
            if (igenyOssze.isEnabled) {
                //mainActivity.loadIgenyOsszeallitasFragment("","")// ez csak megjelenítés semmi sql nem fut alatta
                mainActivity.containerCheck(mainActivity.dolgKod)
            }
        }
        igenyLezar.setOnClickListener {
            if (igenyLezar.isEnabled) {
                mainActivity.igenyKontenerCheck()
            }
        }
        igenyKiszed.setOnClickListener {
            if (igenyKiszed.isEnabled) {
                mainActivity.igenyKontenerKiszedes()
            }
        }
        igenyKihelyez.setOnClickListener {
            if (igenyKihelyez.isEnabled) {
                mainActivity.loadKihelyezesFragment()
            }
        }
        kiszedesreVar.setOnClickListener {
            if (kiszedesreVar.isEnabled) {
                mainActivity.kiszedesreVaro()
            }
        }
        tobbletOssze.setOnClickListener {
            if (tobbletOssze.isEnabled) {
                mainActivity.containerCheck7(mainActivity.dolgKod)
            }
        }
        tobbletKihelyez.setOnClickListener {
            if (tobbletKihelyez.isEnabled) {
                mainActivity.loadTobbletKontenerKihelyezes()
            }
        }
        cikkLekerdezes.setOnClickListener {
            mainActivity.loadCikklekerdezesFragment()
        }
        kilepes.setOnClickListener {
            mainActivity.finishAndRemoveTask()
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

    fun setMenuProgressOn() {
        try {
            menuProgress.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.d("MenuFrag", "setMenuProgressOn: ")
        }

    }

    fun setMenuProgressOff() {
        try {
            menuProgress.visibility = View.GONE
        } catch (e: Exception) {
            Log.d("MenuFrag", "setMenuProgressOn: ")
        }
    }
}
