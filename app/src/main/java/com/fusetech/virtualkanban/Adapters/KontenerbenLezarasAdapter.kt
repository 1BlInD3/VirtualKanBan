package com.fusetech.virtualkanban.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_item.view.*

class KontenerbenLezarasAdapter(var kontenerCikkLezaras: ArrayList<KontenerbenLezarasItem>, val listener: onItemClickListener):
    RecyclerView.Adapter<KontenerbenLezarasAdapter.KontenerbenLezarasHolder>() {
    inner class KontenerbenLezarasHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val cikkszam = itemView.kontenerbenCikkText
        val megj1 = itemView.kontenerbenMegj1Text
        val megj2 = itemView.kontenerbenMegj2Text
        val intRem = itemView.kontenerbenIntRemText
        val igeny = itemView.kontenerbenIgenyeltText
        val mozgas = itemView.konterbenMozgasText

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KontenerbenLezarasHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.kontenerben_lezaras_item, parent, false)
        return KontenerbenLezarasHolder(itemView)
    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: KontenerbenLezarasHolder, position: Int) {
        val currentPosition = kontenerCikkLezaras[position]
        if (kontenerCikkLezaras[position].statusz == 2) {
            /*holder.cikkszam.setBackgroundResource(R.drawable.blue_select)
            holder.megj1.setBackgroundResource(R.drawable.blue_select)
            holder.megj2.setBackgroundResource(R.drawable.blue_select)
            holder.intRem.setBackgroundResource(R.drawable.blue_select)
            holder.igeny.setBackgroundResource(R.drawable.blue_select)
            holder.mozgas.setBackgroundResource(R.drawable.blue_select)*/
            holder.itemView.setBackgroundResource(R.drawable.blue_select)
        } else if (kontenerCikkLezaras[position].statusz == 3) {
            /*holder.cikkszam.setBackgroundResource(R.drawable.color_green)
            holder.megj1.setBackgroundResource(R.drawable.color_green)
            holder.megj2.setBackgroundResource(R.drawable.color_green)
            holder.intRem.setBackgroundResource(R.drawable.color_green)
            holder.igeny.setBackgroundResource(R.drawable.color_green)
            holder.mozgas.setBackgroundResource(R.drawable.color_green)*/
            holder.itemView.setBackgroundResource(R.drawable.color_green)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.highlight_selected2)
            /*holder.cikkszam.setBackgroundResource(R.drawable.highlight_selected2)
            holder.megj1.setBackgroundResource(R.drawable.highlight_selected2)
            holder.megj2.setBackgroundResource(R.drawable.highlight_selected2)
            holder.intRem.setBackgroundResource(R.drawable.highlight_selected2)
            holder.igeny.setBackgroundResource(R.drawable.highlight_selected2)
            holder.mozgas.setBackgroundResource(R.drawable.highlight_selected2)*/
        }
        holder.cikkszam.text = currentPosition.cikkszam
        holder.megj1.text = currentPosition.megjegyzes1
        holder.megj2.text = currentPosition.megjegyzes2
        holder.intRem.text = currentPosition.intrem
        holder.igeny.text = currentPosition.igeny.toString()
        holder.mozgas.text = currentPosition.kiadva.toString()
    }

    override fun getItemCount() = kontenerCikkLezaras.size

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
}