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

    private lateinit var cancelBtn : Button
    private lateinit var idTxt : TextView
    private lateinit var progressBar : ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        progressBar = view.loginProgress
        progressBar.visibility = View.GONE
        idTxt = view.idText
        cancelBtn = view.cancelButton
        val mainActivity : MainActivity = activity as MainActivity

        cancelBtn.setOnClickListener{
            mainActivity.loadMenuFragment(false)
        }
        return view
    }
    fun startSpinning(){
        progressBar.visibility = View.VISIBLE
    }
    fun stopSpinning(){
        progressBar.visibility = View.GONE
    }
    fun setId(employeeCode: String){
        idTxt.text = employeeCode
    }
}