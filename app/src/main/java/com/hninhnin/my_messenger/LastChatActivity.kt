package com.hninhnin.my_messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hninhnin.my_messenger.adapters.LastMessageAdapter
import com.hninhnin.my_messenger.libby.Helper.Companion.user
import com.hninhnin.my_messenger.models.MyMessage
import com.hninhnin.my_messenger.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class LastChatActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    val latestMessageMap = HashMap<String, MyMessage>()

    private lateinit var rvLastChat : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.title = "Latest Chat Messages"
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_last_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvLastChat = findViewById(R.id.rvLastChat)
        rvLastChat.layoutManager = LinearLayoutManager(this)
        rvLastChat.adapter = adapter

        Log.d("LastChatActivity", "Adapter item count: ${adapter.itemCount}")

        // go to chat user page when click last chat message
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatUserActivity::class.java)
            val row = item as LastMessageAdapter
            intent.putExtra("toUser", row.friendUser)
            startActivity(intent)
        }

        // check user auth
        checkUserAuth()
        fetchUser()
        listenLastMessage()
    }

    // refresh recycler view
    private  fun  refreshRecyclerView() {
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LastMessageAdapter(it))
            Log.d("LastMessageAdapter", "Message: ${it.text}")
        }
    }

    // listen for latest messages
    private fun listenLastMessage() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("latest-message/$fromId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val myMessage = snapshot.getValue(MyMessage::class.java)
                if(myMessage != null) {
                    latestMessageMap[snapshot.key!!] = myMessage
                    Log.d("LastChatActivity Log", "Message added: ${myMessage.text}")
                }else {
                    Log.d("LastChatActivity Null", "MyMessage is null for key: ${snapshot.key}")
                }
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val myMessage = snapshot.getValue(MyMessage::class.java)
                if (myMessage != null) {
                    latestMessageMap[snapshot.key!!] = myMessage
                    Log.d("LastChatActivity", "Message changed: ${myMessage.text}")
                } else {
                    Log.d("LastChatActivity", "MyMessage is null for key: ${snapshot.key}")
                }
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    // check user auth
    private fun checkUserAuth() {
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    // fetch user data
    private fun fetchUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    // create menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // click menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.startMessage -> {
                val intent = Intent(this, NewChatMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}