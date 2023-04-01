package com.net.instantmessagek

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.ItemFriendBinding
import com.net.instantmessagek.pojo.RoomList
import com.net.instantmessagek.pojo.User
import com.net.instantmessagek.viewmodel.FriendViewModel
import kotlinx.coroutines.runBlocking

class FriendAdapter(var friends:List<User>,private val userName: String,private var activity: Activity):
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    private val TAG = "TAG ${javaClass.simpleName}"
    var chooseFlag = false
    var chooses = mutableMapOf<Int,Boolean>()

    fun setList(friends: List<User>) {
        this.friends = friends
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return friends.count()
    }

    class FriendViewHolder(val itemFriendBinding: ItemFriendBinding) :
    RecyclerView.ViewHolder(itemFriendBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemFriendBinding =
            ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemFriendBinding.friendViewModel = FriendViewModel()
        return FriendViewHolder(itemFriendBinding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val user = friends[position]
        with(holder.itemFriendBinding) {
            tvNameItemFriend.text = user.name
            tvCityItemFriend.text = user.city
            if(chooseFlag) cbChoseItemFriend.visibility = View.VISIBLE
            else cbChoseItemFriend.visibility = View.GONE
            cbChoseItemFriend.setOnClickListener {
                chooses.put(position, cbChoseItemFriend.isChecked)
            }
        }

        holder.itemView.setOnClickListener {
            if(!chooseFlag){
                val j = JsonObject()
                j.addProperty(Com.ACTION, Com.QUERY)
                j.addProperty(Com.DATA, Com.MEMBER)
                j.addProperty(Com.TYPE, Com.LIST)
                j.addProperty(Com.MEMBER+1,userName)
                j.addProperty(Com.MEMBER+2, user.name)
                runBlocking {
                    var back = Connection().httpPost(Com.servletURL+"RoomServlet", j.toString())
                    var r = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create().fromJson(back,RoomList::class.java)
                    if (r != null && !r.roomName.isNullOrEmpty()){
                        val intent = Intent().putExtra(Com.ID, r.roomId.toString())
                            .putExtra(Com.NAME, r.roomName)
                            .putExtra(Com.MEMBER, user.name)
                        intent.setClass(holder.itemFriendBinding.root.context,MessageActivity::class.java)
                        LogDmy().d(TAG,"roomName",r.roomName)
                        activity.startActivity(intent)
                    }
                }
            }

        }
    }

}