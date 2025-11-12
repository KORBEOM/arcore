package com.shibuiwilliam.arcoremeasurement

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File



class GridAdapter3(
    val context: Context,
    var itemlist: MutableList<SnapshotData>,
    var user: String,
    var whichCode: String,
    private val onItemCountChanged: (Int) -> Unit  // 카운트 변경 콜백 추가
) : BaseAdapter() {

    override fun getCount(): Int {
        return itemlist.size
    }

    fun addItem(item: SnapshotData) {
        itemlist.add(item)
        notifyDataSetChanged()
        onItemCountChanged(itemlist.size)
    }

    override fun getItem(position: Int): Any {
        return itemlist[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ViewHolder {
        var imageview: ImageView? = null
        var textview: TextView? = null
        var deleteBtn: ImageButton? = null
        var saveBtn: ImageButton? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if(convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_recyclerview, null)
            holder = ViewHolder()
            holder.imageview = view.findViewById(R.id.img_rv_photo)
            holder.textview = view.findViewById(R.id.tv_rv_name)
            holder.deleteBtn = view.findViewById(R.id.delete_btn)
            holder.saveBtn = view.findViewById(R.id.save_btn)
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val item = itemlist[position]

        holder.imageview?.let {
            Glide.with(context).load(item.image).override(850,850).error(R.drawable.haerang).into(it)
        }

        holder.textview?.let {
            it.text = item.displayName
            it.maxLines = 2
            it.ellipsize = TextUtils.TruncateAt.END
            it.isSingleLine = false
        }

        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Fail/" + item.name
        val file = File(rootPath)

        holder.deleteBtn?.setOnClickListener {
            val result = file.delete()
            if (result) {
                itemlist.remove(item)
                notifyDataSetChanged()
                onItemCountChanged(itemlist.size)  // 삭제 시 카운트 업데이트
                true
            } else {
                false
            }
        }

        holder.saveBtn?.setOnClickListener {
            if (isInternetConnected(context)) {
                try {
                    getProFileImage(rootPath, item)
                    notifyDataSetChanged()
                    Toast.makeText(context, "서버에 전송했습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Handle the exception
                }
            } else {
                val builder = AlertDialog.Builder(context)
                builder
                    .setTitle("네트워크 연결 문제")
                    .setMessage("인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->
                        })
                builder.create()
                builder.show()
            }
        }

        return view
    }

    @SuppressLint("ServiceCast")
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getProFileImage(imagePath: String, item: SnapshotData) {
        val file = File(imagePath)
        val user = RequestBody.create(MediaType.parse("text/plain"), user)
        val countRequestBody = RequestBody.create(MediaType.parse("text/plain"), item.toString())
        val requestFile = RequestBody.create(MediaType.parse("image/png"), file)
        val body = MultipartBody.Part.createFormData("images", file.name, requestFile)

        sendImage(body, item, file, countRequestBody, user)
    }

    fun sendImage(image: MultipartBody.Part, item: SnapshotData, file: File, countRequestBody: RequestBody, user: RequestBody) {
        val service = RetrofitSetting.createBaseService(RetrofitPath2::class.java)
        val call = service?.imageSend(image, countRequestBody, user)!!

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    itemlist.remove(item)
                    file.delete()
                    notifyDataSetChanged()
                    onItemCountChanged(itemlist.size)  // 전송 성공 후 카운트 업데이트
                    Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("로그 failed", t.message.toString())
            }
        })
    }
}
