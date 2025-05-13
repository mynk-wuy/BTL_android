package com.example.btl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtFullname: TextView = itemView.findViewById(R.id.txtFullname)
        val txtAction: TextView = itemView.findViewById(R.id.txtAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.txtTime.text = "[${item.time}]"
        holder.txtFullname.text = "Người điều khiển: ${item.fullname}"
        holder.txtAction.text = "Thiết bị ${item.device} đã ${if (item.status == "1") "Bật" else "Tắt"}"
    }

    override fun getItemCount(): Int = items.size
}
