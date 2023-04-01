package com.net.instantmessagek

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.ItemRoomBinding
import com.net.instantmessagek.pojo.RoomMember
import com.net.instantmessagek.viewmodel.RoomViewModel
import kotlinx.coroutines.runBlocking

class RoomAdapter(private var list: List<RoomMember>,private var activity: Activity):
    RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    private val TAG = "TAG ${javaClass.simpleName}"

    class RoomViewHolder(val itemRoomBinding: ItemRoomBinding):RecyclerView.ViewHolder(itemRoomBinding.root)

    fun setList(list: List<RoomMember>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val itemRoomBinding =
            ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemRoomBinding.roomViewModel = RoomViewModel()
        return RoomViewHolder(itemRoomBinding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val roomMember = list[position]

        with(holder.itemRoomBinding) {
            if(!roomMember.otherMember.isNullOrEmpty()){
                tvRoomNameItemRoom.text = roomMember.otherMember
            }else{
                tvRoomNameItemRoom.text = roomMember.roomName
            }
            tvMessItemRoom.text = ""
            val j = JsonObject()
            j.addProperty(Com.ACTION,Com.QUERY)
            j.addProperty(Com.DATA,roomMember.roomName)
            j.addProperty(Com.TYPE,Com.MESSAGE)
            runBlocking {
                var mess = Connection().httpPost(Com.servletURL +"RoomServlet", j.toString())
                if(mess == Com.EMPTY) {
                    tvMessItemRoom.text = "貼圖"
                }else{
                    tvMessItemRoom.text =mess
                }
            }
        }
        holder.itemView.setOnClickListener{
            val intent = Intent().putExtra(Com.ID,roomMember.roomId.toString())
                .putExtra(Com.NAME,roomMember.roomName)
                .putExtra(Com.MEMBER, roomMember.otherMember)
            intent.setClass(holder.itemRoomBinding.root.context,MessageActivity::class.java)
            LogDmy().d(TAG,"roomName",roomMember.roomName)
            activity.startActivity(intent)
            activity.finish()
        }
    }

}