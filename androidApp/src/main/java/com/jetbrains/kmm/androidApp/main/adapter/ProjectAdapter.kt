package com.jetbrains.kmm.androidApp.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetbrains.androidApp.R
import com.jetbrains.kmm.androidApp.main.Projects

class ProjectAdapter(private val projectsList: List<Projects>,  val listener : ProjectAdapter.onClickListener): RecyclerView.Adapter<ProjectsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProjectsViewHolder(layoutInflater.inflate(R.layout.list_projects, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ProjectsViewHolder, position: Int) {
        val item = projectsList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int {
        return projectsList.size
    }

    interface onClickListener{
        fun onClick(position: Int)
    }

}
