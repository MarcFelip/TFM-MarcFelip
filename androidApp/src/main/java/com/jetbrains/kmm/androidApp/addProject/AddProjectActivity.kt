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

class AddProjectActivity : AppCompatActivity() {

    private lateinit var viewModel: AddProjectViewModel
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        viewModel = ViewModelProvider(this)[AddProjectViewModel::class.java]

        val userButton: ImageButton = findViewById(R.id.btn_profile)
        val homeButton: ImageButton = findViewById(R.id.btn_home)
        val cancelButton: Button = findViewById(R.id.btn_cancel)
        val createButton: Button = findViewById(R.id.btn_create)
        val name: EditText = findViewById(R.id.name_project)
        val location: EditText = findViewById(R.id.location)
        val variety: EditText = findViewById(R.id.variety)
        val data: EditText = findViewById(R.id.data)

        userButton.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        homeButton.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

        createButton.setOnClickListener{
            lifecycleScope.launch {
                val nameStr = name.text.toString()
                val locationStr = location.text.toString()
                val varietyStr = variety.text.toString()
                val dataStr = data.text.toString()

                val projectId = viewModel.addProject(nameStr, locationStr, varietyStr, dataStr)

                if (projectId != null) {
                    val intent = Intent(this@AddProjectActivity, ProjectActivity::class.java)
                    val bundle = Bundle().apply {
                        putString("name", nameStr)
                        putString("data", dataStr)
                        putString("userId", viewModel.getId())
                        putString("location", locationStr)
                        putString("variety", varietyStr)
                        putString("projectId", projectId)
                    }

                    intent.putExtras(bundle)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@AddProjectActivity,"Can't create the project",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        cancelButton.setOnClickListener{
            val intent = Intent(this@AddProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}