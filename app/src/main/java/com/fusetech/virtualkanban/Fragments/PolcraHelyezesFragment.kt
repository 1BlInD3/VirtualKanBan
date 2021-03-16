package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.*
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.view.*
import java.text.SimpleDateFormat
import java.util.*


class PolcraHelyezesFragment : Fragment() {
    private lateinit var dateText: TextView
    private lateinit var cikkText: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_polcra_helyezes, container, false)
        val simpleDate = SimpleDateFormat("yyyy.MM.dd",Locale.getDefault())
        val currentDate = simpleDate.format(Date())
        dateText = view.dateTxt
        dateText.text = currentDate
        cikkText = view.cikkEditTxt
        cikkText.requestFocus()
        return view
    }
}