package com.example.askme.LogIn

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.example.askme.R
import com.example.askme.user
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*

const val GALLERY_PHOTO_REQUEST = 2


class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var loader: ProgressDialog
    private lateinit var user_email_container: TextInputLayout
    private lateinit var user_pass_container: TextInputLayout
    private lateinit var confirm_pass_container: TextInputLayout
    private lateinit var user_fullname_container: TextInputLayout
    private lateinit var user_fullname: TextInputEditText
    private lateinit var user_email: TextInputEditText
    private lateinit var user_pass: TextInputEditText
    private lateinit var confirm_pass: TextInputEditText
    private lateinit var ProfileImage: CircleImageView
    private lateinit var LogInButtonNav: TextView
    private lateinit var signup_button: Button
    private lateinit var UploadImageButton: Button
    private lateinit var image_uri: Uri
    private var onlineUserID = ""
    private var email = ""
    private var name = ""
    private var username = ""
    private var pass = ""
    private var pass2 = ""
    private var profileUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
//        if(auth.currentUser!=null){
//            activity?.let{
//                (it as LogInActivity).navigate()
//            }
//        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogInButtonNav = view.findViewById(R.id.LogInButtonNav)
        signup_button = view.findViewById(R.id.SignUpButton)
        //UploadImageButton = view.findViewById(R.id.UploadImageButton)
        user_email_container = view.findViewById(R.id.user_email_container)
        user_pass_container = view.findViewById(R.id.user_password_container)
        confirm_pass_container = view.findViewById(R.id.confirm_password_container)
        user_fullname_container = view.findViewById(R.id.user_fullname_container)
        user_fullname = view.findViewById(R.id.fullname)
        user_email = view.findViewById(R.id.user_email)
        user_pass = view.findViewById(R.id.user_password)
        confirm_pass = view.findViewById(R.id.confirm_password)
        ProfileImage = view.findViewById(R.id.ProfileImage)
        loader = ProgressDialog(requireActivity())
        LogInButtonNav.setOnClickListener{
            findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
        }
        ProfileImage.setOnClickListener{
            pickPhoto()
        }
        signup_button.setOnClickListener{
            user_email_container.error = null
            user_pass_container.error = null
            confirm_pass_container.error = null
            user_fullname_container.error = null
            email = user_email.text.toString()
            pass = user_pass.text.toString()
            pass2 = confirm_pass.text.toString()
            name = user_fullname.text.toString()
            if(validateInput(name, email, pass, pass2)){
                startLoader()
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful){
                        onlineUserID = auth.currentUser!!.uid
//                        Toast.makeText(requireActivity(),"Authentication Done ${auth.currentUser!!.uid}", Toast.LENGTH_SHORT).show()
                        storeImage()
                    }
                    else{
                        loader.dismiss()
                        Toast.makeText(requireActivity(),"Sign Up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun validateInput(name: String, email: String, pass: String, pass2: String, ): Boolean{
        var valid = true
        if(name.isBlank()){
            user_fullname_container.error = "Please enter your name"
            valid = false
        }
        if(email.isBlank()){
            user_email_container.error = "Please enter your email address"
            valid = false
        }
        if(pass.isBlank()){
            user_pass_container.error ="Please enter your password"
            valid = false
        }
        else if(pass.length < 8) {
            user_pass_container.error = "Password should be 8 characters or more"
            valid = false
        }
        if(pass2.isBlank()){
            confirm_pass_container.error ="Please confirm your password"
            valid = false
        }
        else if(pass.length >= 8&&pass!=pass2){
            confirm_pass_container.error ="Passwords didn't match. Try again."
            valid = false
        }

        if(ProfileImage.tag==null){
            Toast.makeText(requireActivity(), "Please upload your profile picture", Toast.LENGTH_SHORT). show()
            valid = false
        }
        return valid
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
            ProfileImage.setImageDrawable(bitmapDrawable)
            ProfileImage.tag = image_uri.toString()
        }
    }
    private fun storeImage(){
        storageRef = FirebaseStorage.getInstance().reference
        val filePath = storageRef.child("profile images").child("${onlineUserID}_profile.jpg")
        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,image_uri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG , 20, baos)
        val data = baos.toByteArray()
        val uploadTask = filePath.putBytes(data).addOnSuccessListener {
            //Toast.makeText(requireActivity(), "Profile image added successfully", Toast.LENGTH_SHORT). show()
            filePath.downloadUrl.addOnSuccessListener {
                profileUrl = it.toString()
                //Log.d("profilrURL","$profileUrl")
                saveUserToDatabase()
            }
        }.addOnFailureListener{
            loader.dismiss()
            Toast.makeText(requireActivity(), "Sign Up failed: ${it.message}", Toast.LENGTH_SHORT). show()
        }
    }
    private fun saveUserToDatabase(){
        val userref = FirebaseDatabase.getInstance().getReference("users")
        val User = user(name,email,profileUrl)
        userref.child(onlineUserID).setValue(User).addOnSuccessListener {
            //Toast.makeText(requireActivity(),"Details added Successfully", Toast.LENGTH_SHORT).show()
            loader.dismiss()
            activity?.let{
                (it as LogInActivity).navigate()
            }
        }.addOnFailureListener{
            loader.dismiss()
            Toast.makeText(requireActivity(), "Sign Up failed: ${it.message}", Toast.LENGTH_SHORT). show()
        }
    }
    private fun startLoader(){
        loader.setMessage("Signing Up")
        loader.setCanceledOnTouchOutside(false)
        loader.show()
    }
}