package com.jetbrains.kmm.androidApp.project

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
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
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.max
import kotlin.math.min



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
    var currentFocalLength: Float? = null
    val realFocalLength = 4.53f
    val cropFactor = 7.2f


    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

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

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (_: IOException) {
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

            // Inference on CNN
            val assetManager = assets
            val modelPath = "best_m_float32.tflite"
            val fileDescriptor = assetManager.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val length = fileDescriptor.length
            val modelByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)

            val interpreter = Interpreter(modelByteBuffer)


            // Process image
            val options = BitmapFactory.Options()
            val bitmapFromFile = BitmapFactory.decodeFile(photoPath)
            lifecycleScope.launch {
                val appleSize = processImage(bitmapFromFile, interpreter)
                println("Apple size: $appleSize")
            }

            // interpreter.close()
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
        var image_file = File.createTempFile(
            fileName,
            ".jpg",
            storageDir
        )
        photoPath = image_file.absolutePath
        return image_file
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


    //Resize bitmap
    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    // Convert Bitmap to ByteBuffer
    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val width = bitmap.width
        val height = bitmap.height
        val byteBuffer = ByteBuffer.allocateDirect(4 * width * height * 3) // Asumiendo RGB
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(width * height)

        bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
        var pixel = 0
        for (i in 0 until width) {
            for (j in 0 until height) {
                val value = intValues[pixel++]

                try {
                    byteBuffer.putFloat((value shr 16 and 0xFF) / 255.0f) // Red
                    byteBuffer.putFloat((value shr 8 and 0xFF) / 255.0f)  // Green
                    byteBuffer.putFloat((value and 0xFF) / 255.0f)        // Blue
                } catch (e: Exception) {
                    Log.e("Error", "Error al poner flotante en ByteBuffer", e)
                }
            }
        }
        return byteBuffer
    }

    // Do the inference to the model and post process the results
    private suspend fun processImage(bitmap: Bitmap, model: Interpreter): Double {
        var appleSizeAdjusted:  Double = 0.0
        val focalLength = 26

        withContext(Dispatchers.Default) {
            val resizedBitmap = resizeBitmap(bitmap, 640, 640)
            val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)

            println("Byte Buffer to Inference: $byteBuffer")
            val output = Array(1) { Array(38) { FloatArray(8400) } }
            model.run(byteBuffer, output)

            val confidenceThreshold = 0.8f
            val nmsThreshold = 0.3f

            // FIlter index by confidence
            val confidenceAppleIndices = mutableListOf<Int>()
            val confidenceSupportIndices = mutableListOf<Int>()
            for (i in 0 until 8399) {
                val confidenceApple = output[0][4][i]  // Index 4: "Apple"
                val confidenceSupport = output[0][5][i]  //Index 5: "Support"

                if (confidenceApple > confidenceThreshold) {
                    confidenceAppleIndices.add(i)
                }
                if (confidenceSupport > confidenceThreshold) {
                    confidenceSupportIndices.add(i)
                }
            }

            // Get all the bboxes
            val finalBoxesAndClasses = mutableListOf<Map<String, Any>>()
            for ((indices, className) in listOf(
                Pair(confidenceAppleIndices, "Apple"),
                Pair(confidenceSupportIndices, "Support")
            )) {
                val boxes = mutableListOf<List<Int>>()
                for (index in indices) {
                    val xCenter = output[0][0][index]
                    val yCenter = output[0][1][index]
                    val width = output[0][2][index]
                    val height = output[0][3][index]

                    val x1 = ((xCenter - width / 2) * 640).toInt()
                    val y1 = ((yCenter - height / 2) * 640).toInt()
                    val x2 = ((xCenter + width / 2) * 640).toInt()
                    val y2 = ((yCenter + height / 2) * 640).toInt()
                    boxes.add(listOf(x1, y1, x2, y2))
                }

                val scores = FloatArray(indices.size) {
                    output[0][4][indices[it]]
                }.toList()

                val indicesToKeep = applyNMS(boxes, scores, nmsThreshold)

                for (i in indicesToKeep) {
                    val (x1, y1, x2, y2) = boxes[i]
                    finalBoxesAndClasses.add(
                        mapOf(
                            "box" to listOf(x1, y1, x2, y2),
                            "class" to className,
                            "index" to indicesToKeep[0]
                        )
                    )
                }
            }

            println("finalBoxesAndClasses: $finalBoxesAndClasses")

            // Store bboxes of the apples and supports
            val appleBoxes = finalBoxesAndClasses.filter { it["class"] == "Apple" }.map { it["box"] as List<Int> }
            val supportBoxes = finalBoxesAndClasses.filter { it["class"] == "Support" }.map { it["box"] as List<Int> }

            // Filter bboxes of apples with intersection with the support in case of more than one apple
            val filteredAppleBoxes = if (appleBoxes.size == 1) {
                appleBoxes
            } else {
                appleBoxes.filter { appleBox ->
                    supportBoxes.none { supportBox -> bboxIntersection(appleBox, supportBox) }
                }
            }

            // Diameter in px
            val appleDiameterPx = calculateMaxBBoxDistance(filteredAppleBoxes.firstOrNull())
            val supportDiameterPx = calculateMaxBBoxDistance(supportBoxes.firstOrNull())

            // Calculate final predicted size of the apple
            val appleMm = (appleDiameterPx * 20) / supportDiameterPx
            val distanceDifference = (appleMm / 2) * 0.1
            val zaZsRatio = (distanceDifference + focalLength) / focalLength
            appleSizeAdjusted = appleMm * zaZsRatio

        }

        return appleSizeAdjusted
    }

    // Function to calculate the intersection between two bboxes
    fun bboxIntersection(box1: List<Int>, box2: List<Int>): Boolean {
        val xx1 = max(box1[0], box2[0])
        val yy1 = max(box1[1], box2[1])
        val xx2 = min(box1[2], box2[2])
        val yy2 = min(box1[3], box2[3])

        val w = max(0, xx2 - xx1)
        val h = max(0, yy2 - yy1)

        return w * h > 0
    }


    // Non Maximum Suppression (NMS) function to get the bbox with more conf in case of overlapping
    fun applyNMS(boxes: List<List<Int>>, scores: List<Float>, nmsThreshold: Float): List<Int> {
        val areas = boxes.map { (it[2] - it[0]) * (it[3] - it[1]) }
        val sortedIndices = scores.indices.sortedByDescending { scores[it] }.toMutableList()

        val keep = mutableListOf<Int>()

        while (sortedIndices.isNotEmpty()) {
            val i = sortedIndices.removeAt(0)
            keep.add(i)

            val toDelete = mutableListOf<Int>()

            for (j in sortedIndices) {
                if (bboxIntersection(boxes[i], boxes[j])) {
                    val xx1 = max(boxes[i][0], boxes[j][0])
                    val yy1 = max(boxes[i][1], boxes[j][1])
                    val xx2 = min(boxes[i][2], boxes[j][2])
                    val yy2 = min(boxes[i][3], boxes[j][3])

                    val w = max(0, xx2 - xx1)
                    val h = max(0, yy2 - yy1)
                    val intersectArea = w * h
                    val unionArea = areas[i] + areas[j] - intersectArea

                    if (intersectArea / unionArea.toFloat() > nmsThreshold) {
                        toDelete.add(j)
                    }
                }
            }

            sortedIndices.removeAll(toDelete)
        }

        return keep
    }


    // Calculate highest size distance
    fun calculateMaxBBoxDistance(bbox: List<Int>?): Int {
        if (bbox == null) return 0
        val x1 = bbox[0]
        val y1 = bbox[1]
        val x2 = bbox[2]
        val y2 = bbox[3]

        val horizontalDistance = kotlin.math.abs(x2 - x1)
        val verticalDistance = kotlin.math.abs(y2 - y1)

        return max(horizontalDistance, verticalDistance)
    }

}