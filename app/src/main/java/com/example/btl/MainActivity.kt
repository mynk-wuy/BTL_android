package com.example.btl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtUsername = findViewById<EditText>(R.id.edtUsername)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnFingerprint = findViewById<Button>(R.id.btnFingerprint)


        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString()
            val password = edtPassword.text.toString()
            login(username, password)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnFingerprint.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS) {

                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Xác thực thành công", Toast.LENGTH_SHORT).show()
                            }

                            // Gửi fingerprint_id lên server
                            val fingerprintId = getSavedFingerprintId()

                            if (fingerprintId != null) {
                                loginWithFingerprint(fingerprintId)
                            } else {
                                runOnUiThread {
                                    Toast.makeText(applicationContext, "Chưa liên kết vân tay với tài khoản", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Lỗi: $errString", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Vân tay không hợp lệ", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Xác thực vân tay")
                    .setSubtitle("Sử dụng vân tay để đăng nhập")
                    .setNegativeButtonText("Hủy")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(this, "Thiết bị không hỗ trợ hoặc chưa cài vân tay", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun loginWithFingerprint(fingerprintId: String) {
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("fingerprint_id", fingerprintId)
            .build()

        val request = Request.Builder()
            .url("http://192.168.95.235/btl/fingerprint_login.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Lỗi mạng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                try {
                    val json = JSONObject(res)
                    if (json.getString("status") == "success") {
                        val fullname = json.getString("fullname")
                        val username = json.getString("username")
                        SessionManager.saveUsername(this@MainActivity, username)
                        val intent = Intent(this@MainActivity, ControlActivity::class.java)
                        intent.putExtra("fullname", fullname)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Vân tay không khớp tài khoản nào", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Lỗi dữ liệu phản hồi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getSavedFingerprintId(): String? {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        return sharedPref.getString("fingerprint_id", null)
    }

    private fun login(username: String, password: String) {
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("http://192.168.95.235/btl/login.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res)
                if (json.getString("status") == "success") {
                    val fullname = json.getString("fullname")
                    val usernameFromServer = json.getString("username")
                    SessionManager.saveUsername(this@MainActivity, usernameFromServer)
                    val fingerprintId = json.getString("fingerprint_id")
                    val intent = Intent(this@MainActivity, ControlActivity::class.java)
                    intent.putExtra("fullname", fullname)
                    intent.putExtra("username", usernameFromServer)
                    startActivity(intent)
                    if (fingerprintId == "null") {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Tài khoản chưa xác thực vân tay. Yêu cầu xác thực...", Toast.LENGTH_SHORT).show()
                            authenticateAndBindFingerprint(usernameFromServer, fullname)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Username or password is empty", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun authenticateAndBindFingerprint(username: String, fullname: String) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // Tạo fingerprint ID ngẫu nhiên (hoặc cố định theo thiết bị)
                val existingId = getSavedFingerprintId()
                val fingerprintId = existingId ?: UUID.randomUUID().toString()
                if (existingId == null) {
                    saveFingerprintId(fingerprintId)
                }

                // Gửi lên server để liên kết tài khoản
                val client = OkHttpClient()
                val formBody = FormBody.Builder()
                    .add("username", username)
                    .add("fingerprint_id", fingerprintId)
                    .build()

                val request = Request.Builder()
                    .url("http://192.168.95.235/btl/bind_fingerprint.php")
                    .post(formBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val res = response.body?.string()
                        val json = JSONObject(res)
                        if (json.getString("status") == "success") {
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Đã liên kết vân tay thành công", Toast.LENGTH_SHORT).show()
                            }
                            // Chuyển tới màn hình chính
                            val intent = Intent(this@MainActivity, ControlActivity::class.java)
                            intent.putExtra("fullname", fullname)
                            intent.putExtra("username", username)
                            //startActivity(intent)
                        } else {
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Liên kết thất bại", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Lỗi: $errString", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Vân tay không hợp lệ", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Liên kết vân tay")
            .setSubtitle("Xác thực để liên kết tài khoản")
            .setNegativeButtonText("Hủy")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun saveFingerprintId(fingerprintId: String) {
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("fingerprint_id", fingerprintId)
            apply()
        }
    }
}