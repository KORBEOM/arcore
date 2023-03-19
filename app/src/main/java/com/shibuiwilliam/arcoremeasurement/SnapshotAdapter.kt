package com.shibuiwilliam.arcoremeasurement

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        private val save_btn : Button = view.findViewById(R.id.save_btn)

        fun bind(item: SnapshotData , itemid : Int) {
            txtName.text = item.name
            Glide.with(itemView).load(item.img).into(imgProfile)
            val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary/" + item.name
            val file = File(rootPath)

            delete_btn.setOnClickListener {

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
            save_btn.setOnClickListener {
              getProFileImage(rootPath)

            }
        }
    }

    fun getProFileImage(imagePath: String){

        val file = File(imagePath)
        val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
        val body = MultipartBody.Part.createFormData("img", file.name, requestFile)

        sendImage(body)

    }
    fun sendImage(image: MultipartBody.Part) {
        val service = RetrofitSetting.createBaseService(RetrofitPath::class.java) //레트로핏 통신 설정
        val call = service.imageSend(image)!! //통신 API 패스 설정


        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response?.isSuccessful) {
                    Log.d("로그 ",""+response?.body().toString())
                    Toast.makeText(context,"통신성공", Toast.LENGTH_SHORT).show()

                }
                else {
                    Log.d("로그 ",""+ response )
                    Toast.makeText(context,"통신실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("로그 failed",t.message.toString())
            }
        })
    }


}