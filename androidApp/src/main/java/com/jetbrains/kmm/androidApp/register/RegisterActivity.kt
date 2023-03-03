package com.jetbrains.kmm.androidApp.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.login.LoginActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val register_button: Button = findViewById(R.id.register)
        val login_button: TextView = findViewById(R.id.login)
        val username: EditText = findViewById(R.id.username)
        val password: EditText = findViewById(R.id.password)

        val viewModel = ViewModelProvider(this)[RegisterViewModel::class.java] as RegisterViewModel

        register_button.setOnClickListener {
            lifecycleScope.launch {
                val registerSuccess = viewModel.registration(username.text.toString(), password.text.toString())

                if (registerSuccess) {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@RegisterActivity, "Registration Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        login_button.setOnClickListener {

            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}