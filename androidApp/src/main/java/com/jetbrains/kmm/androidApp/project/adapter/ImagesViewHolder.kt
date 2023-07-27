package com.jetbrains.kmm.androidApp.project.adapter

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.project.Images

class ImagesViewHolder(view: View, val listener : ImagesAdapter.onClickListener): RecyclerView.ViewHolder(view){

    val apple_id = view.findViewById<TextView>(R.id.apple_id)
    val apple_size = view.findViewById<TextView>(R.id.apple_size)
    val image = view.findViewById<ImageView>(R.id.icon_IV)

    init {
        view.setOnClickListener{
            val position = adapterPosition
            listener.onClick(position)
        }
    }

    fun render(ImageModel: Images){
        apple_id.text = ImageModel.id
        apple_size.text = ImageModel.size.toString()

        val bitmap = BitmapFactory.decodeByteArray(ImageModel.apple_image, 0, ImageModel.apple_image.size)
        image.setImageBitmap(bitmap)
    }
}