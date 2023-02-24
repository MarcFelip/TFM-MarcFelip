package com.jetbrains.kmm.androidApp.profile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputEditText
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    //private val viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user_img: ImageView = findViewById(R.id.profile_image)
        val user_name: TextView = findViewById(R.id.user_name)
        val user_email: TextView = findViewById(R.id.user_email)
        val edit_button: Button = findViewById(R.id.edit_user_data_button)
        val password_button: Button = findViewById(R.id.reset_password_button)
        val logout_button: Button = findViewById(R.id.logout)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        viewModel.getProfileData().observe(this) { (user_name_db, user_email_db) ->
            user_name.text = user_name_db
            user_email.text = user_email_db
        }

        logout_button.setOnClickListener {
            lifecycleScope.launch {
                viewModel.doLogout()
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        edit_button.setOnClickListener{
            showDialog()
        }

    }

    private fun showDialog(){
        val dialog = MaterialDialog(this).noAutoDismiss()
            .customView(R.layout.dialog_edit_profile)

        val btn_save = dialog.findViewById<Button>(R.id.btn_save_profile)
        val name_value = dialog.findViewById<TextInputEditText>(R.id.name)
        val email_value = dialog.findViewById<TextInputEditText>(R.id.email)

        btn_save.setOnClickListener{
            lifecycleScope.launch {

                val editSuccess = viewModel.updateProfile(name_value.text.toString(), email_value.text.toString())

                if (editSuccess) {
                    dialog.dismiss()
                }else{
                    Toast.makeText(this@ProfileActivity, "Error Editing Profile", Toast.LENGTH_SHORT).show()

                }
            }
        }

        dialog.show()
    }


    /*
    @Composable
    fun get_data (): Pair <String, String> {
        val profileVM: ProfileViewModel = viewModel()

        val name: String
        val email: String
        profileVM.userInfo.observeAsState().apply {
            this.value?.let {
                name = it.name
                email = it.email
            }
        }
        return Pair(name, email)
    }
    */

}