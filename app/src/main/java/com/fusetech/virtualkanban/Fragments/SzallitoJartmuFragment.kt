package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_szallito_jartmu.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SzallitoJartmuFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var szallitoEdit: EditText
    private lateinit var exitSzallito: Button
    private lateinit var mainActivity: MainActivity

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
        val view = inflater.inflate(R.layout.fragment_szallito_jartmu, container, false)
        mainActivity = activity as MainActivity
        szallitoEdit = view.szallitoEdit
        exitSzallito = view.exitSzallitoButton
        szallitoEdit.isEnabled = false

        exitSzallito.setOnClickListener {
            mainActivity.loadMenuFragment(true)
            mainActivity.igenyKontenerKiszedes()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SzallitoJartmuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun setJarmu(jarmu: String){
        szallitoEdit.setText(jarmu)
    }
}