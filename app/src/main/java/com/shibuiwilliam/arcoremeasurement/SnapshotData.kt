package com.shibuiwilliam.arcoremeasurement

import android.graphics.Color
import java.io.File

data class SnapshotData(
    var name: String,
    var image: File,
    var server_text: String ,
    var test_color: Int = Color.GRAY
)
