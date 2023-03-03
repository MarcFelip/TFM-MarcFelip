package com.jetbrains.kmm.androidApp.addProject

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.MainActivity
import com.jetbrains.kmm.androidApp.profile.ProfileActivity
import com.jetbrains.kmm.androidApp.project.ProjectActivity

class AddProjectActivity : AppCompatActivity() {


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        val user_button: ImageButton = findViewById(R.id.btn_profile)
        val cancel_button: Button = findViewById(R.id.btn_cancel)
        val create_button: Button = findViewById(R.id.btn_create)

        user_button.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        create_button.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, ProjectActivity::class.java)
            startActivity(intent)
        }

        cancel_button.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

    }


}