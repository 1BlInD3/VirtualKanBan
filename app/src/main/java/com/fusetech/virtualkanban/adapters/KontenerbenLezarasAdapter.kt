package com.fusetech.virtualkanban.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.dataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R

class KontenerbenLezarasAdapter(var kontenerCikkLezaras: ArrayList<KontenerbenLezarasItem>, val listener: onItemClickListener):
    RecyclerView.Adapter<KontenerbenLezarasAdapter.KontenerbenLezarasHolder>() {
    inner class KontenerbenLezarasHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val cikkszam: TextView = itemView.findViewById(R.id.kontenerbenCikkText)
        val megj1: TextView = itemView.findViewById(R.id.kontenerbenMegj1Text)
        val megj2: TextView = itemView.findViewById(R.id.kontenerbenMegj2Text)
        val intRem: TextView = itemView.findViewById(R.id.kontenerbenIntRemText)
        val igeny: TextView = itemView.findViewById(R.id.kontenerbenIgenyeltText)
        val mozgas: TextView = itemView.findViewById(R.id.konterbenMozgasText)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = absoluteAdapterPosition
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
            holder.itemView.setBackgroundResource(R.drawable.text_blue_selector)
            holder.cikkszam.setTextColor(Color.parseColor("#FFFFFF"))
            holder.megj1.setTextColor(Color.parseColor("#FFFFFF"))
            holder.megj2.setTextColor(Color.parseColor("#FFFFFF"))
            holder.intRem.setTextColor(Color.parseColor("#FFFFFF"))
            holder.igeny.setTextColor(Color.parseColor("#FFFFFF"))
            holder.mozgas.setTextColor(Color.parseColor("#FFFFFF"))
        } else if (kontenerCikkLezaras[position].statusz == 3) {
            holder.itemView.setBackgroundResource(R.drawable.text_green_selector)
            holder.cikkszam.setTextColor(Color.parseColor("#000000"))
            holder.megj1.setTextColor(Color.parseColor("#000000"))
            holder.megj2.setTextColor(Color.parseColor("#000000"))
            holder.intRem.setTextColor(Color.parseColor("#000000"))
            holder.igeny.setTextColor(Color.parseColor("#000000"))
            holder.mozgas.setTextColor(Color.parseColor("#000000"))
        } else if (kontenerCikkLezaras[position].statusz < 2 && kontenerCikkLezaras[position].balance == 0.0){
            Log.d("BVH", "onBindViewHolder: PIROS_JÓ")
            holder.itemView.setBackgroundResource(R.drawable.text_red_selector)
            holder.cikkszam.setTextColor(Color.parseColor("#FFFFFF"))
            holder.megj1.setTextColor(Color.parseColor("#FFFFFF"))
            holder.megj2.setTextColor(Color.parseColor("#FFFFFF"))
            holder.intRem.setTextColor(Color.parseColor("#FFFFFF"))
            holder.igeny.setTextColor(Color.parseColor("#FFFFFF"))
            holder.mozgas.setTextColor(Color.parseColor("#FFFFFF"))
        }
        else {
            holder.itemView.setBackgroundResource(R.drawable.text_white_selector)
            holder.cikkszam.setTextColor(Color.parseColor("#000000"))
            holder.megj1.setTextColor(Color.parseColor("#000000"))
            holder.megj2.setTextColor(Color.parseColor("#000000"))
            holder.intRem.setTextColor(Color.parseColor("#000000"))
            holder.igeny.setTextColor(Color.parseColor("#000000"))
            holder.mozgas.setTextColor(Color.parseColor("#000000"))
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
