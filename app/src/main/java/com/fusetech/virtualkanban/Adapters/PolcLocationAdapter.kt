package com.fusetech.virtualkanban.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.polc_location_view.view.*

class PolcLocationAdapter(private var locationItems: ArrayList<PolcLocation>, private val listener: PolcItemClickListener): RecyclerView.Adapter<PolcLocationAdapter.PolcLocationHolder>() {
    var selectedPos = 0
    inner class PolcLocationHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val polcHely: TextView = itemView.polcNameText
        val darabszam: TextView = itemView.dbTxt

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position!= RecyclerView.NO_POSITION) {
                listener.polcItemClick(position)
                notifyItemChanged(selectedPos)
                selectedPos = layoutPosition
                notifyItemChanged(selectedPos)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolcLocationHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.polc_location_view,parent,false)
        return PolcLocationHolder(itemView)
    }
    override fun onBindViewHolder(holder: PolcLocationHolder, position: Int) {
        val currentItem = locationItems[position]
        holder.polcHely.text = currentItem.polc
        holder.darabszam.text = currentItem.mennyiseg
        holder.itemView.setSelected(selectedPos == position);
    }
    override fun getItemCount() = locationItems.size

    interface PolcItemClickListener{
        fun polcItemClick(position: Int)
    }
}