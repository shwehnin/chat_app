package com.hninhnin.my_messenger.adapters

import android.widget.ImageView
import android.widget.TextView
import com.hninhnin.my_messenger.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.hninhnin.my_messenger.models.User

class UserAdapter(val user: User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txtUserName).text = user.userName
        Picasso.get().load(user.profileImage).into(viewHolder.itemView.findViewById<ImageView>(R.id.imgUserProfileImg))
    }

    override fun getLayout(): Int {
        return R.layout.new_message_user_row
    }

}