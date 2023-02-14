package com.shibuiwilliam.arcoremeasurement

import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity

class TemporaryFolder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.temporary_folder)

        val gridView = findViewById<GridView>(R.id.gridView)


    }

}