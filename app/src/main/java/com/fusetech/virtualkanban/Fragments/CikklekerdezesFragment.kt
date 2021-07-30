package com.fusetech.virtualkanban.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_cikklekerdezes.view.*

private const val TAG = "CikklekerdezesFragment"

class CikklekerdezesFragment : Fragment() {

    private var editText: EditText? = null
    private var frame: FrameLayout? = null
    private lateinit var setItemOrBinManually: SetItemOrBinManually
    private var mainActivity: MainActivity? = null
    private var myView: View? = null

    interface SetItemOrBinManually{
        fun setValue(value: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_cikklekerdezes, container, false)
        mainActivity = activity as MainActivity
        frame = myView?.cikk_container!!
        editText = myView?.binOrItemText!!
        editText?.setSelection(editText?.text?.length!!)
        editText?.filters = arrayOf<InputFilter>(AllCaps())
        editText?.requestFocus()

        editText?.setOnClickListener{
            setItemOrBinManually.setValue(editText?.text.toString().trim())
            editText?.setSelection(editText?.text?.length!!)
            editText?.selectAll()
            editText?.requestFocus()
        }

        return myView
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        setItemOrBinManually = if (context is SetItemOrBinManually) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    fun setBinOrItem(code: String){
        editText?.setText(code)
        editText?.setSelection(editText?.text?.length!!)
        editText?.selectAll()
        editText?.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        myView = null
        frame = null
        editText = null
        editText?.filters = null
        mainActivity?.cikklekerdezesFragment = null
        mainActivity = null

    }
}