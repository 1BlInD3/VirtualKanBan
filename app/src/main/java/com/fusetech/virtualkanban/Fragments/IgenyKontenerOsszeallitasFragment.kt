package com.fusetech.virtualkanban.Fragments

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.view.*
import org.w3c.dom.Text

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var progressBar: ProgressBar
private lateinit var polcTextIgeny: EditText
private lateinit var megjegyzes1_igeny:TextView
private lateinit var megjegyzes2_igeny:TextView
private lateinit var intrem_igeny:TextView
private lateinit var unit_igeny:TextView

class IgenyKontenerOsszeallitasFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_osszeallitas, container, false)
        progressBar = view.progressBar_igeny
        polcTextIgeny = view.bin_igeny
        megjegyzes1_igeny = view.megjegyzes_igeny
        megjegyzes2_igeny = view.megjegyzes2_igeny
        intrem_igeny = view.intrem_igeny
        unit_igeny = view.unit_igeny
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny.text = ""
        intrem_igeny.text = ""
        unit_igeny.text = ""
        setBinFocusOn()
        setProgressBarOff()

        return view
    }

    fun setProgressBarOff(){
        progressBar.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progressBar.visibility = View.VISIBLE
    }
    fun setBinFocusOn(){
        polcTextIgeny.selectAll()
        polcTextIgeny.requestFocus()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerOsszeallitasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}