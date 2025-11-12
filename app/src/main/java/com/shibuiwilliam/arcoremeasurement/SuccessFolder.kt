// SuccessFolder.kt
package com.shibuiwilliam.arcoremeasurement

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File
import kotlin.concurrent.thread

class SuccessFolder : AppCompatActivity() {
    private lateinit var whichCode: String
    private lateinit var gridAdapter2: GridAdapter2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.densityDpi

        setContentView(R.layout.temporary_folder)

        val gridView: GridView = findViewById(R.id.itemrecycle)
        Log.d("popopopopo", screenWidth.toString())
        val user = intent.getStringExtra("user") ?: "유저 없음"
        gridView.numColumns = if(screenWidth < 450) 2 else 1
        gridView.horizontalSpacing = 20
        gridView.verticalSpacing = 50
        val datas = mutableListOf<SnapshotData>()
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success"
        val file = File(rootPath)
        var list1 = mutableListOf<File>()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected
        whichCode = intent.getStringExtra("whichCode") ?: "0000"
        list1 = file.listFiles().toMutableList()
        val allbtn = all_btn.findViewById<Button>(R.id.all_btn)

        datas.apply {
            for(i in list1) {
                add(createSnapshotData(i))
            }
        }

        imageCount.text = "Total Images: ${datas.size}"

        gridAdapter2 = GridAdapter2(this, datas, user, whichCode) { totalImages ->
            runOnUiThread {
                imageCount.text = "Total Images: $totalImages"
            }
        }

        allbtn.setOnClickListener {
            if (isInternetConnected(applicationContext)) {
                try {
                    val total_size = datas.size
                    if (total_size == 0) {
                        Toast.makeText(applicationContext, "전송할 이미지가 없습니다", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    showProgress(true)
                    thread(start = true) {
                        var remainingImages = datas.size
                        while (remainingImages > 0) {
                            runOnUiThread {
                                send_file.text = "$remainingImages/$total_size"
                                imageCount.text = "Total Images: $remainingImages"
                            }
                            Thread.sleep(100) // Prevent UI freeze
                            remainingImages = datas.size
                        }

                        runOnUiThread {
                            showProgress(false)
                            send_file.text = ""
                            imageCount.text = "Total Images: 0"
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        showProgress(false)
                        Toast.makeText(applicationContext, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                AlertDialog.Builder(this)
                    .setTitle("네트워크 연결 문제")
                    .setMessage("인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인", null)
                    .show()
            }
        }

        gridView.adapter = gridAdapter2
        init()
        gridAdapter2.notifyDataSetChanged()
    }

    private fun init() {
        showProgress(false)
    }

    fun showProgress(isShow: Boolean) {
        if (isShow) progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.GONE
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun createSnapshotData(file: File): SnapshotData {
        val originalName = file.name
        val nameData = originalName.split("_")
        val displayText = if (nameData.size > 3) {
            "${nameData[0]}_${nameData[1]}_${nameData[2]}"  // 날짜, 시간, 위치 정보만 표시
        } else {
            originalName
        }

        return SnapshotData(
            name = originalName,
            image = file.absolutePath,
            displayName = displayText,
            server_text = ""
        )
    }
}
