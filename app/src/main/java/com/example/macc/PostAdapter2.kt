package com.example.styleup

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.R
import com.example.macc.retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostAdapter2(private val context: Context, private val postList: List<Post>):
    RecyclerView.Adapter<PostAdapter2.PostViewHolder>()  {
    class PostViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postToolbar: Toolbar = itemView.findViewById(R.id.postToolbar)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        val numberOfLikes: TextView = itemView.findViewById(R.id.numberOfLikes)
        val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        var isLiked = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_2, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // Carica i dati del post nella posizione specifica
        val currentPost = postList[position]

        // Imposta l'immagine del post (aggiungi la logica necessaria per caricare l'immagine)
        holder.postImage.setImageBitmap(currentPost.imageData)

        // Imposta il nome utente
        holder.usernameText.text = currentPost.username

        // Set number of likes
        holder.numberOfLikes.text = currentPost.likes

        if (currentPost.likedByLoggedUser) {
            holder.likeButton.setImageResource(R.drawable.ic_like_filled)
            holder.isLiked = true
        }

        // Like button
        holder.likeButton.setOnClickListener {
            // Inverti lo stato del like
            holder.isLiked = !holder.isLiked

            if (holder.isLiked) {
                // request likePost API
                val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val username = sharedPreferences.getString("username", "")
                val likePostRequest = LikePostRequest(username, currentPost.id)

                val likePostApiService = retrofit.create(LikePostAPI::class.java)
                likePostApiService.likePost(likePostRequest).enqueue(object : Callback<LikePostResponse> {
                    override fun onResponse(call: Call<LikePostResponse>, response: Response<LikePostResponse>) {
                        try {
                            // Access the result using response.body()
                            val result: LikePostResponse? = response.body()

                            // Check if the result is not null before accessing properties
                            result?.let {
                                val status = it.status
                                if (status == 200) {
                                    holder.likeButton.setImageResource(R.drawable.ic_like_filled)
                                    holder.numberOfLikes.text = ((holder.numberOfLikes.text as String).toInt() + 1).toString()
                                }
                                else {
                                    Log.e("PostAdapter", "${it.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PostAdapter", e.toString())
                        }
                    }
                    override fun onFailure(call: Call<LikePostResponse>, t: Throwable) {
                        Log.e("PostAdapter", "${t.message}")
                    }
                })
            }
            else {
                // request unlikePost API
                val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val username = sharedPreferences.getString("username", "")
                val unlikePostRequest = UnlikePostRequest(username, currentPost.id)

                val unlikePostApiService = retrofit.create(UnlikePostAPI::class.java)
                unlikePostApiService.unlikePost(unlikePostRequest).enqueue(object : Callback<UnlikePostResponse> {
                    override fun onResponse(call: Call<UnlikePostResponse>, response: Response<UnlikePostResponse>) {
                        try {
                            // Access the result using response.body()
                            val result: UnlikePostResponse? = response.body()

                            // Check if the result is not null before accessing properties
                            result?.let {
                                val status = it.status
                                if (status == 200) {
                                    holder.likeButton.setImageResource(R.drawable.ic_like) // Icona del like vuota
                                    holder.numberOfLikes.text = ((holder.numberOfLikes.text as String).toInt() - 1).toString()
                                }
                                else {
                                    Log.e("PostAdapter", "${it.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PostAdapter", e.toString())
                        }
                    }
                    override fun onFailure(call: Call<UnlikePostResponse>, t: Throwable) {
                        Log.e("PostAdapter", "${t.message}")
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}
