package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_ellenorzo_kod.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "EllenorzoKodFragment"


class EllenorzoKodFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var ellenorzoKod: EditText? = null
    private var ellenorzoProgress: ProgressBar? = null
    private var myView: View? = null

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
        myView  = inflater.inflate(R.layout.fragment_ellenorzo_kod, container, false)

        ellenorzoKod = myView?.ellenorzoEdit
        ellenorzoProgress = myView?.ellenorzoProgress
        setProgressBarOff()
        ellenorzoKod?.requestFocus()

        return myView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EllenorzoKodFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun setProgressBarOff(){
        ellenorzoProgress?.visibility = View.GONE
    }
    fun setProgressBarOn(){
        ellenorzoProgress?.visibility = View.VISIBLE
    }
    fun setCode(code: String){
        ellenorzoKod?.selectAll()
        ellenorzoKod?.setText(code)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        ellenorzoKod = null
        ellenorzoProgress = null
        myView = null
    }
}