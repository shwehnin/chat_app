package com.hninhnin.my_messenger

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.hninhnin.my_messenger.libby.Helper
import com.hninhnin.my_messenger.models.User
import com.hninhnin.my_messenger.adapters.UserAdapter

class NewChatMessageActivity : AppCompatActivity() {
    private lateinit var newMessageRecycler : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        // set title in action bar
        supportActionBar?.title = "New Message"
        // show back btn in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_chat_message)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        newMessageRecycler = findViewById(R.id.newMessageRecycler)

        // get all users
        fetchAllUser()
    }

    // get all users
    private fun fetchAllUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if(user != null && user.userName != Helper.user?.userName){
                        adapter.add(UserAdapter(user))
                    }
                }

                // check to the another user
                adapter.setOnItemClickListener { item, view ->
                    val toUser = item as UserAdapter
                    val intent = Intent(this@NewChatMessageActivity, ChatUserActivity::class.java)
                    intent.putExtra("toUser", toUser.user)
                    startActivity(intent)
                }

                newMessageRecycler.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // go to previous page from back btn action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}