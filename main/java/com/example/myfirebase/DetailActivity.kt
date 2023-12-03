package com.example.myfirebase

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class DetailActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val user = intent.getStringExtra("user")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        var price = intent.getDoubleExtra("price", 0.0)
        var salesWhether = intent.getBooleanExtra("salesWhether", false)

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.displayName

        val userTextView = findViewById<TextView>(R.id.user)
        val titleTextView = findViewById<TextView>(R.id.title)
        val descriptionTextView = findViewById<TextView>(R.id.description)
        val priceTextView = findViewById<TextView>(R.id.price)
        val salesWhetherTextView = findViewById<TextView>(R.id.salesWhether)
        val modifyButton = findViewById<Button>(R.id.modifyButton)
        val editPrice = findViewById<EditText>(R.id.editPrice) // editPrice는 가격을 수정하는 EditText의 ID입니다.
        val editSalesWhether = findViewById<CheckBox>(R.id.editSalesWhether) // editSalesWhether는 판매 여부를 수정하는 CheckBox의 ID입니다.
        val messageButton = findViewById<Button>(R.id.messageButton) //메시지 버튼 내가 나의 글이 아닌 경우에만 보이게한다

        userTextView.text = user
        titleTextView.text = title
        descriptionTextView.text = description
        priceTextView.text = price.toString()
        salesWhetherTextView.text = if (salesWhether) "판매 중" else "판매 완료"

        messageButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this)

            builder.setTitle("메시지를 입력해주세요")
            builder.setView(input)

            builder.setPositiveButton("전송") { dialog, _ ->
                // 여기에 액션을 정의해주세요.
                val m_Text = "From  ${currentUserEmail.toString()} :  " + input.text.toString() + "\n\n"
                val db = FirebaseFirestore.getInstance()

                db.collection("users").whereEqualTo("name", user)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val messageList = document.getString("messageList")
                            val updatedMessageList = "$messageList $m_Text"
                            document.reference.update("messageList", updatedMessageList)
                        }
                    }
                dialog.dismiss()
                startActivity(Intent(this, NextActivity::class.java))
            }
            builder.setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }




        if (currentUserEmail != null && currentUserEmail == user) { //Email과 비교한 게 아닌 현재 접속한 user의 name과 비교하였음
            // 수정 버튼과 수정 입력 필드를 보이게 하고, 기존 값으로 초기화
            modifyButton.visibility = View.VISIBLE
            editPrice.visibility = View.VISIBLE
            editSalesWhether.visibility = View.VISIBLE
            editPrice.setText(price.toString())
            editSalesWhether.isChecked = salesWhether

            modifyButton.setOnClickListener {
                // 수정 버튼을 누르면 Firestore에 수정 내용 업데이트
                price = editPrice.text.toString().toDouble()
                salesWhether = editSalesWhether.isChecked
                priceTextView.text = price.toString()
                salesWhetherTextView.text = if (salesWhether) "판매 중" else "판매 완료"

                val db = FirebaseFirestore.getInstance()
                val itemsRef = db.collection("items")

                // user와 title로 문서를 찾음
                itemsRef.whereEqualTo("user", user).whereEqualTo("title", title)
                    .get()
                    .addOnSuccessListener { documents ->
                        val documentId = documents.firstOrNull()?.id // 첫 번째 문서의 ID를 얻음

                        if (documentId != null) {
                            // 문서 ID를 사용해서 문서 참조를 생성
                            val docRef = db.collection("items").document(documentId)

                            val updates = hashMapOf<String, Any>(
                                "price" to price,
                                "salesWhether" to salesWhether
                            )

                            docRef.update(updates)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                        } else {
                            // 조건에 맞는 문서가 없는 경우의 처리
                            Log.w(TAG, "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }

                val intent = Intent(this@DetailActivity, NextActivity::class.java)
                startActivity(intent)
            }

        } else {
            Toast.makeText(this, "당신이 올린 물품이 아닙니다. 메시지를 보낼 수 있어요", Toast.LENGTH_SHORT).show()

            // 메시지 보내기 버튼을 보이게 설정
            messageButton.visibility = View.VISIBLE
        }
    }
}


