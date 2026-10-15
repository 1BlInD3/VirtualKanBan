package com.fusetech.virtualkanban.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.dataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R

class SzerelohelyItemAdapter (val szereloLista : ArrayList<SzerelohelyItem>) :RecyclerView.Adapter<SzerelohelyItemAdapter.SzereloHelyItemViewHolder>() {
    class SzereloHelyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val szereloText: TextView = itemView.findViewById(R.id.szerelohely)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SzereloHelyItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.igeny_kihelyezes_item,parent,false)
        return SzereloHelyItemViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: SzereloHelyItemViewHolder, position: Int) {
        val currentItem = szereloLista[position]
        holder.szereloText.text = currentItem.szerelohely
    }
    override fun getItemCount() = szereloLista.size
}
