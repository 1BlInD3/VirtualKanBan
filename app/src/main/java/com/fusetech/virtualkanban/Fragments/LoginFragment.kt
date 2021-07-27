package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment : Fragment() {

    private var cancelBtn : Button? = null
    private var idTxt : TextView? = null
    private var progressBar : ProgressBar? = null
    private var myView: View? = null
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_login, container, false)

        progressBar = myView?.loginProgress!!
        progressBar?.visibility = View.GONE
        idTxt = myView?.idText!!
        cancelBtn = myView?.cancelButton!!
        mainActivity  = activity as MainActivity
        mainActivity?.startExitTimer()
        cancelBtn?.setOnClickListener{
            mainActivity?.loadMenuFragment(false)
        }
        return myView
    }
    fun startSpinning(){
        progressBar?.visibility = View.VISIBLE
    }
    fun stopSpinning(){
        progressBar?.visibility = View.GONE
    }
    fun setId(employeeCode: String){
        idTxt?.text = employeeCode
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity = null
        myView = null
        cancelBtn = null
        idTxt = null
        progressBar = null
        mainActivity?.loginFragment = null
    }
}