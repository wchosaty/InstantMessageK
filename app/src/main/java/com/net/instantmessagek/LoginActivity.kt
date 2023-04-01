package com.net.instantmessagek

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.ActivityLoginBinding
import com.net.instantmessagek.pojo.User
import com.net.instantmessagek.viewmodel.LoginViewModel
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {
    private val TAG = "TAG ${javaClass.simpleName}"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferences = createEncryptedPreferences()
        // 未加密Preferences
//        preferences = createPreferences()
        val viewModel: LoginViewModel by viewModels()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.loginViewModel = viewModel
        binding.lifecycleOwner = this
//        setContentView(binding.root)
        binding.apply {

            btLoginIn.setOnClickListener {
                runBlocking {
                    register()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        var backString = ""
        val userName = preferences.getString(Com.NAME, "")
        val userid = preferences.getLong(Com.ID, 0L)
        var j = JsonObject()
        j.addProperty(Com.ACTION, Com.QUERY)
        j.addProperty(Com.TYPE, Com.ID)
        j.addProperty(Com.DATA, userid.toString())
        runBlocking {
            backString = Connection().httpPost(Com.servletURL + "UserServlet", j.toString())
            LogDmy().d(TAG, "onStart", backString)
            if (!backString.isNullOrEmpty() && backString != "null") startActivity(
                Intent(
                    this@LoginActivity,
                    MainActivity::class.java
                )
            )
        }
    }

    private suspend fun saveUsers(user: User): String {
        val j = JsonObject()
        var backString = ""
        val gsonDate = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        j.addProperty(Com.ACTION, Com.INSERT)
        j.addProperty(Com.TYPE, Com.USER)
        j.addProperty(Com.DATA, gsonDate.toJson(user))
        LogDmy().d(TAG, "saveUsers", j.toString())
        backString = Connection().httpPost(Com.servletURL + "UserServlet", j.toString())
        LogDmy().d(TAG, "back", backString)
        LogDmy().d(TAG, "back return", backString)
        return backString
    }

    private suspend fun register() = runBlocking {
        binding.apply {
            var back = "234"
            val userName = loginViewModel?.userName?.value?.trim()
            val passWord = loginViewModel?.passWord?.value?.trim()
            val city = loginViewModel?.city?.value?.trim()

            if (!userName.isNullOrEmpty() && !passWord.isNullOrEmpty() && !city.isNullOrEmpty()) {
                back = saveUsers(User(0L, userName, passWord, city))

                LogDmy().d(TAG, "register()back", back)
                var jBack = Gson().fromJson(back, JsonObject::class.java)
                var user_id = jBack.get(Com.ID).asString.trim().toLong()
                LogDmy().d(TAG, "user_id", user_id.toString())
                preferences.edit().putString(Com.NAME, userName)
                    .putLong(Com.ID, user_id)
                    .putBoolean(Com.STATUS, true).apply()
                Toast.makeText(this@LoginActivity, "register", Toast.LENGTH_LONG).show()
                loginViewModel?.userName?.value = ""
                loginViewModel?.passWord?.value = ""
                loginViewModel?.city?.value = ""
                loginViewModel?.nickName?.value = ""

                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            }
        }
    }

    private fun createPreferences(): SharedPreferences {
        return this.getPreferences(MODE_PRIVATE)
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKey.Builder(this@LoginActivity)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            this@LoginActivity, "login", masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


}