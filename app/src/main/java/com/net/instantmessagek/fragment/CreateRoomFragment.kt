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
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.net.instantmessagek.FriendAdapter
import com.net.instantmessagek.R
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.FragmentCreateRoomBinding
import com.net.instantmessagek.pojo.RoomList
import com.net.instantmessagek.pojo.RoomMember
import com.net.instantmessagek.viewmodel.FriendViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CreateRoomFragment : Fragment() {
    private val TAG = "TAG ${javaClass.simpleName}"
    private lateinit var binding: FragmentCreateRoomBinding
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
        val name = preferences.getString(Com.NAME, "")
        if (!name.isNullOrEmpty()) {
            userName = name
        } else {
            LogDmy().d(TAG, "useName", "null")
        }
        val viewModel: FriendViewModel by viewModels()
        binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        binding.friendViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            recyclerCreateRoom.layoutManager = LinearLayoutManager(requireContext())
            friendViewModel?.friends?.observe(viewLifecycleOwner) { friends ->
                if (recyclerCreateRoom.adapter == null) {
                    var adapter = FriendAdapter(friends, userName, requireActivity())
                    adapter.chooseFlag = true
                    recyclerCreateRoom.adapter = adapter
                } else {
                    (recyclerCreateRoom.adapter as FriendAdapter).setList(friends)
                }
            }
            ivDoneCreateRoom.setOnClickListener {
                var adapter = recyclerCreateRoom.adapter as FriendAdapter
                if (adapter != null) {
                    var roomName = etRoomNameCreateRoom.text.toString().trim()
                    if (!roomName.isNullOrEmpty()) {
                        var list = mutableListOf<RoomMember>()
                        val roomId = System.currentTimeMillis()
                        list.add(RoomMember(roomId, roomName, userName))
                        for (i in 1 until adapter.friends.size) {
                            if (adapter.chooses[i] == true) {
                                list.add(RoomMember(roomId, roomName, adapter.friends.get(i).name))
                            }
                        }
                        runBlocking {
                            var jMember = JsonObject()
                            jMember.addProperty(Com.ACTION, Com.BATCH_INSERT)
                            jMember.addProperty(Com.DATA, Gson().toJson(list))
                            jMember.addProperty(Com.TYPE, Com.MEMBER)
                            var backMember = Connection().httpPost(
                                Com.servletURL + "RoomServlet",
                                jMember.toString()
                            )

                            var roomList = RoomList(roomId, roomName, Com.EMPTY)
                            roomList.memberCount = list.size
                            var jList = JsonObject()
                            jList.addProperty(Com.ACTION, Com.INSERT)
                            jList.addProperty(Com.DATA, Gson().toJson(roomList))
                            jList.addProperty(Com.TYPE, Com.LIST)
                            var backList = Connection().httpPost(
                                Com.servletURL + "RoomServlet",
                                jList.toString()
                            )

                            if (!backList.isNullOrEmpty() && !backMember.isNullOrEmpty()) {
                                Navigation.findNavController(view)
                                    .navigate(R.id.action_createRoomFragment_to_roomListFragment)
                            } else {
                                Snackbar.make(view, "聊天室建立失敗", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            ivCancelCreateRoom.setOnClickListener {
                Navigation.findNavController(view).navigate(R.id.action_createRoomFragment_to_roomListFragment)
            }
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

    override fun onStart() {
        super.onStart()
        binding.friendViewModel?.getFriends(userName)
    }
}