package com.fusetech.virtualkanban.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.dataItems.PolcItems
import kotlinx.android.synthetic.main.polc_view.view.*

class MozgasAdapter(private var myPolcItems: ArrayList<PolcItems>, val listener: CurrentSelection) :
    RecyclerView.Adapter<MozgasAdapter.PolcItemViewHolder>() {
    inner class PolcItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mennyisegText: TextView = itemView.cikkszamHeader
        val unitText: TextView = itemView.polcText
        val megnevezes1Text: TextView = itemView.desc1Header
        val megnevezes2Text: TextView = itemView.megnevezes2
        val intRemText: TextView = itemView.mennyisegHeader
        val allapotText: TextView = itemView.megjegyzesHeader
        val cikkszamText: TextView = itemView.cikkszam
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = absoluteAdapterPosition
            if(position!=RecyclerView.NO_POSITION){
                listener.onCurrentClick(position)
                itemView.isSelected = !itemView.isSelected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolcItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.polc_view_2, parent, false)
        return PolcItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PolcItemViewHolder, position: Int) {
        val currentItem = myPolcItems[position]
        holder.mennyisegText.text = currentItem.mMennyiseg.toString()
        holder.unitText.text = currentItem.mEgyseg
        holder.megnevezes1Text.text = currentItem.mMegnevezes1
        holder.megnevezes2Text.text = currentItem.mMegnevezes2
        holder.intRemText.text = currentItem.mIntRem
        holder.allapotText.text = currentItem.mAllapot
        holder.cikkszamText.text = currentItem.mCikk
    }

    override fun getItemCount() = myPolcItems.size

    interface CurrentSelection{
        fun onCurrentClick(position: Int)
    }

}