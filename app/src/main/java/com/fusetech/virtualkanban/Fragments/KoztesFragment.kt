package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class KoztesFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var childFrame: FrameLayout
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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes, container, false)
        mainActivity = activity as MainActivity
        childFrame = view.data_frame2
        /*val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        childFrame.addView(child)*/
        childFrame.isFocusable = false
        childFrame.isFocusableInTouchMode = false

        return view
    }

}