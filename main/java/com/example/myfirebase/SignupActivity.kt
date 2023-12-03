package com.example.myfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirebase.NextActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var birthDateEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    //메시지구현코드
    private lateinit var messageListText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        birthDateEditText = findViewById(R.id.birthDateEditText)
        //
        messageListText = findViewById(R.id.messageList)

        signupButton = findViewById(R.id.signupButton)
        firebaseAuth = FirebaseAuth.getInstance()

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val birthDate = birthDateEditText.text.toString()
            //메시지 기능 구현.. 메시지의 리스트를 저장할 text다. 안보이게 하고 같이 저장하자 ㅋㅋ 이걸로 메시지구현
            val messageList = messageListText.text.toString();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser

                        // Firebase Authentication에 사용자 이름 등록
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Firestore에 해당 유저의 name과 birth 정보 저장
                                    val firestore = FirebaseFirestore.getInstance()
                                    val userDocument = firestore.collection("users").document(user!!.uid)
                                    val userData = hashMapOf(
                                        "name" to name,
                                        "birth" to birthDate,
                                        "messageList" to messageList
                                    )
                                    userDocument.set(userData)
                                        .addOnSuccessListener {
                                            startActivity(Intent(this, NextActivity::class.java))
                                            finish() // 회원가입 액티비티 종료
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

