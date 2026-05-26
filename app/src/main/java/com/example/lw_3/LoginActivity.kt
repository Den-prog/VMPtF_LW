package com.example.lw_3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val btnLog: Button = findViewById(R.id.btnLog)
        val etUserName: EditText = findViewById(R.id.etUserName)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val btnRegister: Button = findViewById(R.id.btnRegister)

        btnLog.setOnClickListener {
            val name = etUserName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if(name.isNotBlank() && password.isNotBlank()) {
                val credentials = mapOf("name" to name, "password" to password)//створення словника

                RetrofitClient.instance.login(credentials).enqueue(object :
                    Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val user = response.body()?.user
                            if (user != null) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    putExtra("USER_NAME", user.name)
                                    putExtra("IS_ADMIN", user.isAdmin)
                                    putExtra("USER_ID", user.id)
                                }
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Невірний логін або пароль", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Сервер не відповідає", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        btnRegister.setOnClickListener {
            val name = etUserName.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isNotBlank() && password.isNotBlank()) {
                //формування даних для відправки (як очікує Express)
                val credentials = mapOf("name" to name, "password" to password)

                RetrofitClient.instance.register(credentials).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val user = response.body()?.user
                            if (user != null) {
                                Toast.makeText(this@LoginActivity, "Реєстрація успішна!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    putExtra("USER_NAME", user.name)
                                    putExtra("IS_ADMIN", user.isAdmin)
                                    putExtra("USER_ID", user.id)
                                }
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Користувач з таким ім'ям вже існує", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Помилка мережі: сервер недоступний", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@LoginActivity, "Будь ласка, заповніть логін і пароль", Toast.LENGTH_SHORT).show()
            }
}}}