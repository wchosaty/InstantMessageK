package com.net.instantmessagek

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.ItemMessageBinding
import com.net.instantmessagek.pojo.MessageHistory
import com.net.instantmessagek.viewmodel.MessageViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class MessageAdapter(private var list: MutableList<MessageHistory>, private var userName: String, private val activity: Activity):
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val TAG = "TAG ${javaClass.simpleName}"

        fun setList(list: MutableList<MessageHistory>) {
            this.list = list
            notifyDataSetChanged()
        }

        fun addItem(messageHistory: MessageHistory) {
            list.add(messageHistory)
            notifyItemChanged(list.size - 1)
            LogDmy().d(TAG,"addItem size",list.size.toString())
        }

        override fun getItemCount(): Int {
            return list.count()
        }

        class MessageViewHolder(val itemMessageBinding: ItemMessageBinding) :
            RecyclerView.ViewHolder(itemMessageBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val itemMessageBinding =
                ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemMessageBinding.messViewModel = MessageViewModel()
            return MessageViewHolder(itemMessageBinding)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val m =list[position]
            with(holder.itemMessageBinding){
                if(userName == m.fromUser){
                    LayoutOther.visibility = View.GONE
                    LayoutUser.visibility = View.VISIBLE

                    if (m.action == Com.IMAGE) {
                        tvMessageUser.visibility = View.GONE
                        ivImageUser.visibility = View.VISIBLE
                        var j = JsonObject()
                        j.addProperty(Com.ACTION, Com.QUERY)
                        j.addProperty(Com.DATA, m.messageId)
                        j.addProperty(Com.TYPE, Com.IMAGE)
                        runBlocking {
                            var byteArray: ByteArray? = null
                            val file = File(activity.filesDir, m.roomName + "-" + m.messageId + ".png")
                            val pathString = m.roomName + "-" + m.messageId + ".png"
                            LogDmy().d(TAG,"id",m.messageId.toString())
                            if (file.exists()) {
                                LogDmy().d(TAG,"id","exists")
                                byteArray = activity.openFileInput(pathString).readBytes()
                            }

                            if (byteArray == null) {
                                LogDmy().d(TAG,"id","net")
                                byteArray = Connection()
                                    .httpImage(Com.servletURL + "MessageServlet", j.toString())

                                val out = activity.openFileOutput(pathString, Activity.MODE_PRIVATE)
                                out.write(byteArray)
                            }
                            ivImageUser.setImageBitmap(
                                BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size) )
                            // 固定imageSQL下載
//                            ivImageUser.setImageBitmap(Connection()
//                                .httpImage(Com.servletURL+"MessageServlet",j.toString()))
                            LogDmy().d(TAG, "imageUser", "imageBack")
                        }
                    } else {
                        tvMessageUser.text = m.comment
                        tvMessageUser.visibility = View.VISIBLE
                        ivImageUser.visibility = View.GONE

                    }
                }else{
                    LayoutOther.visibility = View.VISIBLE
                    LayoutUser.visibility = View.GONE
                    tvUserNameOther.text = m.fromUser.trim()
                    if(m.action == Com.IMAGE){
                        tvMessageOther.visibility = View.GONE
                        ivImageOther.visibility = View.VISIBLE
                        var j = JsonObject()
                        j.addProperty(Com.ACTION,Com.QUERY)
                        j.addProperty(Com.DATA,m.messageId)
                        j.addProperty(Com.TYPE,Com.IMAGE)
                        runBlocking {
                            var byteArray: ByteArray? = null
                            val file = File(activity.filesDir, m.roomName + "-" + m.messageId + ".png")
                            val pathString = m.roomName + "-" + m.messageId + ".png"
                            LogDmy().d(TAG,"id",m.messageId.toString())
                            if (file.exists()) {
                                LogDmy().d(TAG,"id","exists")
                                byteArray = activity.openFileInput(pathString).readBytes()
                            }

                            if (byteArray == null) {
                                LogDmy().d(TAG,"id","net")
                                byteArray = Connection()
                                    .httpImage(Com.servletURL + "MessageServlet", j.toString())

                                val out = activity.openFileOutput(pathString, Activity.MODE_PRIVATE)
                                out.write(byteArray)
                            }
                            ivImageOther.setImageBitmap(
                                BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size) )
                            // 固定imageSQL下載
//                                ivImageOther.setImageBitmap(Connection()
//                                    .httpImage(Com.servletURL+"MessageServlet",j.toString()) )
                            LogDmy().d(TAG,"imageOther","imageBack")
                        }
                    }else{
                        tvMessageOther.text = m.comment
                        tvMessageOther.visibility = View.VISIBLE
                        ivImageOther.visibility = View.GONE
                    }
                    if(position >= 1){
                        val m2 = list[position-1]
                        if(m.fromUser == m2.fromUser){
                            tvUserNameOther.visibility = View.GONE
                        }else{
                            tvUserNameOther.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
}