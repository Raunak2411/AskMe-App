package com.example.askme.LogIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.askme.MainActivity
import com.example.askme.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LogInActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var firebaseAuthListener: FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
//        auth = FirebaseAuth.getInstance()
//        firebaseAuthListener = FirebaseAuth.AuthStateListener{
//            val currUser: FirebaseUser? = auth.currentUser
//            if(currUser!=null){
//                val intent = Intent(this, MainActivity::class.java).apply {
//                }
//                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//                finish()
//            }
//        }

    }
    fun navigate(){
        val intent = Intent(this, MainActivity::class.java).apply {
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

//    override fun onStart() {
//        super.onStart()
//        auth.addAuthStateListener(firebaseAuthListener)
//    }
//
//    override fun onStop() {
//        super.onStop()
//        auth.removeAuthStateListener(firebaseAuthListener)
//    }
}