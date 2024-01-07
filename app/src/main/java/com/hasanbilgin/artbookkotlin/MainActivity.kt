package com.hasanbilgin.artbookkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hasanbilgin.artbookkotlin.adapter.ArtAdapter
import com.hasanbilgin.artbookkotlin.databinding.ActivityMainBinding

import com.hasanbilgin.artbookkotlin.model.ArtModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList: ArrayList<ArtModel>
    private lateinit var artAdapter: ArtAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}