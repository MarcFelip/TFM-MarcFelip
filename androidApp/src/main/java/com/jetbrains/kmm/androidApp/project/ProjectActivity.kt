package com.jetbrains.kmm.androidApp.project

import android.annotation.SuppressLint
import android.app.Activity
import kotlinx.coroutines.*
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.textfield.TextInputEditText
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.MainActivity
import com.jetbrains.kmm.androidApp.main.MainViewModel
import com.jetbrains.kmm.androidApp.profile.ProfileActivity

class ProjectActivity : AppCompatActivity() {

    private lateinit var viewModel: ProjectViewModel

    private lateinit var imageView: ImageView
    private lateinit var project_id: String
    private lateinit var imageBitmap: Bitmap

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_screen)

        val bundle = intent.extras
        project_id = bundle?.getString("projectId").toString()
        val p_name = bundle?.getString("name")
        val p_data = bundle?.getString("data")
        val p_userId = bundle?.getString("userId")
        val p_location = bundle?.getString("location")
        val p_variety = bundle?.getString("variety")


        viewModel = ViewModelProvider(this).get(ProjectViewModel::class.java)

        val user_button: ImageButton = findViewById(R.id.btn_profile)
        val home_button: ImageButton = findViewById(R.id.btn_home)
        val add_image: Button = findViewById(R.id.btn_add_img)
        var tw_project_name: TextView = findViewById(R.id.textView_pname)

        tw_project_name.text = p_name

        user_button.setOnClickListener{
            val intent = Intent(this@ProjectActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        home_button.setOnClickListener{
            val intent = Intent(this@ProjectActivity, MainActivity::class.java)
            startActivity(intent)
        }

        add_image.setOnClickListener{

            showImageDialog()

        }

    }


    private fun showImageDialog(){
        val dialog = MaterialDialog(this).noAutoDismiss()
            .customView(R.layout.dialog_add_image)

        val btn_camera = dialog.findViewById<Button>(R.id.btn_select_img)
        val btn_save = dialog.findViewById<Button>(R.id.btn_save_img)
        val btn_cancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val text_size = dialog.findViewById<TextInputEditText>(R.id.size)

        val dialogView = dialog.getCustomView()
        imageView = dialogView.findViewById(R.id.imageViewApple)

        btn_camera.setOnClickListener {
            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        btn_save.setOnClickListener {
            btn_save.isEnabled = false
            lifecycleScope.launch{
                val success = viewModel.addLabeledImage(imageBitmap, text_size.text.toString(), project_id)

                if(success){
                    dialog.dismiss()
                }
                else {
                    Toast.makeText(this@ProjectActivity, "Error uploading image", Toast.LENGTH_SHORT).show()
                }
            }
            btn_save.isEnabled = true
        }

        btn_cancel.setOnClickListener {
            Toast.makeText(this@ProjectActivity, "Add image canceled", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            imageBitmap = intent?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

}