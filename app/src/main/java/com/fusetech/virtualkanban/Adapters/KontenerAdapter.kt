package com.fusetech.virtualkanban.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.konteneres_item.view.*

class KontenerAdapter(var kontenerItem: ArrayList<KontenerItem>) : RecyclerView.Adapter<KontenerAdapter.KontenerHolder>() {
    class KontenerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kontenerText = itemView.child_kontener_text
        val polcText = itemView.child_polc_text
        val idoText = itemView.child_ido_text
        val tetelText = itemView.child_tetelszam_text
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KontenerHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.konteneres_item,parent,false)
        return KontenerHolder(itemView)
    }

    override fun onBindViewHolder(holder: KontenerHolder, position: Int) {
        val currentItem = kontenerItem[position]
        holder.kontenerText.text = currentItem.kontener
        holder.polcText.text = currentItem.polc
        holder.idoText.text = currentItem.datum
        holder.tetelText.text = currentItem.tetelszam.toString()
    }

    override fun getItemCount() = kontenerItem.size

}