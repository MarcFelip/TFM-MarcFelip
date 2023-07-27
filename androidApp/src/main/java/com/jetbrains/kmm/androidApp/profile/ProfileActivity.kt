package com.jetbrains.kmm.androidApp.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.login.LoginActivity
import com.jetbrains.kmm.androidApp.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel

    private lateinit var dbName: String
    private lateinit var dbEmail: String
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userImg: ImageView = findViewById(R.id.profile_image)
        val userName: TextView = findViewById(R.id.user_name)
        val userEmail: TextView = findViewById(R.id.user_email)
        val editButton: TextView = findViewById(R.id.edit_profile_button)
        val passwordButton: TextView = findViewById(R.id.change_password_button)
        val logoutButton: ImageButton = findViewById(R.id.logout)
        val homeButton: ImageButton = findViewById(R.id.btn_home)

        homeButton.setOnClickListener{
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
            startActivity(intent)
        }

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        viewModel.getProfileData().observe(this) { (userNameDb, userEmailDb, userImageByteArray) ->
            userName.text = userNameDb
            userEmail.text = userEmailDb

            //Convert the received image as ByteArray to Bitmap
            val userBitmap: Bitmap? = if (userImageByteArray != null) {
                BitmapFactory.decodeByteArray(userImageByteArray, 0, userImageByteArray.size)
            } else {
                null
            }

            // Put the userImage in the ImageView
            userImg.setImageBitmap(userBitmap)
            dbName = userNameDb
            dbEmail = userEmailDb
        }

        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.doLogout()
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        editButton.setOnClickListener{
            showProfileDialog()
        }

        passwordButton.setOnClickListener{
            showPasswordDialog()
        }
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

    //Dialog that allows editing the user profile
    private fun showProfileDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_profile)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnSave = dialog.findViewById<Button>(R.id.btn_save_profile)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val nameValue = dialog.findViewById<EditText>(R.id.username)
        val emailValue = dialog.findViewById<EditText>(R.id.email)
        val btnCamera = dialog.findViewById<Button>(R.id.take_photo)

        imageView = dialog.findViewById(R.id.imageViewUser)

        nameValue.hint = dbName
        emailValue.hint = dbEmail

        btnCamera.setOnClickListener {
            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                val editSuccess = viewModel.updateProfile(
                    if (::imageBitmap.isInitialized) imageBitmap else null,
                    nameValue.text.toString(),
                    emailValue.text.toString()
                )

                if (editSuccess) {
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@ProfileActivity, "Error Editing Profile", Toast.LENGTH_SHORT).show()
                }
            }
        }


        btnCancel.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    //Dialog that allows to change the user password
    private fun showPasswordDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_change_password)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnSave = dialog.findViewById<Button>(R.id.btn_save_password)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }
}