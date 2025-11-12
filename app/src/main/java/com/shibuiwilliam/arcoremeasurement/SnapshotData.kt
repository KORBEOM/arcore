package com.shibuiwilliam.arcoremeasurement

import android.graphics.Color

data class SnapshotData(
    var name: String,
    var image: String,
    var server_text: String,
    var test_color: Int = Color.GRAY,
    var displayName : String
)
