package com.hninhnin.my_messenger.adapters

import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hninhnin.my_messenger.R
import com.hninhnin.my_messenger.models.MyMessage
import com.hninhnin.my_messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class LastMessageAdapter(val myMessage: MyMessage) : Item<GroupieViewHolder>() {
    var friendUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txtLastMsg).text = myMessage.text
        val friendId : String

        if(myMessage.fromId == FirebaseAuth.getInstance().uid) {
            friendId = myMessage.toId
        }else {
            friendId = myMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$friendId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendUser = snapshot.getValue(User::class.java)
                Log.d("LastMessageAdapter Data Change", "Friend User: ${friendUser?.userName}, Image: ${friendUser?.profileImage}")
                viewHolder.itemView.findViewById<TextView>(R.id.txtLastUserName).text = friendUser?.userName
                Picasso.get().load(friendUser?.profileImage).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.imgLastMsg))
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    override fun getLayout(): Int {
        return R.layout.last_message_row
    }
}