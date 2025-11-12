package com.shibuiwilliam.arcoremeasurement

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageView: ImageView = findViewById(R.id.fullScreenImageView)

        val imageResId = intent.getIntExtra("imageResId", 0)
        imageView.setImageResource(imageResId)

        imageView.setOnClickListener {
            finish() // Close the activity when the image is clicked
        }
    }
}