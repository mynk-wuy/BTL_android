package com.example.btl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class UpdateInfoFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var etFullname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnUpdate: Button
    private val username by lazy { SessionManager.getUsername(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_update_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvUsername = view.findViewById(R.id.tvUsername)
        etFullname = view.findViewById(R.id.etFullname)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        btnUpdate = view.findViewById(R.id.btnUpdateInfo)

        tvUsername.text = "Tên đăng nhập: $username"

        loadUserInfo()

        btnUpdate.setOnClickListener {
            updateUserInfo()
        }
    }

    private fun loadUserInfo() {
        val url = "http://192.168.95.235/btl/get_user_info.php"
        val request = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    etFullname.setText(json.getString("fullname"))
                    etEmail.setText(json.getString("email"))
                    etPhone.setText(json.getString("phone"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Không lấy được thông tin!", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show() }) {
            override fun getParams(): Map<String, String> {
                return mapOf("username" to username)
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun updateUserInfo() {
        val url = "http://192.168.95.235/btl/update_user_info.php"
        val updatedFullname = etFullname.text.toString()
        val request = object : StringRequest(Method.POST, url,
            { response ->
                if (response.trim() == "success") {
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    // Lưu fullname vào SharedPreferences
                    val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("fullname", updatedFullname)  // Lưu fullname sau khi cập nhật
                    editor.apply()

                    // Chuyển sang ControlActivity và truyền fullname
                    val intent = Intent(requireContext(), ControlActivity::class.java)
                    intent.putExtra("fullname", updatedFullname)  // Truyền fullname đã cập nhật
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show() }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "username" to username,
                    "fullname" to etFullname.text.toString(),
                    "email" to etEmail.text.toString(),
                    "phone" to etPhone.text.toString()
                )
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }
}

