package com.example.btl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(historyList)
        recyclerView.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        val client = OkHttpClient()
        val request = Request.Builder().url("http://192.168.95.235/btl/get_history.php").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { body ->
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val item = HistoryItem(
                            time = obj.getString("time"),
                            fullname = obj.getString("fullname"),
                            device = obj.getString("device"),
                            status = obj.getString("status")
                        )
                        historyList.add(item)
                    }
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}
