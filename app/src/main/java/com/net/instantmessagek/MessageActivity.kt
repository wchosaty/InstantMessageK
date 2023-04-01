package com.net.instantmessagek

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.ActivityMessageBinding
import com.net.instantmessagek.pojo.MessageHistory
import com.net.instantmessagek.pojo.OrgClient
import com.net.instantmessagek.viewmodel.MessageViewModel
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.net.URI
import java.nio.ByteBuffer
import kotlin.math.max



class MessageActivity : AppCompatActivity() {
    private val TAG = "TAG ${javaClass.simpleName}"
    private lateinit var binding: ActivityMessageBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var userName: String
    private lateinit var thisRoomName: String
    private lateinit var thisRoomId: String
    private lateinit var client: OrgClient
    lateinit var adapter: MessageAdapter
    private var lastMessID : Long = 1L
    private lateinit var gsonDate : Gson
    private var messageList = mutableListOf<MessageHistory>()
    private var imageFlag  = false
    private lateinit var image: ByteArray
    private var newSize: Int = 0
    private val url: String = "ws://10.0.2.2:8899"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        gsonDate = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        newSize = resources.displayMetrics.widthPixels / 2
        preferences = createEncryptedPreferences()
        val name = preferences.getString(Com.NAME, "")
        if (!name.isNullOrEmpty()) {
            LogDmy().d(TAG, "userName", name)
            userName = name
        }

        val viewModel: MessageViewModel by viewModels()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message)
        binding.messViewModel = viewModel
        binding.lifecycleOwner
        initial()
        getLocalData(thisRoomName)
        showMessageList()
        handleButton()
        runBlocking { launch {
                withContext(Dispatchers.IO) {
                    getNetData(lastMessID)
                    CoroutineScope(Dispatchers.Main).launch {
                        var ma = adapter as MessageAdapter
                        if (ma != null) {
                            ma.setList(messageList)
                            if (ma.itemCount > 0) {
                                binding.recyclerAC.scrollToPosition(ma.itemCount - 1)
                            }
                        }
                    }
                }
                startConnect()
            } }
    }

    private fun handleButton() {
        with(binding) {

                ivSendAC.setOnClickListener {
                    if(!imageFlag){
                    var coment =etEditMessageAC.text.toString().trim()
                    if(!coment.isNullOrEmpty()){
                        val j = JsonObject()
                        j.addProperty(Com.ACTION, Com.MESSAGE)
                        var m = MessageHistory(0L,thisRoomId.toLong(),thisRoomName,userName)
                        m.comment = coment
                        m.type = Com.MESSAGE
                        j.addProperty(Com.MESSAGE,gsonDate.toJson(m))
                        LogDmy().d(TAG,"client send",j.toString())
                        client.send(gsonDate.toJson(j))
                        etEditMessageAC.setText("")
                        ivSelectImageMessage.visibility = View.GONE

                    }
                }else{

                        client.send( changeImageSize(image))
//                        client.send(changeImageSize2(image))
                        imageFlag = false
                        ivSelectImageMessage.visibility = View.GONE
                    }
            }
            ivAddImageMessage.setOnClickListener {
                val intent = Intent()
                intent.setType("image/*")
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
                intent.action = Intent.ACTION_GET_CONTENT

                pickImageLauncher.launch(intent)
            }
        }
    }

    private fun changeImageSize(byteImage: ByteArray): ByteArray{
        val srcBitmap = BitmapFactory.decodeByteArray(image,0,image.size)
        val srcWidth = srcBitmap.width
        val srcHeight = srcBitmap.height
        val longer = max(srcHeight,srcWidth)
        if(longer > newSize) {
            val result = longer / newSize.toDouble()
            val newWidth = srcWidth / result.toInt()
            val newHeight = srcHeight /result.toInt()
            val newBitmap =Bitmap.createScaledBitmap(srcBitmap, newWidth, newHeight, false)
            val baos = ByteArrayOutputStream()
            newBitmap.compress(Bitmap.CompressFormat.PNG,0,baos)
            return baos.toByteArray()
        }else{
            return byteImage
        }
    }

    private fun changeImageSize2(byteImage: ByteArray): ByteArray {
        val decoderListener =
            ImageDecoder.OnHeaderDecodedListener { imageDecoder, imageInfo, source ->
                val srcBitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
                val srcWidth = srcBitmap.width
                val srcHeight = srcBitmap.height
                val longer = max(srcHeight, srcWidth)
                if (longer > newSize) {
                    val result = longer / newSize.toDouble()
                    val newWidth = srcWidth / result.toInt()
                    val newHeight = srcHeight / result.toInt()
                    imageDecoder.setTargetSize(newWidth, newHeight)
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val source = ImageDecoder.createSource(byteImage)
            val newBitmap = ImageDecoder.decodeBitmap(source, decoderListener)
            val baos = ByteArrayOutputStream()
            newBitmap.compress(Bitmap.CompressFormat.PNG,0,baos)
            return baos.toByteArray()
        } else {
            return byteImage
        }
    }

    private var pickImageLauncher = registerForActivityResult(StartActivityForResult()) {
        result ->
        if(result.resultCode == RESULT_OK) {
            result.data?.clipData?.let { clipData ->
                for( i in 0 until  clipData.itemCount) {
                    var uri = clipData.getItemAt(i).uri
                    binding.ivSelectImageMessage.setImageURI(clipData.getItemAt(i).uri)
                    var bis = BufferedInputStream(this.contentResolver.openInputStream(uri))
                    image = ByteArray(bis.available())
                    bis.read(image)
                }
                binding.ivSelectImageMessage.visibility = View.VISIBLE
                imageFlag = true
                if(imageFlag){
                    LogDmy().d(TAG,"imagefla true")
                }

            }
        }
    }


    private fun startConnect() {
        val uri = URI.create(url)
        client = OrgClient(uri,this,binding)
        client.connectBlocking()
        val j = JsonObject()
        j.addProperty(Com.ACTION, Com.LOGIN)
        j.addProperty(Com.NAME, thisRoomName)
        j.addProperty(Com.USER, userName)
        client.send(gsonDate.toJson(j))
        LogDmy().d(TAG,"startConnect",j.toString())

    }

    override fun onDestroy() {
        super.onDestroy()
        client.closeBlocking()
    }

    private fun showMessageList() {
        if(messageList.isNullOrEmpty()){
            return
        }
        with(binding) {
            if(adapter == null ){
                adapter = MessageAdapter(messageList,userName,this@MessageActivity)
                binding.recyclerAC.adapter = adapter

                if(messageList.size > 0){
                    recyclerAC.scrollToPosition(messageList.size - 1 )
                }
            }else{
                adapter.setList(messageList)
            }
        }
    }
    private fun getNetData(compareId: Long) {
        var j = JsonObject()
        j.addProperty(Com.ACTION, Com.QUERY)
        j.addProperty(Com.DATA, compareId.toString())
        j.addProperty(Com.TYPE, Com.MESSAGEID+Com.NAME)
        j.addProperty(Com.DATA2, thisRoomName)
        LogDmy().d(TAG,"getNetData send",j.toString())
        runBlocking {
            var backString = Connection().httpPost(Com.servletURL+"MessageServlet", j.toString())
            LogDmy().d(TAG,"getNetData backString",backString)
            var type = object: TypeToken<List<MessageHistory>>() {}.type
            var dwMessList: List<MessageHistory> = gsonDate.fromJson(backString,type)

            if(!dwMessList.isNullOrEmpty()){
                LogDmy().d(TAG,"dwMessList",dwMessList.size.toString())
                if(messageList.isNullOrEmpty()){
                    messageList = dwMessList as MutableList<MessageHistory>
                }else{
                    for(item in dwMessList) {
                        messageList.add(item)
                    }
                }
                saveLocalData(thisRoomName)
            }
        }
    }

    private fun saveLocalData(thisRoomName: String) {
        if(!messageList.isNullOrEmpty()){
            openFileOutput(thisRoomName, MODE_PRIVATE)
                .bufferedWriter().use {
                    it.write(gsonDate.toJson(messageList))
                }
            openFileOutput(thisRoomName +"-MessID", MODE_PRIVATE)
                .bufferedWriter().use {
                    it.write(messageList.get(messageList.size-1).messageId.toString())
                }
        }
    }

    private fun getLocalData(roomName: String) {
        if(roomName.isNullOrEmpty()){
            return
        }
        var s : String ?= null
        val file = File(this.filesDir,roomName)
        if(file.exists()){
            var readName = StringBuilder()
            var readId = StringBuilder()
            this.openFileInput(roomName).bufferedReader().useLines {
                it.iterator().forEach { line ->
                    readName.append(line)
                }
            }
            LogDmy().d(TAG,"readName",roomName.toString())
            this.openFileInput(roomName+"-MessID").bufferedReader().useLines {
                it.iterator().forEach { line2 ->
                    readId.append(line2)
                }
            }
            lastMessID = readId.toString().toLong()
            val type = object : TypeToken<List<MessageHistory>>() {}.type
            messageList = gsonDate.fromJson(readName.toString(),type)
        }
        if(lastMessID < 1L){
            lastMessID = 1L
        }
    }

    private fun initial() {
        thisRoomName = intent.extras?.getString(Com.NAME).toString()
        thisRoomId = intent.extras?.getString(Com.ID).toString()
        LogDmy().d(TAG,thisRoomId,"/"+thisRoomName)

        binding.recyclerAC.layoutManager = LinearLayoutManager(this@MessageActivity)

        adapter = MessageAdapter(messageList,userName,this)
        binding.recyclerAC.adapter = adapter

        if(!thisRoomName.isNullOrEmpty()){
            val otherMember = intent.extras?.getString(Com.MEMBER)?.trim()
            if(!otherMember.isNullOrEmpty()){
                LogDmy().d(TAG,"otherMember",otherMember)
                binding.tvTitleAC.text = otherMember
            }else{
                binding.tvTitleAC.text = thisRoomName
            }
        }
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            this, "login", masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

}