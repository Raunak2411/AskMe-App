package com.example.askme.ui

import android.app.ActionBar
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.askme.*
import com.example.askme.LogIn.GALLERY_PHOTO_REQUEST
import com.example.askme.LogIn.LogInActivity
import com.example.askme.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class PostQuestionFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var question_container: TextInputLayout
    private lateinit var question_edit_text: TextInputEditText
    private lateinit var question_image: ImageView
    private lateinit var submit_button: Button
    private lateinit var delete_button: Button
//    private lateinit var add_image: Button
    private lateinit var question: String
    private lateinit var image_uri: Uri
    private lateinit var onlineUserID: String
    private lateinit var UserName: String
    private lateinit var profileURL: String
    private lateinit var date: String
    private lateinit var loader: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_question, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let{
            (it as MainActivity).supportActionBar?.title = "Ask a Question"
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        //val userref = FirebaseDatabase.getInstance().reference
        question_container = view.findViewById(R.id.question_container)
        question_edit_text = view.findViewById(R.id.question_edit_text)
        question_image = view.findViewById(R.id.question_image)
        submit_button = view.findViewById(R.id.submit_button)
        delete_button = view.findViewById(R.id.delete_button)
//        add_image = view.findViewById(R.id.add_image)
        val currUser: FirebaseUser? = auth.currentUser
        onlineUserID = currUser!!.uid
        loader = ProgressDialog(requireActivity())

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted= current.format(formatter)
        val list = formatted.split(",")
        date = list[0]
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        dbRef.child(onlineUserID).get().addOnSuccessListener {
            if(it.exists()){
                UserName = it.child("name").value as String
                profileURL = it.child("profileURL").value as String
            }
        }

        delete_button.setOnClickListener{
            returnToHome()
        }
        question_image.setOnClickListener{
            pickPhoto()
        }
        submit_button.setOnClickListener{
            question_container.error=null
            question = question_edit_text.text.toString()
            if(question.isBlank()){
                question_container.error="Please enter your Question"
            }
            else if(question_image.tag==null){
                postQuestionWithoutImage()
            }
            else if(question_image.tag!=null){
                postQuestionWithImage()
            }
        }
    }
    private fun postQuestionWithoutImage(){
        startLoader()
        dbRef = FirebaseDatabase.getInstance().getReference("questions")
        val postID = dbRef.push().key
        if (postID != null) {
            val questionData = questionClass(postID,question,onlineUserID,date, UserName, profileURL)
            dbRef.child(postID).setValue(questionData).addOnSuccessListener {
                //Toast.makeText(requireActivity(),"Question added Successfully to database", Toast.LENGTH_SHORT).show()
                FirebaseDatabase.getInstance().getReference("userPosts").child(onlineUserID).child(postID).setValue(true)
                loader.dismiss()
                returnToHome()
            }.addOnFailureListener{
                loader.dismiss()
                Toast.makeText(requireActivity(),"Failed to upload question:", Toast.LENGTH_SHORT).show()
            }


        }
    }
    private fun postQuestionWithImage(){
        startLoader()
        dbRef = FirebaseDatabase.getInstance().getReference("questions")
        val postID = dbRef.push().key
        if (postID != null) {
            var questionUrl: String? = null
            storageRef = FirebaseStorage.getInstance().reference
            val filePath = storageRef.child("questions").child("${postID}_question.jpg")
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,image_uri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG , 30, baos)
            val data = baos.toByteArray()
            val uploadTask = filePath.putBytes(data).addOnSuccessListener {
                //Toast.makeText(requireActivity(), "image added", Toast.LENGTH_SHORT). show()
                filePath.downloadUrl.addOnSuccessListener {
                    questionUrl = it.toString()

                    val questionData = questionClass(postID,question,onlineUserID,date, UserName, profileURL, questionUrl)
                    dbRef.child(postID).setValue(questionData).addOnSuccessListener {
                        //Toast.makeText(requireActivity(),"Question added Successfully to database", Toast.LENGTH_SHORT).show()
                        loader.dismiss()
                        FirebaseDatabase.getInstance().getReference("userPosts").child(onlineUserID).child(postID).setValue(true)
                        returnToHome()
                    }.addOnFailureListener{
                        loader.dismiss()
                        Toast.makeText(requireActivity(),"Failed to upload question:", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener{
                loader.dismiss()
                Toast.makeText(requireActivity(), "failed", Toast.LENGTH_SHORT). show()
            }

        }
    }
    private fun pickPhoto(){
        val pickPhotoIntent = Intent(Intent.ACTION_PICK)
        pickPhotoIntent.type="image/*"
        startActivityForResult(pickPhotoIntent, GALLERY_PHOTO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==GALLERY_PHOTO_REQUEST&&resultCode==Activity.RESULT_OK&&data!=null){
            image_uri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,image_uri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            question_image.setImageDrawable(bitmapDrawable)
            question_image.tag = image_uri.toString()
        }
    }
    private fun startLoader(){
        loader.setMessage("Posting your Question")
        loader.setCanceledOnTouchOutside(false)
        loader.show()
    }

    private fun returnToHome(){
        //return to home
        activity?.let{
            (it as MainActivity).onBackPressed()
        }
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