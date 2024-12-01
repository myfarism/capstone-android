package com.example.capstonebangkitpawers.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capstonebangkitpawers.R
import com.example.capstonebangkitpawers.view.ChatViewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatBotFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_bot, container, false)

        val fab: FloatingActionButton = view.findViewById(R.id.btn_add_story)

        fab.setOnClickListener {
            val intent = Intent(activity, ChatViewActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
