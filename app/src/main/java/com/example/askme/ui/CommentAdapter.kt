package com.example.askme.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askme.R
import com.example.askme.commentClass
import com.example.askme.ui.home.MyAdapter
import com.ms.square.android.expandabletextview.ExpandableTextView
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private val commentList : ArrayList<commentClass>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.comment_item,
            parent,false)
        return CommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentAdapter.CommentViewHolder, position: Int) {
        val currentitem = commentList[position]
        holder.name.text = currentitem.pubName
        holder.date.text = currentitem.date
        holder.commentText.text = currentitem.comment
        Glide.with(context).load(currentitem.profileURL).into(holder.profileImage)
    }


    class CommentViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val name : TextView = itemView.findViewById(R.id.commentedBy)
        val commentText : TextView = itemView.findViewById(R.id.comment_text)
        val date : TextView = itemView.findViewById(R.id.commentDate)
        val profileImage: CircleImageView = itemView.findViewById(R.id.Profile_image)
    }
    override fun getItemCount(): Int {
        return commentList.size
    }
}