package com.fusetech.virtualkanban.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KihelyezesKontenerElemek
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kihelyezes_item.view.*

class KihelyezesKontenerAdapter(val lista: ArrayList<KihelyezesKontenerElemek>):
    RecyclerView.Adapter<KihelyezesKontenerAdapter.KihelyezesViewHolder>() {
    class KihelyezesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
     val cikkszam = itemView.textView33
     val megj1 = itemView.textView34
     val megj2 = itemView.textView35
     val intrem = itemView.textView36
     val igeny = itemView.textView37
     val kiadva = itemView.textView38
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KihelyezesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.kihelyezes_item,parent,false)
        return KihelyezesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: KihelyezesViewHolder, position: Int) {
        val currentItem = lista[position]
        if(lista[position].kiadva == 0){
            holder.cikkszam.setBackgroundColor(Color.RED)
            holder.megj1.setBackgroundColor(Color.RED)
            holder.megj2.setBackgroundColor(Color.RED)
            holder.intrem.setBackgroundColor(Color.RED)
            holder.igeny.setBackgroundColor(Color.RED)
            holder.kiadva.setBackgroundColor(Color.RED)
        }
        holder.cikkszam.text = currentItem.vonalkod
        holder.megj1.text = currentItem.megjegyzes1
        holder.megj2.text = currentItem.megjegyzes2
        holder.intrem.text = currentItem.intrem
        holder.igeny.text = currentItem.igenyelve
        holder.kiadva.text = currentItem.kiadva.toString()
    }

    override fun getItemCount() = lista.size
}