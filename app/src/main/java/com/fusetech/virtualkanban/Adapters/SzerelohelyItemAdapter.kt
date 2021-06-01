package com.fusetech.virtualkanban.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.igeny_kihelyezes_item.view.*

class SzerelohelyItemAdapter (val szereloLista : ArrayList<SzerelohelyItem>) :RecyclerView.Adapter<SzerelohelyItemAdapter.SzereloHelyItemViewHolder>() {
    class SzereloHelyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val szereloText = itemView.szerelohely
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