package com.hninhnin.my_messenger

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.hninhnin.my_messenger.libby.Helper
import com.hninhnin.my_messenger.models.User
import java.io.InputStream
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    var uri: Uri? = null
    private lateinit var txtMember : TextView
    private lateinit var registerAnimationView : LottieAnimationView
    private lateinit var imgProfile: ImageView
    private lateinit var txtRegisterProfile: TextView
    private lateinit var registerBtn: Button
    private lateinit var txtRegisterUserName: EditText
    private lateinit var txtRegisterEmail: EditText
    private lateinit var txtRegisterPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        // hide action bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtMember = findViewById(R.id.txtMember)
        // go to login page
        txtMember.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        imgProfile = findViewById(R.id.imgProfile)
        // click profile image
        imgProfile.setOnClickListener {
            requestPermit()
        }

//        registerAnimationView = findViewById(R.id.registerAnimationView)
        // click register animation
//        registerAnimationView.setOnClickListener {
//            Toast.makeText(this, "click me", Toast.LENGTH_SHORT).show()
//        }

        txtRegisterUserName = findViewById(R.id.txtRegisterUserName)
        txtRegisterEmail = findViewById(R.id.txtRegisterEmail)
        txtRegisterPassword = findViewById(R.id.txtRegisterPassword)

        registerBtn = findViewById(R.id.registerBtn)
        // click register btn
        registerBtn.setOnClickListener {
            val userName: String = txtRegisterUserName.text.trim().toString()
            val email: String = txtRegisterEmail.text.trim().toString()
            val password: String = txtRegisterPassword.text.trim().toString()

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startRegister(email, password)
        }
    }

    // request permission
    private fun requestPermit() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            101 -> {
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }else {
                    startImagePick()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // pick image
    private fun startImagePick() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Choose Image"), 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode ==100 && resultCode == Activity.RESULT_OK && data!=null) {
            uri = data.data
            val inputStr : InputStream = contentResolver.openInputStream(uri!!)!!
            val bitMap  = BitmapFactory.decodeStream(inputStr)
            imgProfile.setImageBitmap(bitMap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // create register
    private fun startRegister(email: String, password: String) {
        if(uri == null) return
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                Helper.debugLog("Create user successfully with user id of ${it.result.user?.uid}")
                userProfileUpload()
            }
            .addOnFailureListener{
                Helper.debugLog("Fail to create user : ${it.message}")
            }
    }

    // upload image to firebase storage
    private fun userProfileUpload() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference(("/images/$filename"))
        ref.putFile(uri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                Helper.debugLog("Image download uri is $it")
                storeUserDataDB("$it")
            }
        }.addOnFailureListener {
            Helper.debugLog("Image upload fail ${it.message}")
        }
    }

    // store user data to firebase realtime database
    private fun storeUserDataDB(profileImgUri : String) {
        val uid = FirebaseAuth.getInstance().uid.toString()
        val userName = txtRegisterUserName.text.trim().toString()
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val user = User(uid, userName, email, profileImgUri)
        // store users dir in uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(user).addOnSuccessListener {
            // go to last chat page not return to register
            val intent = Intent(this, LastChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }.addOnFailureListener{
            Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}