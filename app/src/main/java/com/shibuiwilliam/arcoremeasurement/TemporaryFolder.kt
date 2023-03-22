package com.shibuiwilliam.arcoremeasurement

import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.item_recyclerview.*
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File

lateinit var snapshotAdapter: SnapshotAdapter

class TemporaryFolder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.temporary_folder)

        val datas = mutableListOf<SnapshotData>()
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
        val file = File(rootPath)
        var list1 =  mutableListOf<File>()
        list1 = file.listFiles().toMutableList()

        datas.apply {
            for( i in list1  ){
                add(SnapshotData(img = i , name = i.name, server_text = i.name ))
            }
        }
        snapshotAdapter = SnapshotAdapter(this)
        itemrecycle.adapter = snapshotAdapter



        snapshotAdapter.datas = datas




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


}