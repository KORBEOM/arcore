package com.shibuiwilliam.arcoremeasurement

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.getSystemService
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File



class GridAdapter(val context: Context, var itemlist : MutableList<SnapshotData>) : BaseAdapter() {



    override fun getCount(): Int {
        return itemlist.size
        TODO("Not yet implemented")
    }

    fun addItem(item :SnapshotData){
        itemlist.add(item)
    }

    override fun getItem(position: Int): Any {
        return itemlist.get(position)
        TODO("Not yet implemented")
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
        TODO("Not yet implemented")
    }

    private class ViewHolder{
        var imageview : ImageView? = null
        var textview : TextView? = null
        var deleteBtn : ImageButton? = null
        var saveBtn : ImageButton? = null
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view : View
        val holder : ViewHolder
        val toast =

        if(convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_recyclerview , null)
            holder = ViewHolder()
            holder.imageview = view.findViewById(R.id.img_rv_photo)
            holder.textview = view.findViewById(R.id.tv_rv_name)
            holder.deleteBtn = view.findViewById(R.id.delete_btn)
            holder.saveBtn = view.findViewById(R.id.save_btn)
            view.tag = holder

        }else{
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val item = itemlist[position]

        holder.imageview?.let {
            Glide.with(context).load(item.image).override(850,850).error(R.drawable.ic_close).into(holder.imageview!!)
            Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" , item.image.toString())
        }

        holder.textview?.let {
            val maxLength = 50

            if (holder.textview!!.length() > maxLength)
            {
                val scaleFactor = maxLength.toFloat()
                holder.textview!!.textSize = holder.textview!!.textSize * scaleFactor
            }
            holder.textview!!.text = item.name

        }

        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary/" + item.name
        val file = File(rootPath)

        holder.deleteBtn?.setOnClickListener {

            Log.v(ContentValues.TAG, rootPath)

            val result = file.delete()

            if (result
            ) {
                Log.v(ContentValues.TAG, "delete success")
                Toast.makeText(context,"삭제되었습니다", Toast.LENGTH_SHORT).show()
                itemlist.remove(item)
                notifyDataSetChanged()
                true
            } else {
                Log.v(ContentValues.TAG, "123213123213123213 reject")

                false
            }
        }
        holder.saveBtn?.setOnClickListener {

            getProFileImage(rootPath,item)
            notifyDataSetChanged()


        }

        return view

        TODO("Not yet implemented")


    }

    fun getProFileImage(imagePath: String,item: SnapshotData ){

        val file = File(imagePath)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        sendImage(body,item,file)

    }
    fun sendImage(image: MultipartBody.Part,item: SnapshotData , file :File) {
        val service = RetrofitSetting.createBaseService(RetrofitPath::class.java) //레트로핏 통신 설정
        val call = service?.imageSend(image)!! //통신 API 패스 설정


        call.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("로그23132132123213 ",""+ response?.body().toString())
                    itemlist.remove(item)
                    Toast.makeText(context,"전송완료", Toast.LENGTH_SHORT).show()
                    file.delete()
                    notifyDataSetChanged()

                }
                else {
                    Log.d("로그1354156123 ",""+ response.raw())
                    Toast.makeText(context,"통신실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("로그 failed",t.message.toString())
            }
        })
    }

}