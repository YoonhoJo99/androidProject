package com.example.myfirebase

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShowMessageActivity : AppCompatActivity() {

    private lateinit var messageTextView: TextView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_message)

        messageTextView = findViewById(R.id.messageTextView)
        firestore = FirebaseFirestore.getInstance()

        // 현재 접속한 유저의 messageList를 가져와서 출력
        val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName
        if (currentUserName != null) {
            getMessageList(currentUserName) { messageText ->
                messageTextView.text = messageText
            }
        }
    }

    // 현재 접속한 유저의 messageList를 Firestore에서 가져오는 함수
    private fun getMessageList(userName: String, callback: (String) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("name", userName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                val messageList = document?.getString("messageList") ?: ""
                callback(messageList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user message list: $exception")
            }
    }
}




