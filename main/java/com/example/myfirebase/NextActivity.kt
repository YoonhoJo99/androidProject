package com.example.myfirebase

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase



//판매 목록을 보여주는 액티비티
class NextActivity : AppCompatActivity() {
    private lateinit var adapter: ItemAdapter
    private lateinit var items: ArrayList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        // 툴바 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        items = ArrayList<Item>()

        val db = FirebaseFirestore.getInstance()
        db.collection("items")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                items.clear() // 리스트를 초기화

                for (document in value!!) {
                    val user = document.getString("user") ?: ""
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val salesWhether = document.getBoolean("salesWhether") ?: false
                    items.add(Item(user, title, description, price, salesWhether))
                }
                adapter.notifyDataSetChanged() // 데이터 변경을 어댑터에 알린다. -> 올린 글이 바로 반영되도록 하기 위해
            }

        adapter = ItemAdapter(items) { item ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("user", item.user)
                putExtra("title", item.title)
                putExtra("description", item.description)
                putExtra("price", item.price)
                putExtra("salesWhether", item.salesWhether)
            }
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    //메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //판매 여부 구분하는 것
            R.id.filter_sales_O -> {
                val filteredItems = items.filter { it.salesWhether }
                adapter.updateItems(filteredItems)
                true
            }
            R.id.filter_sales_X -> {
                val filteredItems = items.filter { !it.salesWhether }
                adapter.updateItems(filteredItems)
                true
            }
            //물건 등록
            R.id.new_post -> {
                val intent = Intent(this, CreatePostActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.messageList -> {
                val intent = Intent(this, ShowMessageActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}

data class Item(
    val user: String,
    val title: String,
    val description: String,
    val price: Double,
    val salesWhether: Boolean
)


class ItemAdapter(
    private var items: List<Item>,
    private val listener: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.title)
        val price = view.findViewById<TextView>(R.id.price)
        val salesWhether = view.findViewById<TextView>(R.id.salesWhether)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.price.text = item.price.toString()
        holder.salesWhether.text = if (item.salesWhether) "판매 중" else "판매 완료"
        holder.itemView.setOnClickListener { listener(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

}






