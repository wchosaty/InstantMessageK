package com.net.instantmessagek.pojo

import java.io.Serializable
import java.util.*

data class User(var userId: Long, var name:String, var passWord: String,var city: String): Serializable {
        var friendCount: Int = 0
        var status: String = ""
        var time: Date ?= null
        var nickname: String = ""

}