package com.hninhnin.my_messenger.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(val uid: String, val userName: String, val email: String, val profileImage: String) : Parcelable{
    constructor(): this("", "", "", "")
}