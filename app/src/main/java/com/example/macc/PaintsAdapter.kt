package com.example.macc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaintsAdapter(private val paintsList: List<Paint>, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<PaintsAdapter.PaintViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaintViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paint, parent, false)
        return PaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaintViewHolder, position: Int) {
        val paint = paintsList[position]
        holder.paintImage.setImageBitmap(paint.paint)
        holder.paintName.text = paint.paint_name

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(paint)
        }
    }

    override fun getItemCount(): Int {
        return paintsList.size
    }

    class PaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paintImage: ImageView = itemView.findViewById(R.id.paintImage)
        val paintName: TextView = itemView.findViewById(R.id.paintName)
        val paintYear: TextView = itemView.findViewById(R.id.paintYear)
    }

    interface OnItemClickListener {
        fun onItemClick(paint: Paint)
    }
}