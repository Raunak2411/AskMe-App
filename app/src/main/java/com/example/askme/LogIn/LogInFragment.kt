package com.example.askme.LogIn

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.askme.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth


class LogInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loader: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            activity?.let{
                (it as LogInActivity).navigate()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = ProgressDialog(requireActivity())
        val SignUnButtonNav = view.findViewById<TextView>(R.id.SignUnButtonNav)
        SignUnButtonNav.setOnClickListener{
            findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
        }
        val login_button = view.findViewById<Button>(R.id.LoginButton)
        login_button.setOnClickListener{
            val user_email_container = view.findViewById<TextInputLayout>(R.id.user_email_container)
            val user_pass_container = view.findViewById<TextInputLayout>(R.id.user_password_container)
            val user_email = view.findViewById<TextInputEditText>(R.id.user_email)
            val user_pass = view.findViewById<TextInputEditText>(R.id.user_password)
            user_email_container.error = null
            user_pass_container.error = null
            val email = user_email.text.toString()
            val pass = user_pass.text.toString()
            if(validateInput(email,pass)){
                startLoader()
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        loader.dismiss()
                        activity?.let{
                            (it as LogInActivity).navigate()
                        }
                    }
                    else {
                        loader.dismiss()
                        Toast.makeText(requireActivity(),
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun validateInput(email: String, pass: String): Boolean{
        var valid = true
        val user_pass_container = view?.findViewById<TextInputLayout>(R.id.user_password_container)
        val user_email_container = view?.findViewById<TextInputLayout>(R.id.user_email_container)
        if(email.isBlank()){
            user_email_container?.error = "Please enter an email address"
            valid = false
        }
        if(pass.isBlank()){
            user_pass_container?.error="Please enter password"
            valid = false
        }
        else if(pass.length < 8) {
            user_pass_container?.error = "Password should be 8 characters or more"
            valid = false
        }
        return valid
    }
    private fun startLoader(){
        loader.setMessage("Loging In")
        loader.setCanceledOnTouchOutside(false)
        loader.show()
    }
}