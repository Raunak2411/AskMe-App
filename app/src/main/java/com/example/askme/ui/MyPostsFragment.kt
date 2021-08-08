package com.example.askme.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.askme.MainActivity
import com.example.askme.R
import com.example.askme.questionClass
import com.example.askme.ui.home.MyAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class MyPostsFragment : Fragment() {

    private lateinit var userRecyclerview : RecyclerView
    private lateinit var ProgressBar: ProgressBar
    private lateinit var questionList : ArrayList<questionClass>
    private lateinit var dbref : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var onlineUserID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_posts, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let{
            (it as MainActivity).supportActionBar?.title = "My Posts"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currUser: FirebaseUser? = auth.currentUser
        onlineUserID = currUser!!.uid
        ProgressBar = view.findViewById(R.id.progressBar)
        userRecyclerview = view.findViewById(R.id.post_list)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        userRecyclerview.layoutManager = linearLayoutManager
        userRecyclerview.setHasFixedSize(true)
        questionList = arrayListOf<questionClass>()
        getMyPost()
    }

    private fun getMyPost() {
        dbref = FirebaseDatabase.getInstance().getReference("userPosts").child(onlineUserID)
        val dbref2 = FirebaseDatabase.getInstance().getReference("questions")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    questionList.clear()
                    val post_count = snapshot.childrenCount.toInt()
                    var count = 0
                    for (userSnapshot in snapshot.children){
                        val MyPostID = userSnapshot.key as String
                        dbref2.child(MyPostID).get().addOnSuccessListener{ data_snapshot ->
                            val MyQues = data_snapshot.getValue(questionClass::class.java)
                            questionList.add(MyQues!!)
                            count++
                            if(count==post_count){
                                userRecyclerview.adapter = MyAdapter(questionList){
                                    findNavController().navigate(MyPostsFragmentDirections.actionMyPostsFragmentToNavComments(it))
                                }
                                ProgressBar.isVisible = false
                            }
                            //Log.d("rrrttt","ques list size ${questionList.size}")
                        }.addOnFailureListener{}

                    }
                    //Log.d("rrrttt","post list size ${post_count}")
                }
                else{
                    questionList.clear()
                    userRecyclerview.adapter = MyAdapter(questionList){
                        findNavController().navigate(MyPostsFragmentDirections.actionMyPostsFragmentToNavComments(it))
                    }
                    ProgressBar.isVisible = false
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


}