package com.fusetech.virtualkanban.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.dataItems.IgenyItem
import com.fusetech.virtualkanban.R

class IgenyItemAdapter(private var myItemIgeny: ArrayList<IgenyItem>, private val listener: IgenyItemClick): RecyclerView.Adapter<IgenyItemAdapter.IgenyItemAdapterHolder>() {
    var selectedPos = 0
    inner class IgenyItemAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
        val cikkszam: TextView = itemView.findViewById(R.id.cikkszam_igeny)
        val megjegyzes: TextView = itemView.findViewById(R.id.megjegyzes_igeny_view)
        val mertegys: TextView = itemView.findViewById(R.id.mert_igeny)
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position: Int = absoluteAdapterPosition
            if(position!=RecyclerView.NO_POSITION){
                listener.igenyClick(position)
                notifyItemChanged(selectedPos)
                selectedPos = layoutPosition
                notifyItemChanged(selectedPos)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IgenyItemAdapterHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.igeny_item_view,parent,false)
        return IgenyItemAdapterHolder(itemView)
    }
    override fun onBindViewHolder(holder: IgenyItemAdapterHolder, position: Int) {
        val currentPosition = myItemIgeny[position]
        holder.itemView.setBackgroundResource(R.drawable.text_white_selector)
        holder.cikkszam.text = currentPosition.cikkszam
        holder.megjegyzes.text = currentPosition.megnevezes
        holder.mertegys.text = currentPosition.mennyiseg
    }
    override fun getItemCount()=myItemIgeny.size

    interface IgenyItemClick{
        fun igenyClick(position: Int)
    }
}
