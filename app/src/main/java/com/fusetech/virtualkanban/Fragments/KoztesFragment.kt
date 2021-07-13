package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes.view.*


class KoztesFragment : Fragment() {

    private lateinit var childFrame: FrameLayout
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes, container, false)
        mainActivity = activity as MainActivity
        childFrame = view.data_frame2
        childFrame.isFocusable = false
        childFrame.isFocusableInTouchMode = false

        return view
    }

}