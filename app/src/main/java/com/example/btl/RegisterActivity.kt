package com.example.btl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private lateinit var edtUser: EditText
    private lateinit var edtPass: EditText
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var btnSubmit: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edtUser = findViewById(R.id.edtUser)
        edtPass = findViewById(R.id.edtPass)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val username = edtUser.text.toString()
            val password = edtPass.text.toString()
            val fullname = edtName.text.toString()
            val email = edtEmail.text.toString()
            val phone = edtPhone.text.toString()

            if (username.isEmpty() || password.isEmpty() || fullname.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi API kiểm tra tài khoản trước khi đăng ký
            checkUserExists(username, email, phone) { exists ->
                if (exists) {
                    runOnUiThread {
                        Toast.makeText(this, "Tài khoản, email hoặc số điện thoại đã tồn tại! Vui lòng nhập lại", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    sendRegisterRequest(username, password, fullname, email, phone)
                }
            }
        }
    }

    private fun checkUserExists(username: String, email: String, phone: String, onResult: (Boolean) -> Unit) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("email", email)
            .add("phone", phone)
            .build()

        val request = Request.Builder()
            .url("http://192.168.95.235/btl/check_user.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Không thể kết nối đến server!", Toast.LENGTH_SHORT).show()
                }
                onResult(true) // Mặc định không cho đăng ký khi lỗi
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                val json = JSONObject(result)
                val exists = json.getBoolean("exists")
                onResult(exists)
            }
        })
    }

    private fun sendRegisterRequest(username: String, password: String, fullname: String, email: String, phone: String) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .add("fullname", fullname)
            .add("email", email)
            .add("phone", phone)
            .build()

        val request = Request.Builder()
            .url("http://192.168.95.235/btl/register.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Lỗi khi đăng ký!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                }
            }
        })
    }
}
