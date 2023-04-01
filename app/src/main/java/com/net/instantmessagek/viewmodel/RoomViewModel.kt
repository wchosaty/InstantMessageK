package com.net.instantmessagek.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.pojo.RoomMember

class RoomViewModel: ViewModel() {
    private val TAG = "TAG ${javaClass.simpleName}"
    val roomMembers: MutableLiveData<List<RoomMember>> by lazy { MutableLiveData() }
}