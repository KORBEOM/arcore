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
import kotlinx.android.synthetic.main.item_recyclerview.*
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File
import kotlin.concurrent.thread


open class SuccessFolder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.densityDpi

        setContentView(R.layout.temporary_folder)
        val gridView : GridView = findViewById(R.id.itemrecycle)
        Log.d("popopopopo" , screenWidth.toString())
        val user = intent.getStringExtra("user") ?: "유저 없음"
        gridView.numColumns = if(screenWidth < 450 ) 2 else 1
        gridView.horizontalSpacing = 20
        gridView.verticalSpacing = 50
        val datas = mutableListOf<SnapshotData>()
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success"
        val file = File(rootPath)
        var list1 =  mutableListOf<File>()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        list1 = file.listFiles().toMutableList()
        val allbtn = all_btn.findViewById<Button>(R.id.all_btn)

        datas.apply {
            for( i in list1  ){
                add(createSnapshotData(i))
            }
        }
        val gridAdapter2 : GridAdapter2 = GridAdapter2(this , datas , user)



        allbtn.setOnClickListener {
            for (i in datas) {
                gridAdapter2.getProFileImage(rootPath + "/" + i.name, i )
                gridAdapter2.notifyDataSetChanged()
            }
            if (isInternetConnected(applicationContext))
            {
                try {
                    val total_size = datas.size
                    showProgress(true)
                    thread(start = true) {
                        Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", datas.size.toString())
                        while (datas.size != 0) {
                            send_file.text = datas.size.toString() + "/" + total_size
                        }
                        send_file.text = datas.size.toString() + "/" + total_size
                        runOnUiThread {
                            showProgress(false)
                            send_file.text = ""
                            for (i in datas) {
                                gridAdapter2.getProFileImage(rootPath + "/" + i.name, i)
                                gridAdapter2.notifyDataSetChanged()
                            }
                        }
                    }
                }catch (e: java.lang.Exception){

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


        gridView.adapter = gridAdapter2


        init()

        gridAdapter2.notifyDataSetChanged()



    }



    private fun filedelete()
    {
        val deletebtn = delete_btn.findViewById<Button>(R.id.delete_btn)
        deletebtn.setOnClickListener {
            val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
            val file = File(rootPath)
            file.delete()
        }


    }


    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    private fun init(){
        showProgress(false)
    }

    fun showProgress(isShow:Boolean){
        if (isShow)progressBar.visibility = View.VISIBLE
        else progressBar.visibility =View.GONE
    }

    fun createSnapshotData(file: File): SnapshotData {
        val originalName = file.name
        val dateTime = originalName.substringBefore("_") // 날짜와 시간 부분 추출
        return SnapshotData(
            name = originalName,
            image = file.absolutePath,
            displayName = dateTime,
            server_text = ""
        )
    }
}