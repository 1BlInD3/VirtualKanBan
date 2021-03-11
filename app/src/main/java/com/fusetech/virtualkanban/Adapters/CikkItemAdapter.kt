package com.fusetech.virtualkanban.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.DataItems.CikkItems
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.cikk_view.view.*

class CikkItemAdapter (private val cikkList : ArrayList<CikkItems>): RecyclerView.Adapter<CikkItemAdapter.CikkViewHolder>() {
    class CikkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mennyisegText = itemView.cikkszamHeader
        val polcText = itemView.polcText
        val raktarText = itemView.desc1Header
        val allapotText = itemView.megjegyzesHeader
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