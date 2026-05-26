package com.example.lw_3

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter(
    private val videoList: MutableList<Video>,//список відео для відображення
    private val currentUserName: String,
    private val currentUserId: Int,
    private val isAdmin: Boolean,
    private val allUsers: List<User>,//просто список всіх юзерів
    private val onVideoDelete: (Int) -> Unit) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {//колбек функція для видалення


    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVideoTitle: TextView = itemView.findViewById(R.id.txtVideoTitle)
        val btnSendComment: Button = itemView.findViewById(R.id.btnSendComment)

        val editTextComment: EditText = itemView.findViewById(R.id.editTextComment)

        val tvCommentsList: TextView = itemView.findViewById(R.id.tvCommentsList)

        val videoPlayer: VideoView = itemView.findViewById(R.id.VideoPlayer)

        val btnShare: Button = itemView.findViewById(R.id.btnShare)

        val btnDeleteVideo: Button = itemView.findViewById(R.id.btnDeleteVideo)

        val btnClearComments: Button = itemView.findViewById(R.id.btnClearComments)

        val btnLike: Button = itemView.findViewById(R.id.btnLike)
    }

    //створення нової порожної картки (XML макет) і передаємо її у ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    //бере порожню картку і наповнює її реальними даними з об'єкта Video
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val currentVideo = videoList[position]
        holder.tvCommentsList.text = if (currentVideo.comments.isEmpty()) "Немає коментарів" else currentVideo.comments.joinToString("\n") { "${it.author}: ${it.text}" }

        val shareData = currentVideo.sharedvideos.find{it.receiverId == currentUserId}

        if(shareData!= null){
            holder.txtVideoTitle.text = "${currentVideo.name}\nПоділено від: ${shareData.sharedBy}"
        }
        else{
            holder.txtVideoTitle.text = currentVideo.name
        }

        val fixedUrl = currentVideo.url.replace("localhost", "10.0.2.2")
        val videoUri = fixedUrl.toUri()

        holder.videoPlayer.setVideoURI(videoUri)

        val mediaController = android.widget.MediaController(holder.itemView.context)
        mediaController.setAnchorView(holder.videoPlayer)
        holder.videoPlayer.setMediaController(mediaController)
        holder.videoPlayer.setOnErrorListener { _, _, _ ->
            true
        }

        holder.videoPlayer.setOnClickListener {
            if(holder.videoPlayer.isPlaying){
                holder.videoPlayer.pause()
            }
            else{
                holder.videoPlayer.start()
            }
        }

 

        holder.btnLike.text = if (currentVideo.isLiked) "❤️ ${currentVideo.likes}" else "🤍 ${currentVideo.likes}"

        holder.btnLike.setOnClickListener {
            //відправляємо серверу ID користувача, який натиснув лайк
            val requestBody = mapOf("userId" to currentUserId.toString())

            RetrofitClient.instance.toggleLike(currentVideo.id, requestBody).enqueue(object : retrofit2.Callback<Video> {
                override fun onResponse(call: retrofit2.Call<Video>, response: retrofit2.Response<Video>) {
                    if (response.isSuccessful) {
                        val updateVideo = response.body()
                        if(updateVideo != null) {
                            currentVideo.isLiked = !currentVideo.isLiked
                            currentVideo.likes = updateVideo.likes
                            holder.btnLike.text = if (currentVideo.isLiked) "❤️ ${currentVideo.likes}" else "🤍 ${currentVideo.likes}"
                        }
                    }
                }
                override fun onFailure(call: retrofit2.Call<Video>, t: Throwable) {}
            })
        }

        if(isAdmin){
            holder.btnDeleteVideo.visibility = View.VISIBLE
            holder.btnClearComments.visibility = View.VISIBLE
        }
        else{
            holder.btnDeleteVideo.visibility = View.GONE
            holder.btnClearComments.visibility = View.GONE
        }

        holder.btnClearComments.setOnClickListener {
            RetrofitClient.instance.clearComments(currentVideo.id).enqueue(object : retrofit2.Callback<Video> {
                override fun onResponse(call: retrofit2.Call<Video>, response: retrofit2.Response<Video>) {
                    if (response.isSuccessful) {
                        currentVideo.comments.clear()
                        holder.tvCommentsList.text = "Немає коментарів"
                    }
                }
                override fun onFailure(call: retrofit2.Call<Video>, t: Throwable) {}
            })
        }

        holder.btnDeleteVideo.setOnClickListener {
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onVideoDelete(adapterPosition)
            }
        }

        holder.btnShare.setOnClickListener {
            //відфільтровуємо поточного юзера, щоб він не міг поділитися відео сам із собою
            val availableUsers = allUsers.filter { it.name != currentUserName }
            val userNames = availableUsers.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Кому надіслати?")

            builder.setItems(userNames) { _, which ->
                val selectedUser = availableUsers[which]

                val requestBody = mapOf(
                    "receiverId" to selectedUser.id,
                    "senderName" to currentUserName
                )

                RetrofitClient.instance.shareVideo(currentVideo.id, requestBody).enqueue(object : retrofit2.Callback<Map<String, Any>> {
                    override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                        if (response.isSuccessful) {
                            val newShared = SharedVideo(receiverId = selectedUser.id, sharedBy = currentUserName)
                            currentVideo.sharedvideos.add(newShared)

                            Toast.makeText(holder.itemView.context, "Відео надіслано користувачу ${selectedUser.name}", Toast.LENGTH_SHORT).show()
                        } else {

                            Toast.makeText(holder.itemView.context, "Ви вже поділилися цим відео з ${selectedUser.name}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Помилка мережі", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            builder.show()
        }

        holder.btnSendComment.setOnClickListener {
            val commentText = holder.editTextComment.text.toString()
            if (commentText.isNotEmpty()) {
                val requestBody = mapOf(
                    "author" to mapOf("name" to currentUserName),
                    "text" to commentText
                )

                RetrofitClient.instance.addComment(currentVideo.id, requestBody).enqueue(object : retrofit2.Callback<Video> {
                    override fun onResponse(call: retrofit2.Call<Video>, response: retrofit2.Response<Video>) {
                        if (response.isSuccessful) {
                            val newComment = Comment(author = currentUserName, text = commentText)
                            currentVideo.comments.add(newComment)
                            holder.tvCommentsList.text = currentVideo.comments.joinToString("\n") { "${it.author}: ${it.text}" }
                            holder.editTextComment.text.clear()
                            Toast.makeText(holder.itemView.context, "Коментар додано", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(holder.itemView.context, "Помилка сервера", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Video>, t: Throwable) {
                        Toast.makeText(holder.itemView.context, "Помилка мережі", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(holder.itemView.context, "Введіть текст коментаря", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //повідомляє RecyclerView, скільки всього елементів у нас є
    override fun getItemCount(): Int {
        return videoList.size
    }
}