package com.example.askme.ui

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.askme.LogIn.LogInActivity
import com.example.askme.MainActivity
import com.example.askme.R
import com.example.askme.commentClass
import com.example.askme.questionClass
import com.example.askme.ui.home.MyAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CommentFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var onlineUserID: String
    private lateinit var UserName: String
    private lateinit var profileURL: String
    private lateinit var commentRecyclerview : RecyclerView
    private lateinit var ProgressBar: ProgressBar
    private lateinit var commentEditText: EditText
    private lateinit var postButton: Button
    private lateinit var profileImage: ImageView
    private lateinit var commentList : ArrayList<commentClass>
    private lateinit var dbref : DatabaseReference
    private lateinit var loader: ProgressDialog
    private lateinit var postId: String
    private lateinit var commentText: String
    private lateinit var date: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let{
            (it as MainActivity).supportActionBar?.title = "Comments"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let{
            postId = CommentFragmentArgs.fromBundle(it).postId
        }

        auth = FirebaseAuth.getInstance()
        val currUser: FirebaseUser? = auth.currentUser
        onlineUserID = currUser!!.uid
        dbref = FirebaseDatabase.getInstance().getReference("users")
        dbref.child(onlineUserID).get().addOnSuccessListener {
            if(it.exists()){
                UserName = it.child("name").value as String
                profileURL = it.child("profileURL").value as String
            }
        }
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted= current.format(formatter)
        val list = formatted.split(",")
        date = list[0]

        ProgressBar = view.findViewById(R.id.progressBar2)
        loader = ProgressDialog(requireActivity())

        commentRecyclerview = view.findViewById(R.id.comments_list)
        postButton = view.findViewById(R.id.Post_comment_button)
        commentEditText = view.findViewById(R.id.new_comment)
        profileImage = view.findViewById(R.id.Comment_profile_image)
        val linearLayoutManager = LinearLayoutManager(requireContext())
//        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true
        commentRecyclerview.layoutManager = linearLayoutManager
        commentRecyclerview.setHasFixedSize(true)
        commentList = arrayListOf<commentClass>()
        setProfileImage()
        getCommentData()


        postButton.setOnClickListener{
            commentText = commentEditText.text.toString()
            if(TextUtils.isEmpty(commentText)){
                commentEditText.setError("Please enter your comment")
            }
            else{
                postComment()
            }
        }

    }
    private fun getCommentData(){
        dbref = FirebaseDatabase.getInstance().getReference("comments").child(postId)
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    commentList.clear()
                    for (userSnapshot in snapshot.children){
                        val comments = userSnapshot.getValue(commentClass::class.java)
                        commentList.add(comments!!)
                    }
                    commentRecyclerview.adapter = CommentAdapter(commentList)
                    ProgressBar.isVisible = false
                }
                else{
                    commentList.clear()
                    commentRecyclerview.adapter = CommentAdapter(commentList)
                    ProgressBar.isVisible = false
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun postComment(){
        startLoader()
        dbref = FirebaseDatabase.getInstance().getReference("comments").child(postId)
        val commentId = dbref.push().key
        if (commentId != null) {
            val commentData = commentClass(commentId, postId, commentText,date, UserName, profileURL)
            dbref.child(commentId).setValue(commentData).addOnSuccessListener {
                loader.dismiss()
                commentEditText.text.clear()
                //Toast.makeText(requireActivity(),"Comment added Successfully to database", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                loader.dismiss()
                Toast.makeText(requireActivity(),"Failed to upload your comment:", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setProfileImage(){
        dbref = FirebaseDatabase.getInstance().getReference("users")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Glide.with(requireActivity()).load(dataSnapshot.child("profileURL").value as String).into(profileImage)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        dbref.child(onlineUserID).addValueEventListener(postListener)
    }
    private fun startLoader(){
        loader.setMessage("Posting your comment")
        loader.setCanceledOnTouchOutside(false)
        loader.show()
    }

    override fun onStart() {
        super.onStart()
        activity?.let{
            (it as MainActivity).HideFlotingButton()
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.let{
            (it as MainActivity).ShowFlotingButton()
        }
    }
}