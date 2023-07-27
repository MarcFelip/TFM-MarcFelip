package com.jetbrains.kmm.androidApp.main

import android.annotation.SuppressLint
import android.app.Dialog
import kotlinx.coroutines.*
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.addProject.AddProjectActivity
import com.jetbrains.kmm.androidApp.login.LoginActivity
import com.jetbrains.kmm.androidApp.main.adapter.ProjectAdapter
import com.jetbrains.kmm.androidApp.profile.ProfileActivity
import com.jetbrains.kmm.androidApp.project.ProjectActivity
import com.jetbrains.kmm.shared.Models


class MainActivity : AppCompatActivity(), ProjectAdapter.onClickListener {

    private lateinit var viewModel: MainViewModel
    private var loadedProjects: List<Models.Projects> = emptyList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userButton: ImageButton = findViewById(R.id.btn_profile)
        val newProjectButton: Button = findViewById(R.id.btn_new_project)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.isLoggedIn.observe(this, Observer { isLoggedIn ->
            if (!isLoggedIn) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        userButton.setOnClickListener{
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        newProjectButton.setOnClickListener{
            showRecalibrateDialog()
        }

        lifecycleScope.launch{
            initRecyclerView()
        }
    }

    //Dialog that asks to calibrate the camera when creating a new project
    private fun showRecalibrateDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_recalibrate)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnCalibrate = dialog.findViewById<Button>(R.id.btn_calibrate)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        btnCalibrate.setOnClickListener {
            val intent = Intent(this@MainActivity, AddProjectActivity::class.java)
            startActivity(intent)
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this@MainActivity, AddProjectActivity::class.java)
            startActivity(intent)
        }
        dialog.show()
    }

    //Init the RecyclerView
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

    //On click element from the RecyclerView
    override fun onClick(position: Int) {
        val projectShow = loadedProjects[position]

        val intent = Intent(this@MainActivity, ProjectActivity::class.java)

        val bundle = Bundle().apply {
            putString("projectId", projectShow.projectId)
            putString("name", projectShow.name.toString())
            putString("data", projectShow.data.toString())
            putString("userId", projectShow.userId.toString())
            putString("location", projectShow.location.toString())
            putString("variety", projectShow.variety.toString())
        }

        intent.putExtras(bundle)
        startActivity(intent)
    }
}