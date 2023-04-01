package com.net.instantmessagek.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.net.instantmessagek.R
import com.net.instantmessagek.RoomAdapter
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.FragmentRoomListBinding
import com.net.instantmessagek.pojo.RoomMember
import com.net.instantmessagek.viewmodel.RoomViewModel
import kotlinx.coroutines.runBlocking


class RoomListFragment : Fragment() {
    private val TAG = "TAG ${javaClass.simpleName}"
    private lateinit var binding: FragmentRoomListBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var userName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferences = createEncryptedPreferences()
        val name = preferences.getString(Com.NAME,"")
        if(!name.isNullOrEmpty()){
            LogDmy().d(TAG,"userName",name)
            userName = name
        }else LogDmy().d(TAG,"userName","null")

        val viewModel: RoomViewModel by viewModels()
        binding = FragmentRoomListBinding.inflate(inflater, container ,false)
        binding.roomViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            recyclerRoom.layoutManager = LinearLayoutManager(requireContext())
            roomViewModel?.roomMembers?.observe(viewLifecycleOwner) { members ->
                if(recyclerRoom.adapter == null ){
                    recyclerRoom.adapter = RoomAdapter(members,requireActivity())
                }else{
                    (recyclerRoom.adapter as RoomAdapter).setList(members)
                }

            }
            ivFriendListRoom.setOnClickListener {
                Navigation.findNavController(view).navigate(R.id.action_roomListFragment_to_friendListFragment)
            }
            btAddRoom.setOnClickListener {
                Navigation.findNavController(view).navigate(R.id.action_roomListFragment_to_createRoomFragment)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getRooms()
    }

    private fun getRooms() {
       val j = JsonObject()
        j.addProperty(Com.ACTION,Com.QUERY)
        j.addProperty(Com.DATA,userName)
        j.addProperty(Com.TYPE,Com.MEMBER)
        runBlocking{
            val backString = Connection().httpPost(Com.servletURL + "RoomServlet",j.toString())
            LogDmy().d(TAG,"backString",backString)
            val gsonDate = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            val type = object : TypeToken<List<RoomMember>>() {}.type
            binding.roomViewModel?.roomMembers?.value  = gsonDate.fromJson<List<RoomMember>>(backString, type)
        }
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            requireContext(), "login", masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
}