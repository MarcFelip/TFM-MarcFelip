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

        val add_image_button: ImageButton = findViewById(R.id.add_image)
        val user_button: ImageButton = findViewById(R.id.user_button)

        user_button.setOnClickListener{
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        add_image_button.setOnClickListener{
            showDialog()
        }


    }

    private fun showDialog(){
        val dialog = MaterialDialog(this).noAutoDismiss()
            .customView(R.layout.dialog_add_image)

        val btn_img = dialog.findViewById<Button>(R.id.btn_select_img)
        val btn_save = dialog.findViewById<Button>(R.id.btn_save_img)
        val edit_size = dialog.findViewById<TextInputEditText>(R.id.size)

        image_view =  dialog.findViewById<ImageView>(R.id.imageView)

        btn_img.setOnClickListener{
            take_photo()
        }

        btn_save.setOnClickListener{
            lifecycleScope.launch {
                val addingSuccess = viewModel.addLabeledImage(final_image, edit_size.text.toString())

                if (addingSuccess) {
                    Toast.makeText(this@MainActivity, "!!!!!!!!!!!!!!!!!!!!!!!!!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error Adding the Data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun take_photo(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE){
            image = data?.extras?.get("data") as Bitmap
            if (image != null) {
                final_image = image as Bitmap
                image_view.setImageBitmap(image)
            }else{
                take_photo()
            }
        }
    }
}