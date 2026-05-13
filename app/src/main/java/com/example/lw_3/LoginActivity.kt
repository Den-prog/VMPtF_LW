package com.example.lw_3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
        val cbAdmin: CheckBox = findViewById(R.id.checkBox)

        btnLog.setOnClickListener {
            val name = etUserName.text.toString()
            val isAdmin = cbAdmin.isChecked

            if(name.isNotBlank()){
                val intent = Intent(this, MainActivity::class.java).apply{
                    putExtra("USER_NAME", name)
                    putExtra("IS_ADMIN", isAdmin)
                }
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this, "Будь ласка, введіть ім'я", Toast.LENGTH_SHORT).show()
            }
        }

    }
}