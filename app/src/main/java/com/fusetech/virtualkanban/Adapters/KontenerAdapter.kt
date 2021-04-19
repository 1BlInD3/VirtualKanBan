package com.fusetech.virtualkanban.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.konteneres_item.view.*

class KontenerAdapter(var kontenerItem: ArrayList<KontenerItem>, val listener: onKontenerClickListener) : RecyclerView.Adapter<KontenerAdapter.KontenerHolder>(){
    inner class KontenerHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        val kontenerText = itemView.child_kontener_text
        val polcText = itemView.child_polc_text
        val idoText = itemView.child_ido_text
        val tetelText = itemView.child_tetelszam_text

        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onKontenerClick(position)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KontenerHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.konteneres_item,parent,false)
        return KontenerHolder(itemView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: KontenerHolder, position: Int) {
        val currentItem = kontenerItem[position]
        if(kontenerItem[position].status == 2){
            //holder.kontenerText.setBackgroundColor(Color.RED)
            holder.kontenerText.setBackgroundColor(R.color.vikings)
            holder.polcText.setBackgroundColor(R.color.vikings)
            holder.idoText.setBackgroundColor(R.color.vikings)
            holder.tetelText.setBackgroundColor(R.color.vikings)
        }
        holder.kontenerText.text = currentItem.kontener
        holder.polcText.text = currentItem.polc
        holder.idoText.text = currentItem.datum
        holder.tetelText.text = currentItem.tetelszam.toString()
    }

    override fun getItemCount() = kontenerItem.size

    interface onKontenerClickListener{
        fun onKontenerClick(position: Int)
    }
}