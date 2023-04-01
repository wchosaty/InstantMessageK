package com.net.instantmessagek.pojo

import android.os.Message
import java.io.Serializable

data class RoomList(var roomId:Long, var roomName:String, var lastMessage: String): Serializable {
    var lastMessageTime = 0L
    var memberCount = 0
    var member1 = ""
    var member2 = ""

}