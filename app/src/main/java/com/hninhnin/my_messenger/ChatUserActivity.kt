package com.hninhnin.my_messenger

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.hninhnin.my_messenger.adapters.ChatLeftAdapter
import com.hninhnin.my_messenger.adapters.ChatRightAdapter
import com.hninhnin.my_messenger.libby.Helper
import com.hninhnin.my_messenger.models.MyMessage
import com.hninhnin.my_messenger.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class ChatUserActivity : AppCompatActivity() {
    var toUser: User? = null
    private lateinit var btnSend : ImageView
    private lateinit var etChatMessage : EditText
    val adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var rvChatUser: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        toUser = intent.getParcelableExtra("toUser")
        val name = toUser!!.userName.replaceFirstChar {
            it.uppercase()
        }
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSend = findViewById(R.id.btnSend)
        etChatMessage = findViewById(R.id.etChatMessage)
        rvChatUser = findViewById(R.id.rvChatUser)

//        Toast.makeText(this, toUser?.userName, Toast.LENGTH_SHORT).show()

        // send message to each other
        btnSend.setOnClickListener {
            sendMessage()
        }

        rvChatUser.layoutManager = LinearLayoutManager(this)
        rvChatUser.adapter = adapter

        // Send message when "Enter" is pressed
        etChatMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                event?.action == android.view.KeyEvent.ACTION_DOWN && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // Send message when the send button is clicked
        checkMessageArrive()
    }

    // messages list
    private fun checkMessageArrive() {
        val fromId = Helper.user?.uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val myMessage = snapshot.getValue(MyMessage::class.java)
                if(myMessage != null) {
                    Log.d("Chatting Message", "Message: ${myMessage.text}")
                    Log.d("From Id", "From ${myMessage.fromId}")
                    Log.d("Current Id", "Current User ${FirebaseAuth.getInstance().uid}")
                    if(myMessage.fromId == FirebaseAuth.getInstance().uid) {
                        // We send to other
                        adapter.add(ChatRightAdapter(myMessage.text, Helper.user!!))
                    }else {
                        // Send from other
                        adapter.add(ChatLeftAdapter(myMessage.text, toUser!!))
                    }
                }

                rvChatUser.scrollToPosition(adapter.itemCount-1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun sendMessage() {
        val msg = etChatMessage.text.toString()
        val fromId = Helper.user?.uid
        val toId = toUser?.uid

        val fromRef = FirebaseDatabase.getInstance().getReference("/messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/messages/$toId/$fromId").push()

        val message = MyMessage(fromRef.key!!, msg, fromId!!, toId!!, System.currentTimeMillis()/1000)

        fromRef.setValue(message).addOnSuccessListener {
//            Toast.makeText(this, "Message already send", Toast.LENGTH_SHORT).show()
        }

        toRef.setValue(message)

        // latest message
        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference("/latest-message/$fromId/$toId")
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-message/$toId/$fromId")

        latestMessageFromRef.setValue(message)
        latestMessageToRef.setValue(message)

        // clear input text
        etChatMessage.text.clear()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}