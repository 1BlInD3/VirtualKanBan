package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_load.view.*

private const val ARG_PARAM1 = "param1"

class LoadFragment : Fragment() {
    private var param1: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_load, container, false)
        val errorText = view.errorText
        val progressBar = view.progressBar

        if(param1 == ""){
            errorText.text = "Hibás bevitel"
            progressBar.visibility = View.GONE
        }
        else if(param1 == "A feldolgozás során hiba lépett fel"){
            errorText.text = param1
            progressBar.visibility = View.GONE
        }
        else if(param1 == "A polc üres"){
            errorText.text = param1
            progressBar.visibility = View.GONE
        }
        else if(param1 == "Nincs ilyen kód a rendszerben"){
            errorText.text = param1
            progressBar.visibility = View.GONE
        }
        else{
            errorText.text = param1
            progressBar.visibility = View.VISIBLE
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            LoadFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}