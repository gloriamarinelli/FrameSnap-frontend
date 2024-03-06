package com.example.styleup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(
    private val friendList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendNameTextView: TextView = itemView.findViewById(R.id.friendNameTextView)
        val friendImageView: ImageView = itemView.findViewById(R.id.friendImageView)

        init {
            itemView.setOnClickListener {
                onItemClick(friendList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_card_layout, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friendName = friendList[position]
        holder.friendNameTextView.text = friendName
        // Carica l'immagine dell'amico (se necessario)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
}