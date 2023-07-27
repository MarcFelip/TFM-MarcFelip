package com.jetbrains.kmm.androidApp.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.MainActivity
import com.jetbrains.kmm.androidApp.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerButton: TextView = findViewById(R.id.login)
        val loginButton: Button = findViewById(R.id.register)
        val username: EditText = findViewById(R.id.username)
        val password: EditText = findViewById(R.id.password)
        val passwordLayout: TextInputLayout = findViewById(R.id.passwordLayout)
        passwordLayout.isEndIconVisible = true // Muestra el botón de alternancia


        val viewModel = ViewModelProvider(this)[LoginViewModel::class.java] as LoginViewModel

        loginButton.setOnClickListener {
            lifecycleScope.launch {
                val loginSuccess = viewModel.doLogin(username.text.toString(), password.text.toString())

                if (loginSuccess) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "Username or password incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}