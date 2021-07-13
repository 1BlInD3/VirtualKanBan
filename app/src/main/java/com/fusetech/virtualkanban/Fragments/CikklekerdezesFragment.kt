package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_cikklekerdezes.view.*


class CikklekerdezesFragment : Fragment() {

    private lateinit var editText: EditText
    private lateinit var setItemOrBinManually: SetItemOrBinManually

    interface SetItemOrBinManually{
        fun setValue(value: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cikklekerdezes, container, false)

        editText = view.binOrItemText
        editText.setSelection(editText.text.length)
        editText.filters = arrayOf<InputFilter>(AllCaps())
        editText.requestFocus()

        editText.setOnClickListener{
            setItemOrBinManually.setValue(editText.text.toString().trim())
            editText.setSelection(editText.text.length)
            editText.selectAll()
            editText.requestFocus()
        }

        return view
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
        editText.setText(code)
        editText.setSelection(editText.text.length)
        editText.selectAll()
        editText.requestFocus()
    }
}