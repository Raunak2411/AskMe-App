package com.example.askme.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.askme.MainActivity
import com.example.askme.R
import com.example.askme.questionClass
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var userRecyclerview : RecyclerView
    private lateinit var ProgressBar: ProgressBar
    private lateinit var questionList : ArrayList<questionClass>
    private lateinit var dbref : DatabaseReference
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let{
            (it as MainActivity).supportActionBar?.title = "Ask Me"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressBar = view.findViewById(R.id.progressBar)
        userRecyclerview = view.findViewById(R.id.post_list)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        userRecyclerview.layoutManager = linearLayoutManager
        userRecyclerview.setHasFixedSize(true)
        questionList = arrayListOf<questionClass>()
        getUserData()

    }

    private fun getUserData() {
        dbref = FirebaseDatabase.getInstance().getReference("questions")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    questionList.clear()
                    for (userSnapshot in snapshot.children){
                        val questionPost = userSnapshot.getValue(questionClass::class.java)
                        questionList.add(questionPost!!)
                    }
                    userRecyclerview.adapter = MyAdapter(questionList){
                        findNavController().navigate(HomeFragmentDirections.actionNavHomeToNavComments(it))
                    }
                    ProgressBar.isVisible = false
                }
                else{
                    questionList.clear()
                    userRecyclerview.adapter = MyAdapter(questionList){
                        findNavController().navigate(HomeFragmentDirections.actionNavHomeToNavComments(it))
                    }
                    ProgressBar.isVisible = false
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}