package com.example.btl

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class ChangePasswordFragment : Fragment() {

    private lateinit var edtOldPassword: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private val phpUrl = "http://192.168.95.235/btl/change_password.php"

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy username từ SessionManager
        username = SessionManager.getUsername(requireContext())
        Log.d("ChangePassword", "Username from Session: $username")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        edtOldPassword = view.findViewById(R.id.edtOldPassword)
        edtNewPassword = view.findViewById(R.id.edtNewPassword)
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener {
            changePassword()
        }

        return view
    }

    private fun changePassword() {
        val oldPass = edtOldPassword.text.toString()
        val newPass = edtNewPassword.text.toString()
        val confirmPass = edtConfirmPassword.text.toString()

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirmPass) {
            Toast.makeText(context, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Không lấy được tên người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val request = object : StringRequest(Method.POST, phpUrl,
            { response ->
                when (response.trim()) {
                    "success" -> {
                        Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()

                        val intent = Intent(requireContext(), ControlActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    "invalid" -> {
                        Toast.makeText(context, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(context, "Lỗi không xác định: $response", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            { error ->
                Toast.makeText(context, "Lỗi kết nối: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "username" to username,
                    "old_password" to oldPass,
                    "new_password" to newPass
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }
}
