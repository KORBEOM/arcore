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
    private lateinit var whichcode: String
    private lateinit var name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.densityDpi
        name = intent.getStringExtra("name") ?: "유저 없음"
        whichcode = intent.getStringExtra("whichCode") ?: "0000"
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

        gridAdapter = GridAdapter(this, datas, name) { totalImages ->
            updateTotalImages(totalImages)
        }

        Log.d("이미지 리스트에 넘어온 이름", name)
        Log.d("이미지 리스트에 넘어온 위판장", whichcode)
        Log.d("user", name)
        allbtn.setOnClickListener {
            if (isInternetConnected(applicationContext)) {
                if (datas.isEmpty()) {
                    Toast.makeText(applicationContext, "전송할 이미지가 없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val datasCopy = ArrayList(datas)
                gridAdapter.uploadAllImages(datasCopy)

            } else {
                AlertDialog.Builder(this)
                    .setTitle("네트워크 연결 문제")
                    .setMessage("인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인", null)
                    .show()
            }
        }

        gridView.adapter = gridAdapter

        init()

        gridAdapter.notifyDataSetChanged()
    }

    fun updateTotalImages(total: Int) {
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
            val datePart = parts[1]  // YYYYMMDD
            val timePart = parts[2].substringBefore(".")  // HHMMSS

            val year = datePart.take(4)
            val month = datePart.substring(4, 6)
            val day = datePart.takeLast(2)
            val hour = timePart.take(2)
            val minute = timePart.substring(2, 4)

            "$year/$month/$day\n$hour:$minute"  // YYYY/MM/DD HH:MM 형식, 줄바꿈 추가
        } else {
            originalName.substringBeforeLast(".")  // 파일 확장자만 제거
        }
        Log.d("이미지 이름 ", originalName)

        return SnapshotData(
            name = originalName,  // 원본 파일 이름 유지
            image = file.absolutePath,
            displayName = displayName,  // 표시용 이름
            server_text = ""
        )
    }
}