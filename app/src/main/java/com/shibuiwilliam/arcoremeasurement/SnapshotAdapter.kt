package com.shibuiwilliam.arcoremeasurement

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class SnapshotAdapter(private val context: Context) : RecyclerView.Adapter<SnapshotAdapter.ViewHolder>() {

    var datas = mutableListOf<SnapshotData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recyclerview,parent,false)
        return ViewHolder(view)


    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position] , position)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val txtName: TextView = view.findViewById(R.id.tv_rv_name)
        private val imgProfile: ImageView = view.findViewById(R.id.img_rv_photo)
        private val delete_btn: Button = view.findViewById(R.id.delete_btn)

        fun bind(item: SnapshotData , itemid : Int) {
            txtName.text = item.name
            Glide.with(itemView).load(item.img).into(imgProfile)
            delete_btn.setOnClickListener {
                val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary/" + item.name
                val file = File(rootPath)
                Log.v(TAG, rootPath)

                val result = file.delete()
                Log.v(TAG, itemid.toString())
                Log.v(TAG , datas.toString())

                if (result
                ) {
                    Log.v(TAG, "123123213123123delete success")
                    datas.remove(item)
                    notifyDataSetChanged()
                    true
                } else {
                    Log.v(TAG, "123213123213123213 reject")

                    false
                }
            }
        }
    }


}