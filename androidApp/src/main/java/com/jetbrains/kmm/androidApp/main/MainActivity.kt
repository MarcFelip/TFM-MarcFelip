package com.jetbrains.kmm.androidApp.main

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputEditText
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.addProject.AddProjectActivity
import com.jetbrains.kmm.androidApp.profile.ProfileActivity


class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 1
    private lateinit var image_view: ImageView
    private var image: Bitmap? = null
    private lateinit var final_image: Bitmap

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user_button: ImageButton = findViewById(R.id.btn_profile)
        val new_project_button: Button = findViewById(R.id.btn_new_project)

        user_button.setOnClickListener{
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        new_project_button.setOnClickListener{
            val intent = Intent(this@MainActivity, AddProjectActivity::class.java)
            startActivity(intent)
        }
    }


}