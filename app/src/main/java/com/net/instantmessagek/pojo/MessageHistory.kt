package com.net.instantmessagek.pojo

import java.io.Serializable

data class MessageHistory(var messageId:Long, var roomId:Long,var roomName: String, var fromUser:String): Serializable  {
    var action = ""
    var type = ""
    var comment = ""
    var b: ByteArray? = null
}