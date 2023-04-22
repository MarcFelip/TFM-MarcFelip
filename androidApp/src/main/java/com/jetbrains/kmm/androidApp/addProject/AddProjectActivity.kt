package com.jetbrains.kmm.androidApp.addProject

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.MainActivity
import com.jetbrains.kmm.androidApp.profile.ProfileActivity
import com.jetbrains.kmm.androidApp.project.ProjectActivity
import com.jetbrains.kmm.androidApp.addProject.AddProjectViewModel
import com.jetbrains.kmm.androidApp.profile.ProfileViewModel

class AddProjectActivity : AppCompatActivity() {

    private lateinit var viewModel: AddProjectViewModel
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        viewModel = ViewModelProvider(this).get(AddProjectViewModel::class.java)

        val user_button: ImageButton = findViewById(R.id.btn_profile)
        val home_button: ImageButton = findViewById(R.id.btn_home)
        val cancel_button: Button = findViewById(R.id.btn_cancel)
        val create_button: Button = findViewById(R.id.btn_create)
        val name: EditText = findViewById(R.id.name_project)
        val location: EditText = findViewById(R.id.location)
        val variety: EditText = findViewById(R.id.variety)
        val data: EditText = findViewById(R.id.data)

        user_button.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        home_button.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

        create_button.setOnClickListener{
            lifecycleScope.launch {
                val name_str = name.text.toString()
                val location_str = location.text.toString()
                val variety_str = variety.text.toString()
                val data_str = data.text.toString()

                val addSucces = viewModel.addProject(name_str, location_str, variety_str, data_str)

                if (addSucces) {
                    val intent = Intent(this@AddProjectActivity, ProjectActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@AddProjectActivity,"Can't create the project",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        cancel_button.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

    }


}