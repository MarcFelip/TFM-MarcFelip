package com.jetbrains.kmm.androidApp.main

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream

public class MainViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> = _loginStatus

    val alreadyLoggedIn: LiveData<Boolean> = repo.isUserLoggedIn().asLiveData(Dispatchers.Main)

    suspend fun addLabeledImage(image: Bitmap, size: String): Boolean {
        return try {
            val float_size = size.toFloat()
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageBytes = stream.toByteArray()

            repo.addLabeledImage(imageBytes, float_size)

            true
        } catch (e: Exception) {

            false
        }
    }
}