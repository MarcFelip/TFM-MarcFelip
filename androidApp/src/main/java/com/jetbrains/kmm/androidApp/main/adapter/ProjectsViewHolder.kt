package com.jetbrains.kmm.androidApp.main.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.Projects

class ProjectsViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val project_name = view.findViewById<TextView>(R.id.project_name)
    val project_data = view.findViewById<TextView>(R.id.project_data)

    fun render(projectModel: Projects){
        project_name.text = projectModel.name
        project_data.text = projectModel.data
    }
}