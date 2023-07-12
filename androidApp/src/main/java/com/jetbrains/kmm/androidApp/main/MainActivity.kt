package com.jetbrains.kmm.androidApp.main

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.addProject.AddProjectActivity
import com.jetbrains.kmm.androidApp.main.adapter.ProjectAdapter
import com.jetbrains.kmm.androidApp.profile.ProfileActivity
import com.jetbrains.kmm.androidApp.project.ProjectActivity
import com.jetbrains.kmm.shared.Models


class MainActivity : AppCompatActivity(), ProjectAdapter.onClickListener {

    private val CAMERA_REQUEST_CODE = 1
    private lateinit var image_view: ImageView
    private var image: Bitmap? = null
    private lateinit var final_image: Bitmap

    private lateinit var viewModel: MainViewModel
    private lateinit var projectsAdapter: ProjectAdapter
    private var loadedProjects: List<Models.Projects> = emptyList()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user_button: ImageButton = findViewById(R.id.btn_profile)
        val new_project_button: Button = findViewById(R.id.btn_new_project)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        user_button.setOnClickListener{
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        new_project_button.setOnClickListener{
            val intent = Intent(this@MainActivity, AddProjectActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch{
            initRecyclerView()
        }

    }

    private suspend fun initRecyclerView(){

        loadedProjects = viewModel.loadProjects()

        val listProjects = loadedProjects.map { project ->
            Projects(
                name = project.name ?: "",
                data = project.data ?: ""
            )
        }

        val recyclerView = findViewById<RecyclerView>(R.id.projets_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProjectAdapter(listProjects, this@MainActivity)
    }

    override fun onClick(position: Int) {
        val project_show = loadedProjects[position]

        val intent = Intent(this@MainActivity, ProjectActivity::class.java)

        val bundle = Bundle().apply {
            //putString("projectId", project_show._id.toString())
            putString("projectId", project_show.projectId.toString())
            putString("name", project_show.name.toString())
            putString("data", project_show.data.toString())
            putString("userId", project_show.userId.toString())
            putString("location", project_show.location.toString())
            putString("variety", project_show.variety.toString())
        }

        intent.putExtras(bundle)
        startActivity(intent)
    }


}