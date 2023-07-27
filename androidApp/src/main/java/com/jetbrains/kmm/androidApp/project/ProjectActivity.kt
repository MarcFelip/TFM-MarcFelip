package com.jetbrains.kmm.androidApp.project

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import kotlinx.coroutines.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.MainActivity
import com.jetbrains.kmm.androidApp.profile.ProfileActivity
import com.jetbrains.kmm.androidApp.project.adapter.ImagesAdapter
import com.jetbrains.kmm.shared.Models

class ProjectActivity : AppCompatActivity(), ImagesAdapter.onClickListener {

    private lateinit var imagesAdapter: ImagesAdapter
    private lateinit var viewModel: ProjectViewModel

    private lateinit var imageView: ImageView
    private lateinit var project_id: String
    private lateinit var imageBitmap: Bitmap
    private var loadedImages: List<Models.AppleImages> = emptyList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_screen)

        val bundle = intent.extras
        project_id = bundle?.getString("projectId").toString()
        val pName = bundle?.getString("name")
        val pData = bundle?.getString("data")
        val pUserid = bundle?.getString("userId")
        val pLocation = bundle?.getString("location")
        val pVariety = bundle?.getString("variety")


        viewModel = ViewModelProvider(this).get(ProjectViewModel::class.java)

        val userButton: ImageButton = findViewById(R.id.btn_profile)
        val homeButton: ImageButton = findViewById(R.id.btn_home)
        val addImage: Button = findViewById(R.id.btn_add_img)
        val twProjectName: TextView = findViewById(R.id.textView_pname)
        val editProjectButton: ImageButton = findViewById(R.id.btn_edit_project)
        val histogramButton: ImageButton = findViewById(R.id.btn_histogram)

        twProjectName.text = pName

        userButton.setOnClickListener{
            val intent = Intent(this@ProjectActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        homeButton.setOnClickListener{
            val intent = Intent(this@ProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

        addImage.setOnClickListener{
            addImageDialog()
        }

        histogramButton.setOnClickListener{
            //TODO
        }

        editProjectButton.setOnClickListener{
            if (pName != null && pData != null && pLocation != null && pVariety != null) {
                editProjectDialog(project_id, pName, pData, pLocation, pVariety)
            }
        }

        lifecycleScope.launch{
            initRecyclerView()
        }
    }

    //Dialog to add a new image on the project
    private fun addImageDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_image)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnCamera = dialog.findViewById<Button>(R.id.btn_select_img)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save_img)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val textSize = dialog.findViewById<TextInputEditText>(R.id.size)

        imageView = dialog.findViewById(R.id.imageViewApple)

        btnCamera.setOnClickListener {
            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        btnSave.setOnClickListener {
            btnSave.isEnabled = false
            lifecycleScope.launch{
                val success = viewModel.addLabeledImage(imageBitmap, textSize.text.toString(), project_id)

                if(success){
                    dialog.dismiss()
                }
                else {
                    Toast.makeText(this@ProjectActivity, "Error uploading image", Toast.LENGTH_SHORT).show()
                }
            }
            btnSave.isEnabled = true
        }

        btnCancel.setOnClickListener {
            Toast.makeText(this@ProjectActivity, "Add image canceled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // Request to get an image from the camera or gallery
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            imageBitmap = intent?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    //Dialog to delete an image from the project
    @SuppressLint("NotifyDataSetChanged")
    private fun deleteImageDialog(image_id: String, project_id: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_image)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnDelete = dialog.findViewById<Button>(R.id.btn_delete)

        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                viewModel.deleteImage(image_id, project_id)
                dialog.cancel()
                delay(500)
            }
        }

        dialog.show()
    }

    //Dialog to edit project information
    private fun editProjectDialog(project_id: String, p_name: String, p_data: String, p_location: String, p_variety: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_project)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnSave = dialog.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnDelete = dialog.findViewById<ImageButton>(R.id.btn_delete)
        val projectNameDialog = dialog.findViewById<EditText>(R.id.name_project_dialog)
        val locationDialog = dialog.findViewById<EditText>(R.id.location_dialog)
        val varietyDialog = dialog.findViewById<EditText>(R.id.variety_dialog)
        val dataDialog = dialog.findViewById<EditText>(R.id.data_dialog)

        projectNameDialog.hint = p_name
        locationDialog.hint = p_location
        varietyDialog.hint = p_variety
        dataDialog.hint = p_data

        val tw_project_name: TextView = findViewById(R.id.textView_pname)

        btnSave.setOnClickListener {
            lifecycleScope.launch {

                val editSuccess = viewModel.updateProject(projectNameDialog.text.toString(), locationDialog.text.toString(), varietyDialog.text.toString(), dataDialog.text.toString(), project_id)

                if (editSuccess) {
                    // Check if the new project name is not empty, then update the TextView
                    val newProjectName = projectNameDialog.text.toString()
                    if (newProjectName.isNotEmpty()) {
                        tw_project_name.text = newProjectName
                    }

                    dialog.dismiss()

                }else{
                    Toast.makeText(this@ProjectActivity, "Error Editing Project", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        btnDelete.setOnClickListener {
            deleteProjectDialog()
        }
        dialog.show()
    }

    //Dialog to delete the project
    private fun deleteProjectDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_project)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnDelete = dialog.findViewById<Button>(R.id.btn_delete)

        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                viewModel.deleteProject(project_id)
            }
            val intent = Intent(this@ProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }
        dialog.show()
    }

    //Init the RecyclerView
    private suspend fun initRecyclerView() {

        loadedImages = viewModel.loadImages(project_id)

        val listImages = loadedImages.map { image ->
            Images(
                apple_image = (image.appleImage ?: "") as ByteArray,
                size = (image.size ?: "") as Float,
                id = image.imageId ?: ""
            )
        }

        imagesAdapter = ImagesAdapter(listImages, this@ProjectActivity)

        val recyclerView = findViewById<RecyclerView>(R.id.apples_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = imagesAdapter
    }

    //On click element from the RecyclerView
    override fun onClick(position: Int) {
        val image = loadedImages[position]
        val imageId = image.imageId
        val projectId = image.project_id
        if (imageId != null && projectId != null) {
            deleteImageDialog(imageId, projectId)
        }
    }
}