package com.fusetech.virtualkanban.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.dataItems.CikkItems
import com.fusetech.virtualkanban.R

class CikkItemAdapter (private val cikkList : ArrayList<CikkItems>): RecyclerView.Adapter<CikkItemAdapter.CikkViewHolder>() {
    class CikkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mennyisegText: TextView = itemView.findViewById(R.id.cikkszamHeader)
        val polcText: TextView = itemView.findViewById(R.id.polcText)
        val raktarText: TextView = itemView.findViewById(R.id.desc1Header)
        val allapotText: TextView = itemView.findViewById(R.id.megjegyzesHeader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CikkViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cikk_view,parent,false)
        return CikkViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CikkViewHolder, position: Int) {
        val currentItem = cikkList[position]
        holder.mennyisegText.text = currentItem.mMennyiseg.toString()
        holder.polcText.text = currentItem.mPolc
        holder.raktarText.text = currentItem.mRaktar
        holder.allapotText.text = currentItem.mAllapot
    }

    override fun getItemCount()=cikkList.size
}
