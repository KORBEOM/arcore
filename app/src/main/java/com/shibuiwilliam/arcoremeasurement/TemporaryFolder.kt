package com.shibuiwilliam.arcoremeasurement

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File

lateinit var snapshotAdapter: SnapshotAdapter

class TemporaryFolder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.temporary_folder)

        initRecycler()


    }
    private fun initRecycler() {
        val datas = mutableListOf<SnapshotData>()
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
        val file = File(rootPath)
        var list1 =  mutableListOf<File>()
        list1 = file.listFiles().toMutableList()

        datas.apply {
            for( i in list1  ){
                add(SnapshotData(img = i , name = i.name ))
            }
        }
        snapshotAdapter = SnapshotAdapter(this)
        rv_profile.adapter = snapshotAdapter



        snapshotAdapter.datas = datas
                snapshotAdapter.notifyDataSetChanged()
    }

}