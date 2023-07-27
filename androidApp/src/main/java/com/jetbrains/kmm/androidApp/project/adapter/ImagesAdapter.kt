package com.jetbrains.kmm.androidApp.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.project.Images


class ImagesAdapter(private var imagesList: List<Images>, val listener: ImagesAdapter.onClickListener): RecyclerView.Adapter<ImagesViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ImagesViewHolder(layoutInflater.inflate(R.layout.list_apples, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val item = imagesList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    fun updateData(newImages: List<Images>) {
        imagesList = newImages
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }

    interface onClickListener{
        fun onClick(position: Int)
    }
}
