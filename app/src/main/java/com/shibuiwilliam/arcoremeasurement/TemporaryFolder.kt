package com.shibuiwilliam.arcoremeasurement

import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.item_recyclerview.*
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

lateinit var snapshotAdapter: SnapshotAdapter

open class TemporaryFolder : AppCompatActivity() {

    private lateinit var totalImagesTextView: TextView
    private lateinit var gridAdapter: GridAdapter
    private val datas = mutableListOf<SnapshotData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.densityDpi
        val user = intent.getStringExtra("user") ?: "유저 없음"
        setContentView(R.layout.temporary_folder)
        val gridView: GridView = findViewById(R.id.itemrecycle)
        totalImagesTextView = findViewById(R.id.imageCount)

        Log.d("popopopopo", screenWidth.toString())
        gridView.numColumns = 2
        gridView.horizontalSpacing = 30
        gridView.verticalSpacing = 30
        gridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH

        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
        val testPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Fail/"
        val successPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success/"
        val file = File(rootPath)
        var list1 = mutableListOf<File>()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        list1 = file.listFiles()?.toMutableList() ?: mutableListOf()
        val allbtn = findViewById<Button>(R.id.all_btn)

        datas.apply {
            for (i in list1) {
                add(createSnapshotData(i))
            }
        }

        updateTotalImages(datas.size)

        gridAdapter = GridAdapter(this, datas, user) { totalImages ->
            updateTotalImages(totalImages)
        }

        Log.d("user", user)
        allbtn.setOnClickListener {
            gridAdapter.getProFileImage(rootPath, datas, testPath, successPath, user)
            gridAdapter.notifyDataSetChanged()
            updateTotalImages(datas.size)

            if (isInternetConnected(applicationContext)) {
                try {
                    val total_size = datas.size
                    showProgress(true)
                    thread(start = true) {
                        Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", datas.size.toString())
                        while (datas.size != 0) {
                            runOnUiThread { updateTotalImages(datas.size) }
                        }
                        Log.d("gimoring ", "aaaaaaaaaaaaaaaaaaaaaaaaddddddddddd")
                        runOnUiThread {
                            showProgress(false)
                            send_file.text = ""
                        }
                    }
                } catch (e: java.lang.Exception) {
                }
            } else {
                val builder = AlertDialog.Builder(this)
                builder
                    .setTitle("네트워크 연결 문제")
                    .setMessage("인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->
                            // Start 버튼 선택시 수행
                        })
                builder.create()
                builder.show()
            }
        }

        gridView.adapter = gridAdapter

        init()

        gridAdapter.notifyDataSetChanged()
    }

    private fun updateTotalImages(total: Int) {
        totalImagesTextView.text = "Total Images: $total"
    }

    @SuppressLint("WrongViewCast")
    private fun filedelete() {
        val deletebtn = findViewById<Button>(R.id.delete_btn)
        deletebtn.setOnClickListener {
            val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
            val file = File(rootPath)
            if (file.exists()) {
                file.delete()
                datas.clear()
                gridAdapter.notifyDataSetChanged()
                updateTotalImages(datas.size)
            }
        }
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun init() {
        showProgress(false)
    }

    fun showProgress(isShow: Boolean) {
        if (isShow) progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.GONE
    }
    fun createSnapshotData(file: File): SnapshotData {
        val originalName = file.name
        val parts = originalName.split("_")
        val displayName = if (parts.size >= 3) {
            val datePart = parts[1].takeLast(4)  // YYYYMMDD에서 MMDD만 가져옴
            val timePart = parts[2].substringBefore(".").take(4)  // HHMMSS에서 HHMM만 가져옴
            "$datePart$timePart"  // MMDDHHMM 형식
        } else {
            originalName.substringBeforeLast(".")  // 파일 확장자만 제거
        }

        return SnapshotData(
            name = originalName,
            image = file.absolutePath,
            displayName = displayName,
            server_text = ""
        )
    }
}

