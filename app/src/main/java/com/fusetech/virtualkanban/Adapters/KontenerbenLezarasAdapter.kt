package com.fusetech.virtualkanban.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_item.view.*

class KontenerbenLezarasAdapter(var kontenerCikkLezaras: ArrayList<KontenerbenLezarasItem>):
    RecyclerView.Adapter<KontenerbenLezarasAdapter.KontenerbenLezarasHolder>() {
    class KontenerbenLezarasHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cikkszam = itemView.kontenerbenCikkText
        val megj1 = itemView.kontenerbenMegj1Text
        val megj2 = itemView.kontenerbenMegj2Text
        val intRem = itemView.kontenerbenIntRemText
        val igeny = itemView.kontenerbenIgenyeltText
        val mozgas = itemView.konterbenMozgasText
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KontenerbenLezarasHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.kontenerben_lezaras_item,parent,false)
        return KontenerbenLezarasHolder(itemView)
    }
    override fun onBindViewHolder(holder: KontenerbenLezarasHolder, position: Int) {
        val currentPosition = kontenerCikkLezaras[position]
        holder.cikkszam.text = currentPosition.cikkszam
        holder.megj1.text = currentPosition.megjegyzes1
        holder.megj2.text = currentPosition.megjegyzes2
        holder.intRem.text = currentPosition.intrem
        holder.igeny.text = currentPosition.igeny.toString()
        holder.mozgas.text = currentPosition.kiadva.toString()
    }
    override fun getItemCount() = kontenerCikkLezaras.size
}