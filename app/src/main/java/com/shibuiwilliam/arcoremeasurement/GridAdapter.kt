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
import kotlinx.android.synthetic.main.temporary_folder.send_file
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

class GridAdapter(
    val context: Context,
    var itemlist: MutableList<SnapshotData>,
    var user: String,
    private val onItemDeleted: (Int) -> Unit
) : BaseAdapter() {

    private lateinit var imageQueue: LinkedList<SnapshotData>

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
                .error(R.drawable.haerang)
                .into(it)
        }

        holder.textview?.let {
            it.text = item.displayName
            it.maxLines = 2
            it.ellipsize = TextUtils.TruncateAt.END
            it.isSingleLine = false
        }

        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary/" + item.name
        val testPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Fail/" + item.name
        val successPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success/" + item.name
        val file = File(rootPath)

        holder.deleteBtn?.setOnClickListener {
            Log.v(ContentValues.TAG, rootPath)
            val result = file.delete()
            if (result) {
                Log.v(ContentValues.TAG, "delete success")
                itemlist.remove(item)
                notifyDataSetChanged()
                onItemDeleted(itemlist.size)
            } else {
                Log.v(ContentValues.TAG, "delete failed")
            }
        }

        holder.saveBtn?.setOnClickListener {
            if (isInternetConnected(context)) {
                try {
                    getProFileoneImage(rootPath, item, testPath, successPath, user)
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("SaveButton", "이미지 전송 실패", e)
                    Toast.makeText(context, "전송 실패", Toast.LENGTH_SHORT).show()
                }
            } else {
                AlertDialog.Builder(context)
                    .setTitle("네트워크 연결 문제")
                    .setMessage("인터넷 연결을 확인해주세요.")
                    .setPositiveButton("확인", null)
                    .show()
            }
        }

        return view
    }

    fun uploadAllImages(images: List<SnapshotData>) {
        if (images.isEmpty()) return

        // 큐 초기화
        imageQueue = LinkedList(images)

        // 첫 이미지 처리 시작
        (context as? TemporaryFolder)?.showProgress(true)
        processNextImage()
    }

    private fun processNextImage() {
        if (imageQueue.isEmpty()) {
            // 모든 이미지 처리 완료
            (context as? TemporaryFolder)?.runOnUiThread {
                (context as? TemporaryFolder)?.apply {
                    showProgress(false)
                    send_file.text = "모든 이미지 처리 완료"
                    send_file.postDelayed({ send_file.visibility = View.GONE }, 3000)
                }
            }
            return
        }

        // 다음 이미지 처리
        val currentImage = imageQueue.poll()
        (context as? TemporaryFolder)?.runOnUiThread {
            (context as? TemporaryFolder)?.apply {
                send_file.visibility = View.VISIBLE
                send_file.text = "이미지 전송 중: ${itemlist.size - imageQueue.size}/${itemlist.size}"
            }
        }

        // 이미지 업로드 실행
        uploadWithRetry(currentImage)
    }

    private fun uploadWithRetry(image: SnapshotData, retryCount: Int = 0, maxRetries: Int = 2) {
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"
        val successPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success"
        val failPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Fail"

        val file = File(rootPath + "/" + image.name)
        val userRequestBody = RequestBody.create(MediaType.parse("text/plain"), user)
        val countRequestBody = RequestBody.create(MediaType.parse("text/plain"), "1")
        val requestFile = RequestBody.create(MediaType.parse("image/png"), file)
        val body = MultipartBody.Part.createFormData("images", image.name, requestFile)

        val service = RetrofitSetting.createBaseService(RetrofitPath2::class.java)
        val call = service?.imageSend(body, countRequestBody, userRequestBody)!!

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("Upload", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                Log.d("Upload", "Response body: ${response.body()}")
                Log.d("Upload", "Response headers: ${response.headers()}")
                
                if (response.isSuccessful && response.code() == 200) {
                    val responseBody = response.body()
                    Log.d("Upload", "Server response: $responseBody")
                    
                    // 서버 응답 검증
                    if (isValidServerResponse(responseBody)) {
                        try {
                            // 성공 시 Success 폴더로 이동
                            moveFileToDirectory(file, successPath, image.name)
                            (context as? TemporaryFolder)?.runOnUiThread {
                                itemlist.remove(image)
                                notifyDataSetChanged()
                                (context as? TemporaryFolder)?.updateTotalImages(itemlist.size)
                                Toast.makeText(context, "${image.name} 전송 성공", Toast.LENGTH_SHORT).show()
                                processNextImage() // 다음 이미지 처리
                            }
                            Log.i("Upload", "Image ${image.name} uploaded successfully")
                        } catch (e: Exception) {
                            Log.e("Upload", "File move operation failed for ${image.name}", e)
                            if (retryCount < maxRetries) {
                                Thread.sleep(1000)
                                uploadWithRetry(image, retryCount + 1, maxRetries)
                            } else {
                                handleFailedUpload(image, failPath, "파일 이동 실패")
                            }
                        }
                    } else {
                        Log.w("Upload", "Invalid server response for ${image.name}: $responseBody")
                        if (retryCount < maxRetries) {
                            Thread.sleep(1000)
                            uploadWithRetry(image, retryCount + 1, maxRetries)
                        } else {
                            handleFailedUpload(image, failPath, "서버 응답 오류")
                        }
                    }
                } else {
                    Log.w("Upload", "Server returned error for ${image.name}. Code: ${response.code()}, Message: ${response.message()}")
                    if (retryCount < maxRetries) {
                        Thread.sleep(1000)
                        uploadWithRetry(image, retryCount + 1, maxRetries)
                    } else {
                        handleFailedUpload(image, failPath, "서버 오류 (${response.code()})")
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("Upload", "Network failure for ${image.name}: ${t.message}", t)
                if (retryCount < maxRetries) {
                    Thread.sleep(1000)
                    uploadWithRetry(image, retryCount + 1, maxRetries)
                } else {
                    handleFailedUpload(image, failPath, "네트워크 오류: ${t.message}")
                }
            }
        })
    }

    private fun isValidServerResponse(response: String?): Boolean {
        // 서버 응답 검증 로직
        // 비어있지 않고 "success", "ok", "200" 등의 성공 표시어가 있는지 확인
        if (response.isNullOrBlank()) {
            return false
        }
        
        val lowerResponse = response.lowercase()
        return lowerResponse.contains("success") || 
               lowerResponse.contains("ok") || 
               lowerResponse.contains("200") ||
               response.trim().isNotEmpty()
    }

    private fun handleFailedUpload(image: SnapshotData, failPath: String, reason: String = "전송 실패") {
        try {
            val sourceFile = File(Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary/" + image.name)
            moveFileToDirectory(sourceFile, failPath, image.name)

            (context as? TemporaryFolder)?.runOnUiThread {
                itemlist.remove(image)
                notifyDataSetChanged()
                (context as? TemporaryFolder)?.updateTotalImages(itemlist.size)
                Toast.makeText(context, "${image.name}: $reason", Toast.LENGTH_SHORT).show()
                processNextImage() // 다음 이미지 처리
            }
        } catch (e: Exception) {
            Log.e("FileMove", "Failed to move failed image ${image.name} to fail folder", e)
            (context as? TemporaryFolder)?.runOnUiThread {
                Toast.makeText(context, "${image.name}: 파일 이동 실패", Toast.LENGTH_SHORT).show()
            }
            processNextImage() // 오류가 발생해도 다음 이미지 처리
        }
    }

    private fun moveFileToDirectory(sourceFile: File, targetDir: String, fileName: String) {
        try {
            File(targetDir).mkdirs() // 대상 디렉토리 생성
            val targetFile = File(targetDir, fileName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.move(
                    sourceFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } else {
                if (!sourceFile.renameTo(targetFile)) {
                    throw IOException("Failed to move file")
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    @SuppressLint("ServiceCast")
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getProFileoneImage(
        imagePath: String,
        item: SnapshotData,
        movePath: String,
        success: String,
        user: String
    ) {
        val file = File(imagePath)
        val user = RequestBody.create(MediaType.parse("text/plain"), user)
        val countRequestBody = RequestBody.create(MediaType.parse("text/plain"), item.toString())
        val requestFile = RequestBody.create(MediaType.parse("image/png"), file)
        val body = MultipartBody.Part.createFormData("images", item.name, requestFile)

        sendOneImage(body, item, file, countRequestBody, user)
    }

    private fun sendOneImage(
        image: MultipartBody.Part,
        item: SnapshotData,
        file: File,
        countRequestBody: RequestBody,
        user: RequestBody
    ) {
        val service = RetrofitSetting.createBaseService(RetrofitPath2::class.java)
        val call = service?.imageSend(image, countRequestBody, user)!!

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("SingleUpload", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                Log.d("SingleUpload", "Response body: ${response.body()}")
                
                if (response.isSuccessful && response.code() == 200) {
                    val responseBody = response.body()
                    
                    if (isValidServerResponse(responseBody)) {
                        try {
                            val successPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Success/"
                            File(successPath).mkdirs()
                            moveFileToDirectory(file, successPath, file.name)

                            (context as? TemporaryFolder)?.runOnUiThread {
                                itemlist.remove(item)
                                notifyDataSetChanged()
                                onItemDeleted(itemlist.size)
                                Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show()
                            }
                            Log.i("SingleUpload", "Single image ${item.name} uploaded successfully")
                        } catch (e: Exception) {
                            Log.e("SingleUpload", "File move failed for ${item.name}", e)
                            handleImageUploadFailure(file, item, -2, "파일 이동 실패")
                        }
                    } else {
                        Log.w("SingleUpload", "Invalid server response for ${item.name}: $responseBody")
                        handleImageUploadFailure(file, item, -3, "서버 응답 오류")
                    }
                } else {
                    Log.w("SingleUpload", "Server error for ${item.name}. Code: ${response.code()}")
                    handleImageUploadFailure(file, item, response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("SingleUpload", "Network failure for ${item.name}: ${t.message}", t)
                handleImageUploadFailure(file, item, -1, "네트워크 오류: ${t.message}")
            }
        })
    }

    private fun handleImageUploadFailure(file: File, item: SnapshotData, errorCode: Int, customMessage: String? = null) {
        try {
            val failPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Fail/"
            File(failPath).mkdirs()
            moveFileToDirectory(file, failPath, file.name)

            (context as? TemporaryFolder)?.runOnUiThread {
                val message = customMessage ?: when (errorCode) {
                    404 -> "위판장을 다시 선택해주세요"
                    500 -> "다시 촬영 해주세요"
                    -1 -> "네트워크 연결 오류"
                    -2 -> "파일 처리 오류"
                    -3 -> "서버 응답 오류"
                    else -> "전송 실패 (코드: $errorCode)"
                }
                
                Toast.makeText(context, "${item.name}: $message", Toast.LENGTH_SHORT).show()
                itemlist.remove(item)
                notifyDataSetChanged()
                onItemDeleted(itemlist.size)
                
                Log.w("ImageUpload", "Upload failed for ${item.name} with code $errorCode: $message")
            }
        } catch (e: Exception) {
            Log.e("FileMove", "실패 파일 이동 실패", e)
        }
    }
}