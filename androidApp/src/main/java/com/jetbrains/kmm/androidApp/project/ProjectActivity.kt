package com.jetbrains.kmm.androidApp.project

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import kotlinx.coroutines.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.jetbrains.androidApp.BuildConfig
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.MainActivity
import com.jetbrains.kmm.androidApp.profile.ProfileActivity
import com.jetbrains.kmm.androidApp.project.adapter.ImagesAdapter
import com.jetbrains.kmm.shared.Models
import java.io.File
import java.io.IOException
import java.util.*


class ProjectActivity : AppCompatActivity(), ImagesAdapter.onClickListener {

    private lateinit var imagesAdapter: ImagesAdapter
    private lateinit var viewModel: ProjectViewModel

    private lateinit var imageView: ImageView
    private lateinit var project_id: String
    private lateinit var imageBitmap: Bitmap
    private var loadedImages: List<Models.AppleImages> = emptyList()

    private var photoPath: String? = null
    val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_CAMERA_PERMISSION = 100


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

        if (!isOnline(this)) {
            Toast.makeText(this@ProjectActivity, "No internet connection", Toast.LENGTH_SHORT).show()
        }

        imageView = dialog.findViewById(R.id.imageViewApple)

        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        btnSave.setOnClickListener {
            btnSave.isEnabled = false
            lifecycleScope.launch{
                val success = viewModel.addLabeledImage(imageBitmap, textSize.text.toString(), project_id)

                if(success){
                    dialog.dismiss()
                    initRecyclerView()
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

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
            }

            if (photoFile != null) {
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            val originalBitmap = BitmapFactory.decodeFile(photoPath)
            imageBitmap = rotateImageIfRequired(this, originalBitmap, Uri.fromFile(File(photoPath)))

            imageView.setImageBitmap(imageBitmap)
        }
    }

    @Throws(IOException::class)
    fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {

        val input = context.contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface
        ei = if (Build.VERSION.SDK_INT > 23) ExifInterface(input!!)
        else ExifInterface(selectedImage.path!!)

        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }


    fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            takePicture()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun createImageFile(): File? {
        val fileName = "AppleImage"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            fileName,
            ".jpg",
            storageDir
        )
        photoPath = image.absolutePath
        return image
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

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}