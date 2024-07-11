package com.shibuiwilliam.arcoremeasurement

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*


class GridAdapter(
    val context: Context,
    var itemlist: MutableList<SnapshotData>,
    var user: String,
    private val onItemDeleted: (Int) -> Unit // Add this callback
) : BaseAdapter() {

    override fun getCount(): Int {
        return itemlist.size
    }

    fun addItem(item: SnapshotData) {
        itemlist.add(item)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
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
            Glide.with(context)
                .load(item.image)
                .override(1280, 1280)
                .centerCrop()
                .error(R.drawable.ic_close)
                .into(it)
        }


        holder.textview?.let {
            it.text = item.displayName
            it.maxLines = 2  // 최대 2줄로 설정
            it.ellipsize = TextUtils.TruncateAt.END
            it.isSingleLine = false  // 한 줄 제한 해제
        }
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary/" + item.name
        val testPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Fail/" + item.name
        val SuccessPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success/" + item.name
        val file = File(rootPath)

        holder.deleteBtn?.setOnClickListener {

            Log.v(ContentValues.TAG, rootPath)

            val result = file.delete()

            if (result) {
                Log.v(ContentValues.TAG, "delete success")
                itemlist.remove(item)
                notifyDataSetChanged()
                onItemDeleted(itemlist.size) // Call the callback to update total images
                true
            } else {
                Log.v(ContentValues.TAG, "123213123213123213 reject")
                false
            }
        }

        holder.saveBtn?.setOnClickListener {

            if (isInternetConnected(context)) {
                try {
                    getProFileoneImage(rootPath, item, testPath, SuccessPath, user)
                    notifyDataSetChanged()
                    Toast.makeText(context, "서버에 전송했습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Handle the exception, e.g., show an error message
                }
            } else {
                val builder = AlertDialog.Builder(context)
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

        return view
    }

    @SuppressLint("ServiceCast")
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getProFileImage(
        imagePath: String,
        item: MutableList<SnapshotData>,
        movePath: String,
        success: String,
        user: String,
    ) {
        val user = RequestBody.create(MediaType.parse("text/plain"), user)
        val countRequestBody = RequestBody.create(MediaType.parse("text/plain"), item.size.toString())
        val fileList = mutableListOf<File>()
        for (i in item) {
            fileList.add(File(imagePath + "/" + i.name))
        }
        val listPart = mutableListOf<MultipartBody.Part>()
        fileList.forEach {
            listPart.add(
                MultipartBody.Part.createFormData(
                    "images", it.name, RequestBody.create(MediaType.parse("image/png"), it)
                )
            )
        }
        val file = File(imagePath)

        val testfile = File(movePath)
        val successfile = File(success)
        sendImage(listPart, item, imagePath, movePath, success, countRequestBody, user)
    }

    fun getProFileoneImage(
        imagePath: String,
        item: SnapshotData,
        movePath: String,
        success: String,
        user: String,
    ) {

        val file = File(imagePath)
        val testfile = File(movePath)
        val successfile = File(success)
        val user = RequestBody.create(MediaType.parse("text/plain"), user)
        val countRequestBody = RequestBody.create(MediaType.parse("text/plain"), item.toString())
        val requestFile = RequestBody.create(MediaType.parse("image/png"), file)
        val body = MultipartBody.Part.createFormData("images", file.name, requestFile)

        sendOneImage(body, item, file, countRequestBody, user)
    }

    fun sendImage(
        image: List<MultipartBody.Part>,
        item: List<SnapshotData>,
        imagepath: String,
        move: String,
        success: String,
        countRequestBody: RequestBody,
        user: RequestBody
    ) {
        val service = RetrofitSetting.createBaseService(RetrofitPath4::class.java) //레트로핏 통신 설정
        val call = service?.imageSend(image, countRequestBody, user)!! //통신 API 패스 설정
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
        val savePath = File(rootPath, "/")

        call.enqueue(object : Callback<String> {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("로그23132132123213 ", "" + response?.body().toString())
                    for (i in item) {
                        Files.move(
                            File(imagepath + "/" + i.name).toPath(),
                            File(success + "/" + i.name).toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                        )
                        File(imagepath + "/" + i.name).delete()
                    }
                    itemlist.removeAll(item)
                    Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()

                } else {
                    Log.d("로그1354156123 ", "" + response.raw())
                    Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show()
                    for (i in item) {
                        Files.move(
                            File(imagepath + "/" + i.name).toPath(),
                            File(move + "/" + i.name).toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                        )
                        File(imagepath + "/" + i.name).delete()
                    }
                    itemlist.removeAll(item)
                    if (response.code() == 404) {
                        Toast.makeText(context, "위판장을 다시 선택해주세요", Toast.LENGTH_SHORT).show()
                    }
                    if (response.code() == 500) {
                        Toast.makeText(context, "다시 촬영 해주세요", Toast.LENGTH_SHORT).show()
                    }
                    notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("로그 failed", t.message.toString())
            }
        })

    }

    fun sendOneImage(
        image: MultipartBody.Part,
        item: SnapshotData,
        file: File,
        countRequestBody: RequestBody,
        user: RequestBody
    ) {
        val service = RetrofitSetting.createBaseService(RetrofitPath2::class.java) //레트로핏 통신 설정
        val call = service?.imageSend(image, countRequestBody, user)!! //통신 API 패스 설정

        call.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("responseOne ", "" + response?.body().toString())
                    itemlist.remove(item)
                    Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show()
                    file.delete()
                    onItemDeleted(itemlist.size)
                    notifyDataSetChanged()

                } else {
                    Log.d("responeFail ", "" + response.raw())
                    Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("sendFail", t.message.toString())
            }
        })
    }

    private fun makeToast(message: String) {
        var toast: Toast? = null
        toast?.cancel()
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun createuuid() {
        val uuid: UUID = UUID.randomUUID()
        println("Generated UUID: $uuid")
    }
}
