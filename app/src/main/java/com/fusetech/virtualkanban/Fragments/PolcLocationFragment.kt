package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polc_location.view.*

class PolcLocationFragment : Fragment(), PolcLocationAdapter.PolcItemClickListener {
   private lateinit var recyclerView: RecyclerView
    private var myItems: ArrayList<PolcLocation> = ArrayList()
    private lateinit var setPolcLocation: SetPolcLocation

    interface SetPolcLocation{
        fun setPolcLocation(binNumber: String?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_polc_location, container, false)

        val frameLayout = view.myFrameLayout
        val child = layoutInflater.inflate(R.layout.polc_location_header,null)
        frameLayout.addView(child)

        recyclerView = view.polcRecycler
        myItems.clear()
        loadData()
        recyclerView.adapter = PolcLocationAdapter(myItems, this)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)

        return view
    }
    fun loadData(){
        val myList:ArrayList<PolcLocation>? = arguments?.getSerializable("02RAKTAR") as? ArrayList<PolcLocation>
        if (myList != null) {
            for(i in myList){
                myItems.add(PolcLocation(i.polc,i.mennyiseg))
            }
        }
    }

    override fun polcItemClick(position: Int) {
        //Toast.makeText(view?.context,"Positiion $position",Toast.LENGTH_SHORT).show()
        val value = myItems[position].polc
        var isSelected = true
        var pos = position
        setPolcLocation.setPolcLocation(value)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setPolcLocation = if(context is SetPolcLocation){
            context as SetPolcLocation
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
}