package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_load.view.*

private const val ARG_PARAM1 = "param1"

class LoadFragment : Fragment() {
    private var param1: String? = null
    private var myView: View? = null
    private var errorText: TextView? = null
    private var progressBar: ProgressBar? = null
    private var mainActivity: MainActivity? = null
    private var mlayout: ConstraintLayout? = null
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
        myView = inflater.inflate(R.layout.fragment_load, container, false)
        errorText = myView?.errorText
        progressBar = myView?.progressBar
        mainActivity = activity as MainActivity
        mlayout = myView?.mlayout

        if(param1 == ""){
            errorText?.text = "Hibás bevitel"
            progressBar?.visibility = View.GONE
        }
        else if(param1 == "A feldolgozás során hiba lépett fel"){
            errorText?.text = param1
            progressBar?.visibility = View.GONE
        }
        else if(param1 == "A polc üres"){
            errorText?.text = param1
            progressBar?.visibility = View.GONE
        }
        else if(param1 == "Nincs ilyen kód a rendszerben"){
            errorText?.text = param1
            progressBar?.visibility = View.GONE
        }
        else{
            errorText?.text = param1
            progressBar?.visibility = View.VISIBLE
        }

        return myView
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
    fun clearLeak(){
        myView = null
        errorText = null
        progressBar = null
        mlayout = null
        mainActivity?.removeFragment("LRF")
        mainActivity?.loadFragment = null
    }
}