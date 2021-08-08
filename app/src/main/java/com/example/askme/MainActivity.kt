package com.example.askme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.example.askme.LogIn.LogInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var dbRef: DatabaseReference
    private lateinit var onlineUserID: String
    private lateinit var fab: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        val currUser: FirebaseUser? = auth.currentUser
        onlineUserID = currUser!!.uid
        fab = findViewById(R.id.fab)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_myPost, R.id.nav_savedPost, R.id.nav_post_question), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        fab.setOnClickListener { view ->
            navController.navigate(R.id.nav_post_question)
        }
        navView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawers()
            when(it.itemId){
                R.id.nav_post_question -> {
                    navController.navigate(R.id.nav_post_question)
                }
                R.id.nav_home -> {
                    //navController.popBackStack()
                    navController.popBackStack(R.id.nav_home,false)
                    //navController.navigate(R.id.nav_home)


                }
                R.id.nav_savedPost -> {
                    navController.navigate(R.id.nav_savedPost)
                    //navController.popBackStack(R.id.nav_savedPost,false)
                    //else navController.navigate(R.id.nav_savedPost)
                    //navController.navigate(R.id.nav_savedPost)
                    //if(navController.currentBackStackEntry==R.id.nav_savedPost)
                }
                R.id.nav_myPost -> {
                    navController.navigate(R.id.nav_myPost)
                }
                R.id.nav_signout -> {
                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    auth.addAuthStateListener{
                        if(auth.currentUser==null){
                            val intent = Intent(this, LogInActivity::class.java).apply {
                            }
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
            true

        }
        val headerlayout: View = navView.getHeaderView(0)
        val UserName: TextView = headerlayout.findViewById(R.id.User_name)
        val UserEmail: TextView = headerlayout.findViewById(R.id.User_email)
        dbRef = FirebaseDatabase.getInstance().getReference("users")
        val ProfilePic: ImageView = headerlayout.findViewById(R.id.profilePic)

//        dbRef.child(onlineUserID).get().addOnSuccessListener {
//            if(it.exists()){
//                UserName.text = it.child("name").value as String
//                Glide.with(this).load(it.child("profilrURL").toString()).into(ProfilePic)
//            }
//        }
//        UserEmail.text = auth.currentUser?.email

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                UserName.text = dataSnapshot.child("name")!!.value as String
                UserEmail.text = dataSnapshot.child("email").value as String
                Glide.with(this@MainActivity).load(dataSnapshot.child("profileURL").value as String).into(ProfilePic)
                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        dbRef.child(onlineUserID).addValueEventListener(postListener)
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    fun HideFlotingButton(){
        fab.isVisible = false
    }
    fun ShowFlotingButton(){
        fab.isVisible = true
    }

}