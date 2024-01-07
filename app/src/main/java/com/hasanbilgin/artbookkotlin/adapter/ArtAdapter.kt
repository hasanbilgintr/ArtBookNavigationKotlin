package com.hasanbilgin.artbookkotlin.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.hasanbilgin.artbookkotlin.ArtActivity

import com.hasanbilgin.artbookkotlin.databinding.ReclerviewRowBinding

import com.hasanbilgin.artbookkotlin.model.ArtModel
import com.hasanbilgin.artbookkotlin.view.ListFragmentDirections

class ArtAdapter(val artList: List<ArtModel>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: ReclerviewRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding =
            ReclerviewRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtHolder(binding)
    }


    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.nameTextview.text = artList[position].artname
        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToArtFragment("old",id=artList[position].id)
            Navigation.findNavController(it).navigate(action)
            //navigation argumentler görmezzse args ve directionslar bulunduğpu klasörü yinele(reload from disk)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }

}