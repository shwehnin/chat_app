package com.hninhnin.my_messenger.models

class MyMessage(val id: String, val text : String, val fromId: String, val toId: String, val timestamp: Long) {
    constructor() : this("", "", "", "", -1)
}