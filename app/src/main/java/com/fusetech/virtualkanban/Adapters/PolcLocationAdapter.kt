package com.fusetech.virtualkanban.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.dataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.polc_location_view.view.*

class PolcLocationAdapter(private var locationItems: ArrayList<PolcLocation>, private val listener: PolcItemClickListener): RecyclerView.Adapter<PolcLocationAdapter.PolcLocationHolder>() {

    inner class PolcLocationHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val polcHely: TextView = itemView.polcNameText
        val darabszam: TextView = itemView.dbTxt

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.polcItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolcLocationHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.polc_location_view, parent, false)
        return PolcLocationHolder(itemView)
    }

    override fun onBindViewHolder(holder: PolcLocationHolder, position: Int) {
        val currentItem = locationItems[position]
        if (locationItems[position].mennyiseg.equals("0")) {
            holder.itemView.setBackgroundResource(R.drawable.text_green_selector)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.text_white_selector)
        }
        holder.polcHely.text = currentItem.polc
        holder.darabszam.text = currentItem.mennyiseg
    }

    override fun getItemCount() = locationItems.size

    interface PolcItemClickListener {
        fun polcItemClick(position: Int)
    }
}

