package com.example.btl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class ControlActivity : AppCompatActivity() {
    private lateinit var switchLight1: Switch
    private lateinit var switchLight2: Switch
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button
    private lateinit var btnEditorInfo: Button
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 3000L // 3 giây
    private lateinit var fullname: String  // Biến toàn cục
    private var backPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        switchLight1 = findViewById(R.id.switchLight1)
        switchLight2 = findViewById(R.id.switchLight2)
        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)
        btnEditorInfo = findViewById(R.id.btnEditProfile)

        // val fullname = intent.getStringExtra("fullname") ?: getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fullname", "Unknown")
        fullname = intent.getStringExtra("fullname")
            ?: getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fullname", "Unknown") ?: "Unknown"

        // Gán vào TextView để hiển thị
        val textGreeting = findViewById<TextView>(R.id.textGreeting)
        textGreeting.text = "Welcome $fullname"


        btnEditorInfo.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Xử lý khi nhấn nút xem lịch sử
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Xử lý bật/tắt đèn 1
        switchLight1.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            updateLightStatus("den1", status)
        }

        switchLight2.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) 1 else 0
            updateLightStatus("den2", status)
        }

        // Bắt đầu cập nhật trạng thái đèn liên tục
        startStatusUpdates()

        btnLogout.setOnClickListener {
            // Quay về màn hình đăng nhập
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (backPressedOnce) {
            finishAffinity() // Thoát hoàn toàn ứng dụng
            return
        }

        this.backPressedOnce = true
        Toast.makeText(this, "Nhấn lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT).show()

        backPressHandler.postDelayed({
            backPressedOnce = false
        }, 2000) // 2 giây để nhấn lần thứ 2
    }

    private fun startStatusUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                getLightStatus()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun getLightStatus() {
        val request = Request.Builder()
            .url("http://192.168.95.235/btl/get_status.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ControlActivity, "Lỗi kết nối đến server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                try {
                    val jsonArray = JSONArray(res)
                    runOnUiThread {
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val device = item.getString("device")
                            val status = item.getInt("status")

                            if (device == "den1") {
                                switchLight1.setOnCheckedChangeListener(null)
                                switchLight1.isChecked = status == 1
                                switchLight1.setOnCheckedChangeListener { _, isChecked ->
                                    val newStatus = if (isChecked) 1 else 0
                                    updateLightStatus("den1", newStatus)
                                }
                            }
                            if (device == "den2") {
                                switchLight2.setOnCheckedChangeListener(null)
                                switchLight2.isChecked = status == 1
                                switchLight2.setOnCheckedChangeListener { _, isChecked ->
                                    val newStatus = if (isChecked) 1 else 0
                                    updateLightStatus("den2", newStatus)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun updateLightStatus(device: String, status: Int) {
        val requestBody = FormBody.Builder()
            .add("device", device)
            .add("status", status.toString())
            .add("fullname", fullname)
            .build()

        val request = Request.Builder()
            .url("http://192.168.95.235/btl/update_status.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ControlActivity, "Không gửi được lệnh điều khiển", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Có thể xử lý nếu cần phản hồi
            }
        })
    }
}
