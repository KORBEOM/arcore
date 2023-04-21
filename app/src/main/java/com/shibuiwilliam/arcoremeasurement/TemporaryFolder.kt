package com.shibuiwilliam.arcoremeasurement

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ListAdapter
import android.widget.ViewAnimator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_recyclerview.*
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.max


lateinit var snapshotAdapter: SnapshotAdapter

class TemporaryFolder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.temporary_folder)

        val gridView : GridView = findViewById(R.id.itemrecycle)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected
        val datas = mutableListOf<SnapshotData>()
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
        val file = File(rootPath)
        var list1 =  mutableListOf<File>()
        list1 = file.listFiles().toMutableList()
        val allbtn = all_btn.findViewById<Button>(R.id.all_btn)

        datas.apply {
            for( i in list1  ){
                add(SnapshotData(image = i , name = i.name, server_text = String() ))
            }
        }
        val gridAdapter : GridAdapter = GridAdapter(this , datas)
        allbtn.setOnClickListener {

            for(i in datas){
                gridAdapter.getProFileImage(rootPath + "/" + i.name,i)
                gridAdapter.notifyDataSetChanged()
            }
            val total_size = datas.size
            showProgress(true)
            thread(start = true) {
                Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" , datas.size.toString())
                while(datas.size != 0) {
                    send_file.text = datas.size.toString() + "/" + total_size
                }
                send_file.text = datas.size.toString() + "/" + total_size
                runOnUiThread{
                    showProgress(false)
                    send_file.text = ""
                    for(i in datas){
                        gridAdapter.getProFileImage(rootPath + "/" + i.name,i)
                        gridAdapter.notifyDataSetChanged()
                    }
                }


            }
        }



        gridView.adapter = gridAdapter


        init()

        gridAdapter.notifyDataSetChanged()



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

   private fun init(){
       showProgress(false)
   }

    fun showProgress(isShow:Boolean){
        if (isShow)progressBar.visibility = View.VISIBLE
        else progressBar.visibility =View.GONE
    }


}