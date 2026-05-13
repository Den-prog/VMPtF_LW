package com.example.lw_3

import android.app.AlertDialog
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class VideoAdapter(
    private val videoList: MutableList<Video>,
    private val currentUserName: String,
    private val isAdmin: Boolean,
    private val onVideoDelete: (Int) -> Unit) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val mockUsers = listOf(
        User(2, "Іван", false),
        User(3, "Марія", false),

    )
    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtVideoTitle: TextView = itemView.findViewById(R.id.txtVideoTitle)
        val btnLike: Button = itemView.findViewById(R.id.btnLike)
        val btnSendComment: Button = itemView.findViewById(R.id.btnSendComment)

        val editTextComment: EditText = itemView.findViewById(R.id.editTextComment)

        val tvCommentsList: TextView = itemView.findViewById(R.id.tvCommentsList)

        val VideoPLayer: VideoView = itemView.findViewById(R.id.VideoPlayer)

        val btnShare: Button = itemView.findViewById(R.id.btnShare)

        val btnDeleteVideo: Button = itemView.findViewById(R.id.btnDeleteVideo)

        val btnClearComments: Button = itemView.findViewById(R.id.btnClearComments)
    }

    //створення нової порожної картки (XML макет) і передаємо її у ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    //бере порожню картку і наповнює її реальними даними з об'єкта Video
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {


        val currentVideo = videoList[position]
        holder.tvCommentsList.text = currentVideo.comments.joinToString("\n")
        holder.txtVideoTitle.text = currentVideo.name
        val videoUri = android.net.Uri.parse(currentVideo.url)

        holder.VideoPLayer.setVideoURI(videoUri)

        val mediaController = android.widget.MediaController(holder.itemView.context)
        mediaController.setAnchorView(holder.VideoPLayer)
        holder.VideoPLayer.setMediaController(mediaController)
        holder.VideoPLayer.setOnErrorListener { mp, what, extra ->
            true
        }

        holder.VideoPLayer.setOnClickListener {
            if(holder.VideoPLayer.isPlaying){
                holder.VideoPLayer.pause()
            }
            else{
                holder.VideoPLayer.start()
            }
        }

        if(currentVideo.isLiked){
            holder.btnLike.text = "Прибрати лайк"
        }
        else{
            holder.btnLike.text = "Поставити лайк"
        }
        holder.btnLike.setOnClickListener {
            currentVideo.isLiked = !currentVideo.isLiked
            if(currentVideo.isLiked){
                holder.btnLike.text = "Прибрати лайк"
            }
            else{
                holder.btnLike.text = "Поставити лайк"
            }

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
            currentVideo.comments.clear()
            holder.tvCommentsList.text = "Немає коментарів"
        }

        holder.btnDeleteVideo.setOnClickListener {
            onVideoDelete(position)
        }

        holder.btnShare.setOnClickListener {
            val userNames = mockUsers.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Кому надіслати?")
            builder.setItems(userNames) { _, which ->
                val selectedUser = mockUsers[which]


                if (!currentVideo.sharedVideos.contains(selectedUser.id)) {
                    currentVideo.sharedVideos.add(selectedUser.id)
                }

                Toast.makeText(holder.itemView.context,
                    "Відео '${currentVideo.name}' надіслано користувачу ${selectedUser.name}",
                    Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        holder.btnSendComment.setOnClickListener {
            val userInput = holder.editTextComment.text.toString()

            if(userInput.isNotBlank()){
                val formattedComment = "$currentUserName: $userInput"
                currentVideo.comments.add(formattedComment)
                holder.tvCommentsList.text = currentVideo.comments.joinToString("\n")

                holder.editTextComment.text.clear()
            }


        }


    }

    //повідомляє RecyclerView, скільки всього елементів у нас є
    override fun getItemCount(): Int {
        return videoList.size

    }


}