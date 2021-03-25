package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polc_location.view.*

class PolcLocationFragment : Fragment(), PolcLocationAdapter.PolcItemClickListener {
   private lateinit var recyclerView: RecyclerView
    private var myItems: ArrayList<PolcLocation> = ArrayList()
    private lateinit var setPolcLocation: SetPolcLocation
    private val TAG = "PolcLocationFragment"
    private lateinit var mainActivity: MainActivity

    interface SetPolcLocation{
        fun setPolcLocation(binNumber: String?,selected: Boolean,position: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_polc_location, container, false)
        mainActivity = activity as MainActivity
        val frameLayout = view.myFrameLayout
        val child = layoutInflater.inflate(R.layout.polc_location_header,null)
        frameLayout.addView(child)

        recyclerView = view.polcRecycler
        recyclerView.isEnabled = false
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
        if(recyclerView.isEnabled){
            val value: String? = myItems[position].polc?.trim()
            var isSelected = true
            var pos = position
            setPolcLocation.setPolcLocation(value,isSelected,pos)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setPolcLocation = if(context is SetPolcLocation){
            context as SetPolcLocation
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    fun setRecyclerOn(){
        recyclerView.isEnabled = true
    }
    fun getDataFromList(position: Int, value: Int){
        var quantity = myItems[position].mennyiseg?.toInt()
        myItems[position].mennyiseg = (quantity?.plus(value)).toString()
        recyclerView.adapter?.notifyDataSetChanged()
    }
    fun checkBinIsInTheList(falseBin: String, value: Int){
        for (i in 0 until myItems.size){
            if(myItems[i].polc?.trim() == falseBin){
                var quantity = myItems[i].mennyiseg?.toInt()
                myItems[i].mennyiseg =(quantity?.plus(value)).toString()
                Log.d(TAG, "checkBinIsInTheList: van ilyen")
                recyclerView.adapter?.notifyDataSetChanged()
                break
            }
            else{
                Log.d(TAG, "checkBinIsInTheList: nincs ilyen ${myItems[i].polc}\tfasle bin =  $falseBin")
            }
        }
    }
    fun checkList(bin: String): Boolean{
        for (i in 0 until myItems.size){
            return myItems[i].polc?.trim() == bin
        }
        return false
    }
}