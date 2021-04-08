package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_lezaras.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontenerLezarasFragment : Fragment() {
    private lateinit var dataFrame: FrameLayout
    private lateinit var childRecycler: RecyclerView
    private var kontenerList: ArrayList<KontenerItem> = ArrayList()
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_lezaras, container, false)
        dataFrame = view.data_frame1
        val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        dataFrame.addView(child)
        childRecycler = child.child_recycler
        childRecycler.adapter = KontenerAdapter(kontenerList)
        childRecycler.layoutManager = LinearLayoutManager(child.context)
        childRecycler.setHasFixedSize(true)

        kontenerList.add(KontenerItem("255653","NNG02","2021.04.01 14:52:02",5))
        kontenerList.add(KontenerItem("255653","NNG02","2021.04.01 14:52:02",5))
        kontenerList.add(KontenerItem("255653","NNG02","2021.04.01 14:52:02",5))

        childRecycler.adapter?.notifyDataSetChanged()

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IgenyKontenerLezarasFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerLezarasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}