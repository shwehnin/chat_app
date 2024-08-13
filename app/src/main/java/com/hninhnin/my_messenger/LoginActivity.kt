package com.hninhnin.my_messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.hninhnin.my_messenger.libby.Helper

class LoginActivity : AppCompatActivity() {
    private lateinit var txtNotMember : TextView
    private lateinit var txtLoginEmail: EditText
    private lateinit var txtLoginPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        //hide action bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtNotMember = findViewById(R.id.txtNotMember)
        txtLoginEmail = findViewById(R.id.txtLoginEmail)
        txtLoginPassword = findViewById(R.id.txtLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // click login btn
        btnLogin.setOnClickListener {
            val email : String = txtLoginEmail.text.trim().toString()
            val password : String = txtLoginPassword.text.trim().toString()
            
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if(password.length< 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startLogin(email, password)
        }

        // go to previous page
        txtNotMember.setOnClickListener {
            finish()
        }
    }

    // create login
    private fun startLogin(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                // not success
                if(!it.isSuccessful) return@addOnCompleteListener
                // go to last chat page not return to login page
                val intent  = Intent(this, LastChatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }.addOnFailureListener {
                Helper.debugLog("Login Fail ${it.message}")
            }
        Helper.debugLog("Email is $email and Password is $password")
    }
}