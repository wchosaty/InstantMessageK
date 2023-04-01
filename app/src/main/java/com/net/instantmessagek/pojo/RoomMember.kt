package com.net.instantmessagek.pojo

import java.io.Serializable
import java.util.*

data class RoomMember(var roomId:Long, var roomName:String, var memberName: String): Serializable {
    var date: Date?= null
    var otherMember = ""
}