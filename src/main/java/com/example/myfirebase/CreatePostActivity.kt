package com.example.myfirebase

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreatePostActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val toolbar: Toolbar? = findViewById(R.id.toolbar) // 뷰 ID 확인
        setSupportActionBar(toolbar)

        val btnSubmit: Button? = findViewById(R.id.btn_submit) // 뷰 ID 확인
        btnSubmit?.setOnClickListener { // null-safe 호출
            val title = findViewById<EditText>(R.id.edit_title).text.toString()
            val description = findViewById<EditText>(R.id.edit_description).text.toString()
            val price = findViewById<EditText>(R.id.edit_price).text.toString().toDouble()
            val user = FirebaseAuth.getInstance().currentUser?.displayName ?: ""

            val db = FirebaseFirestore.getInstance()
            val newPost = hashMapOf(
                "user" to user,
                "title" to title,
                "description" to description,
                "price" to price,
                "salesWhether" to true
            )

            db.collection("items")
                .add(newPost)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(this, "Failed to add post", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

