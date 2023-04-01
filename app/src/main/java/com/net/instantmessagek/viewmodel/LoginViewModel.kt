package com.net.instantmessagek.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel() {
    val userName: MutableLiveData<String> by lazy { MutableLiveData() }
    val passWord: MutableLiveData<String> by lazy { MutableLiveData() }
    val city: MutableLiveData<String> by lazy { MutableLiveData() }
    val nickName: MutableLiveData<String> by lazy { MutableLiveData() }

}