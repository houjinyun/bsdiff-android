package com.hjy.bsdiff

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hjy.bsdiff.databinding.ActivitySecondBinding

class SecondActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvSecondText.text = "This is second page."
    }

}