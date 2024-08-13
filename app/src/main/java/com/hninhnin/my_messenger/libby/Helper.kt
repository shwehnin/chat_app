package com.hninhnin.my_messenger.libby

import android.util.Log
import com.hninhnin.my_messenger.models.User

class Helper {
    companion object {
        var user: User? = null

        fun debugLog(message: String) {
            Log.d("my_message", message)
        }
    }
}