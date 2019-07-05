package com.grio.bugsnap

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.i_animal.view.*


class AnimalsAdapter(val items: MutableList<Drawable> = mutableListOf<Drawable>()) : RecyclerView.Adapter<AnimalsAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.i_animal, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindForecast(items[position])
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindForecast(img: Drawable) {
            itemView.img.setImageDrawable(img)
        }


    }
}