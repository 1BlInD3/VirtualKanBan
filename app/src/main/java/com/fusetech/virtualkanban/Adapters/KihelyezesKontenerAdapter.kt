package com.fusetech.virtualkanban.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KihelyezesKontenerElemek
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kihelyezes_item.view.*

class KihelyezesKontenerAdapter(val lista: ArrayList<KihelyezesKontenerElemek>, val listener: KihelyezesListener):
    RecyclerView.Adapter<KihelyezesKontenerAdapter.KihelyezesViewHolder>() {
    inner class KihelyezesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val cikkszam = itemView.textView33
        val megj1 = itemView.textView34
        val megj2 = itemView.textView38
        val intrem = itemView.textView36
        val igeny = itemView.textView37
        val kiadva = itemView.textView35

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.kihelyezesClick(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KihelyezesViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.kihelyezes_item, parent, false)
        return KihelyezesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: KihelyezesViewHolder, position: Int) {
        val currentItem = lista[position]
        if (lista[position].kiadva == 0) {
            holder.itemView.setBackgroundResource(R.drawable.red_select)
            /*holder.cikkszam.setBackgroundResource(R.drawable.red_select)
            holder.megj1.setBackgroundResource(R.drawable.red_select)
            holder.megj2.setBackgroundResource(R.drawable.red_select)
            holder.intrem.setBackgroundResource(R.drawable.red_select)
            holder.igeny.setBackgroundResource(R.drawable.red_select)
            holder.kiadva.setBackgroundResource(R.drawable.red_select)*/
        } else {
            holder.itemView.setBackgroundResource(R.drawable.highlight_selected2)
            /*holder.cikkszam.setBackgroundResource(R.drawable.highlight_selected2)
            holder.megj1.setBackgroundResource(R.drawable.highlight_selected2)
            holder.megj2.setBackgroundResource(R.drawable.highlight_selected2)
            holder.intrem.setBackgroundResource(R.drawable.highlight_selected2)
            holder.igeny.setBackgroundResource(R.drawable.highlight_selected2)
            holder.kiadva.setBackgroundResource(R.drawable.highlight_selected2)*/
        }
        holder.cikkszam.text = currentItem.vonalkod
        holder.megj1.text = currentItem.megjegyzes1
        holder.megj2.text = currentItem.megjegyzes2
        holder.intrem.text = currentItem.intrem
        holder.igeny.text = currentItem.igenyelve
        holder.kiadva.text = currentItem.kiadva.toString()
    }

    override fun getItemCount() = lista.size

    interface KihelyezesListener{
        fun kihelyezesClick(pos: Int)
    }
}