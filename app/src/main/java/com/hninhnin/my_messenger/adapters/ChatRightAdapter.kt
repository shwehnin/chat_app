package com.hninhnin.my_messenger.adapters

import android.widget.TextView
import com.hninhnin.my_messenger.R
import com.hninhnin.my_messenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class ChatRightAdapter(val message: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.txtMsgRight).text = message
        Picasso.get().load(user.profileImage).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.imgMsgRight))
    }

    override fun getLayout(): Int {
        return R.layout.message_right_row
    }
}