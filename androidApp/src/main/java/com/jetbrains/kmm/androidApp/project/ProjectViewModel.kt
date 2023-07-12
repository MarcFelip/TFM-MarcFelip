package com.jetbrains.kmm.androidApp.project

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.*
import com.jetbrains.kmm.shared.RealmRepo
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream

public class ProjectViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    private val _addStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> = _addStatus

    val alreadyLoggedIn: LiveData<Boolean> = repo.isUserLoggedIn().asLiveData(Dispatchers.Main)

    suspend fun addLabeledImage(image: Bitmap, size: String, projectId: String): Boolean {
        return try {
            //val outputStream = ByteArrayOutputStream()

//            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            val imageByteArray = outputStream.toByteArray()

            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)


            val float_size = size.toFloat()

            repo.addImage(projectId, float_size, base64String)

            _addStatus.postValue(true)
            true
        } catch (e: Exception) {
            _addStatus.postValue(false)
            false
        }
    }
}