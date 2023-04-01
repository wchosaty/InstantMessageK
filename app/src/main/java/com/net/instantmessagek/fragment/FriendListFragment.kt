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
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.net.instantmessagek.FriendAdapter
import com.net.instantmessagek.R
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.databinding.FragmentFriendListBinding
import com.net.instantmessagek.pojo.RoomMember
import com.net.instantmessagek.pojo.User
import com.net.instantmessagek.viewmodel.FriendViewModel
import kotlinx.coroutines.runBlocking

class FriendListFragment : Fragment() {
    private val TAG = "TAG ${javaClass.simpleName}"
    private lateinit var binding: FragmentFriendListBinding
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

        val viewModel: FriendViewModel by viewModels()
        binding = FragmentFriendListBinding.inflate(inflater, container, false)
        binding.friendViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvNameMyUser.text = userName
            recyclerFriend.layoutManager = LinearLayoutManager(requireContext())
            friendViewModel?.friends?.observe(viewLifecycleOwner) { friends ->
                if(recyclerFriend.adapter == null) {
                    recyclerFriend.adapter = FriendAdapter(friends,userName,requireActivity())

                }else{
                    (recyclerFriend.adapter as FriendAdapter).setList(friends)
                }
            }
            ivRoomListFriend.setOnClickListener {
                Navigation.findNavController(view).navigate(R.id.action_friendListFragment_to_roomListFragment)
            }
            }
        }

    override fun onStart() {
        super.onStart()
        binding.friendViewModel?.getFriends(userName)
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