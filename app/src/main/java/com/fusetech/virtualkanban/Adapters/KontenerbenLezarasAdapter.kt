package com.fusetech.virtualkanban.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_item.view.*

class KontenerbenLezarasAdapter(var kontenerCikkLezaras: ArrayList<KontenerbenLezarasItem>, val listener: onItemClickListener):
    RecyclerView.Adapter<KontenerbenLezarasAdapter.KontenerbenLezarasHolder>() {
    inner class KontenerbenLezarasHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val cikkszam = itemView.kontenerbenCikkText
        val megj1 = itemView.kontenerbenMegj1Text
        val megj2 = itemView.kontenerbenMegj2Text
        val intRem = itemView.kontenerbenIntRemText
        val igeny = itemView.kontenerbenIgenyeltText
        val mozgas = itemView.konterbenMozgasText
        init{
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KontenerbenLezarasHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.kontenerben_lezaras_item,parent,false)
        return KontenerbenLezarasHolder(itemView)
    }
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: KontenerbenLezarasHolder, position: Int) {
        val currentPosition = kontenerCikkLezaras[position]
        if(kontenerCikkLezaras[position].statusz == 2){
            holder.cikkszam.setBackgroundColor(R.color.vikings)
            holder.megj1.setBackgroundColor(R.color.vikings)
            holder.megj2.setBackgroundColor(R.color.vikings)
            holder.intRem.setBackgroundColor(R.color.vikings)
            holder.igeny.setBackgroundColor(R.color.vikings)
            holder.mozgas.setBackgroundColor(R.color.vikings)
        }else if(kontenerCikkLezaras[position].statusz == 3){
            holder.cikkszam.setBackgroundColor(Color.GREEN)
            holder.megj1.setBackgroundColor(Color.GREEN)
            holder.megj2.setBackgroundColor(Color.GREEN)
            holder.intRem.setBackgroundColor(Color.GREEN)
            holder.igeny.setBackgroundColor(Color.GREEN)
            holder.mozgas.setBackgroundColor(Color.GREEN)
        }
        holder.cikkszam.text = currentPosition.cikkszam
        holder.megj1.text = currentPosition.megjegyzes1
        holder.megj2.text = currentPosition.megjegyzes2
        holder.intRem.text = currentPosition.intrem
        holder.igeny.text = currentPosition.igeny.toString()
        holder.mozgas.text = currentPosition.kiadva.toString()
    }
    override fun getItemCount() = kontenerCikkLezaras.size

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
}