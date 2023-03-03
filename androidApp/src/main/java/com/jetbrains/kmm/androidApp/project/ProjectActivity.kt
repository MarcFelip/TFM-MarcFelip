package com.jetbrains.kmm.androidApp.project

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

class ProjectActivity : AppCompatActivity() {


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_screen)

        val user_button: ImageButton = findViewById(R.id.btn_profile)
        val add_image: Button = findViewById(R.id.btn_add_img)

        user_button.setOnClickListener{
            val intent = Intent(this@ProjectActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

    }


}