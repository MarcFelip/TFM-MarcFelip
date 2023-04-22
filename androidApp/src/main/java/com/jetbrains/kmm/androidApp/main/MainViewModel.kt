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

    suspend fun loadProjects(): List<Projects> {
        return try {
            val userProjects = repo.getUserProjects()
            userProjects.map { project ->
                Projects(
                    name = project.name ?: "",
                    data = project.data ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error al cargar proyectos: ${e.message}")
            emptyList()
        }

    }


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