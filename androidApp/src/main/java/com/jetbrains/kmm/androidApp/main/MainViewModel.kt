package com.jetbrains.kmm.androidApp.main

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.jetbrains.kmm.shared.Models
import com.jetbrains.kmm.shared.RealmRepo
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {
    private val repo: RealmRepo = RealmRepo()

    suspend fun loadProjects(): List<Models.Projects> {
        return try {
            repo.getUserProjects()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error al cargar proyectos: ${e.message}")
            emptyList()
        }
    }

    /*suspend fun loadActualProject(userId: String, name: String, data: String): List<Models.Projects> {
        return try {
            repo.getActualProject(userId, name, data)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error al cargar proyectos: ${e.message}")
            emptyList()
        }
    }*/


    suspend fun addLabeledImage(image: Bitmap, size: String): Boolean {
        return try {
            val float_size = size.toFloat()
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageBytes = stream.toByteArray()

            //repo.addMeasuredImages(imageBytes, float_size)

            true
        } catch (e: Exception) {

            false
        }
    }
}