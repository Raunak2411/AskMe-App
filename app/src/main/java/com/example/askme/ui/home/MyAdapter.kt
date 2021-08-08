package com.example.askme.ui.home

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askme.MainActivity
import com.example.askme.R
import com.example.askme.questionClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ms.square.android.expandabletextview.ExpandableTextView
import de.hdodenhof.circleimageview.CircleImageView

class MyAdapter(private val questionList : ArrayList<questionClass>, val commentNav: (String)->Unit) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.question_post_item,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val onlineUserID: String = auth.currentUser!!.uid

        val currentitem = questionList[position]

        val post_id: String = currentitem.postID!!

        holder.name.text = currentitem.pubName
        holder.date.text = currentitem.date
        holder.question.text = currentitem.question
        holder.questionExpandable.text = currentitem.question
        Glide.with(context).load(currentitem.profileURL).into(holder.profileImage)
        if(currentitem.questionURL==null){
            holder.QuestionImage.visibility = View.GONE
        }
        else{
            holder.QuestionImage.visibility = View.VISIBLE
            Glide.with(context).load(currentitem.questionURL).into(holder.QuestionImage)
        }

        if(currentitem.publisherID==onlineUserID){
            holder.moreImage.visibility = View.VISIBLE
            holder.moreImage.setOnClickListener{ it ->
                val popupMenu = PopupMenu(context,it)
                popupMenu.inflate(R.menu.post_menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener{ menuItem ->
                    when(menuItem.itemId){
                        R.id.delete -> {
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Alert")
                                .setMessage("Do you want to delete this post?")
                                .setCancelable(true)
                                .setPositiveButton("Yes"){ dialogInterface, x ->
                                    FirebaseDatabase.getInstance().getReference("likes").child(post_id).removeValue()
                                    FirebaseDatabase.getInstance().getReference("dislikes").child(post_id).removeValue()
                                    FirebaseDatabase.getInstance().getReference("comments").child(post_id).removeValue()
                                    FirebaseDatabase.getInstance().getReference("userPosts").child(onlineUserID).child(post_id).removeValue()
                                    FirebaseDatabase.getInstance().getReference("questions").child(post_id).removeValue()
                                    FirebaseDatabase.getInstance().getReference("saved_post-user").child(post_id).get().addOnSuccessListener{ dataSnapshot ->
                                        val dbref_saved = FirebaseDatabase.getInstance().getReference("saved")
                                        for(snapshot in dataSnapshot.children){
                                            val userId = snapshot.key as String
                                            dbref_saved.child(userId).child(post_id).removeValue()
                                        }
                                    }
                                    FirebaseDatabase.getInstance().getReference("saved_post-user").child(post_id).removeValue()
                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT). show()
                                }
                                .setNegativeButton("No"){ dialogInterface, x ->
                                    dialogInterface.cancel()
                                }
                                .show()

//                            Toast.makeText(context, "delete post", Toast.LENGTH_SHORT). show()
                        }
                    }
                    true
                }
            }
        }
        else{
            holder.moreImage.visibility = View.GONE
        }

        holder.likeImage.setOnClickListener{
            if(holder.likeImage.tag.equals("like")&&holder.dislikeImage.tag.equals("dislike")){
                FirebaseDatabase.getInstance().getReference("likes").child(post_id).child(onlineUserID).setValue(true)
            }
            else if(holder.likeImage.tag.equals("like")&&holder.dislikeImage.tag.equals("disliked")){
                FirebaseDatabase.getInstance().getReference("likes").child(post_id).child(onlineUserID).setValue(true)
                FirebaseDatabase.getInstance().getReference("dislikes").child(post_id).child(onlineUserID).removeValue()
            }
            else{
                FirebaseDatabase.getInstance().getReference("likes").child(post_id).child(onlineUserID).removeValue()
            }
        }
        holder.dislikeImage.setOnClickListener{
            if(holder.likeImage.tag.equals("like")&&holder.dislikeImage.tag.equals("dislike")){
                FirebaseDatabase.getInstance().getReference("dislikes").child(post_id).child(onlineUserID).setValue(true)
            }
            else if(holder.likeImage.tag.equals("liked")&&holder.dislikeImage.tag.equals("dislike")){
                FirebaseDatabase.getInstance().getReference("likes").child(post_id).child(onlineUserID).removeValue()
                FirebaseDatabase.getInstance().getReference("dislikes").child(post_id).child(onlineUserID).setValue(true)
            }
            else{
                FirebaseDatabase.getInstance().getReference("dislikes").child(post_id).child(onlineUserID).removeValue()
            }
        }
        holder.saveImage.setOnClickListener{
            if(holder.saveImage.tag.equals("save")){
                FirebaseDatabase.getInstance().getReference("saved").child(onlineUserID).child(post_id).setValue(true)
                FirebaseDatabase.getInstance().getReference("saved_post-user").child(post_id).child(onlineUserID).setValue(true)
            }
            else if(holder.saveImage.tag.equals("saved")){
                FirebaseDatabase.getInstance().getReference("saved").child(onlineUserID).child(post_id).removeValue()
                FirebaseDatabase.getInstance().getReference("saved_post-user").child(post_id).child(onlineUserID).removeValue()
            }
        }

        holder.commentImage.setOnClickListener{
            commentNav.invoke(post_id)
        }
        holder.commentTextView.setOnClickListener{
            commentNav.invoke(post_id)
        }

        checkLiked(post_id,holder.likeImage)
        checkDisliked(post_id,holder.dislikeImage)
        checkSaved(post_id,holder.saveImage)
        getLikeNum(post_id,holder.likesTextView)
        getDislikeNum(post_id,holder.dislikesTextView)
        getCommentNum(post_id,holder.commentTextView)
    }

    override fun getItemCount(): Int {
        return questionList.size
    }
    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val name : TextView = itemView.findViewById(R.id.askedBy)
        val question : TextView = itemView.findViewById(R.id.expandable_text)
        val questionExpandable: ExpandableTextView= itemView.findViewById(R.id.expand_text_view)
        val date : TextView = itemView.findViewById(R.id.dateTextView)
        val profileImage: CircleImageView = itemView.findViewById(R.id.Profile_image)
        val QuestionImage: ImageView = itemView.findViewById(R.id.question_image)
        val likeImage: ImageView = itemView.findViewById(R.id.like)
        val dislikeImage: ImageView = itemView.findViewById(R.id.dislike)
        val saveImage: ImageView = itemView.findViewById(R.id.save)
        val likesTextView : TextView = itemView.findViewById(R.id.likesText)
        val dislikesTextView : TextView = itemView.findViewById(R.id.dislikesText)
        val commentImage: ImageView = itemView.findViewById(R.id.comment)
        val commentTextView : TextView = itemView.findViewById(R.id.commentsText)
        val moreImage: ImageView = itemView.findViewById(R.id.more)
    }
    private fun checkLiked(postID: String,imageView: ImageView){
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val onlineUserID: String = auth.currentUser!!.uid
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("likes").child(postID)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(onlineUserID).exists()){
                    imageView.setImageResource(R.drawable.ic_liked)
                    imageView.setTag("liked")
                }
                else{
                    imageView.setImageResource(R.drawable.ic_like)
                    imageView.setTag("like")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        dbRef.addValueEventListener(postListener)
    }

    private fun checkDisliked(postID: String,imageView: ImageView){
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val onlineUserID: String = auth.currentUser!!.uid
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("dislikes").child(postID)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(onlineUserID).exists()){
                    imageView.setImageResource(R.drawable.ic_disliked)
                    imageView.setTag("disliked")
                }
                else{
                    imageView.setImageResource(R.drawable.ic_dislike)
                    imageView.setTag("dislike")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        dbRef.addValueEventListener(postListener)
    }

    private fun getLikeNum(postID: String,textView: TextView){
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("likes").child(postID)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val numLikes = dataSnapshot.childrenCount.toInt()
                if(numLikes==0){
                    textView.setText("0 likes")
                }
                else if(numLikes==1){
                    textView.setText("1 like")
                }
                else{
                    textView.setText("$numLikes likes")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        dbRef.addValueEventListener(postListener)
    }

    private fun getDislikeNum(postID: String,textView: TextView){
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("dislikes").child(postID)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val numDislikes = dataSnapshot.childrenCount.toInt()
                if(numDislikes==0){
                    textView.setText("0 dislikes")
                }
                else if(numDislikes==1){
                    textView.setText("1 dislike")
                }
                else{
                    textView.setText("$numDislikes dislikes")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        dbRef.addValueEventListener(postListener)
    }

    private fun getCommentNum(postID: String,textView: TextView){
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("comments").child(postID)
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val numComments = dataSnapshot.childrenCount.toInt()
                if(numComments==0){
                    textView.setText("0 comments")
                }
                else if(numComments==1){
                    textView.setText("1 comment")
                }
                else{
                    textView.setText("$numComments comments")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        dbRef.addValueEventListener(postListener)
    }

    private fun checkSaved(postID: String,imageView: ImageView){
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val onlineUserID: String = auth.currentUser!!.uid
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("saved").child(onlineUserID).child(postID)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    imageView.setImageResource(R.drawable.ic_saved)
                    imageView.setTag("saved")
                }
                else{
                    imageView.setImageResource(R.drawable.ic_save)
                    imageView.setTag("save")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        dbRef.addValueEventListener(postListener)
    }

}