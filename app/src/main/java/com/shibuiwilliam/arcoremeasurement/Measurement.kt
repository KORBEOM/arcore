package com.shibuiwilliam.arcoremeasurement

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.shibuiwilliam.arcoremeasurement.Measurement.Screenshot.takeScreenshotOfRootView
import kotlinx.android.synthetic.main.activity_measurement.*
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.temporary_folder.*
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.google.ar.sceneform.rendering.Color as arColor


class Measurement : AppCompatActivity(), Scene.OnUpdateListener, ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    private val MIN_OPENGL_VERSION = 3.0
    private val TAG: String = Measurement::class.java.getSimpleName()
  //  private val screenshotDetectionDelegate = ScreenshotDetectionDelegate(this, this)

    private var arFragment: ArFragment? = null

    private var distanceModeTextView: TextView? = null
    private lateinit var pointTextView: TextView

    private lateinit var arrow1UpLinearLayout: LinearLayout
    private lateinit var arrow1DownLinearLayout: LinearLayout
    private lateinit var arrow1UpView: ImageView
    private lateinit var arrow1DownView: ImageView
    private lateinit var arrow1UpRenderable: Renderable
    private lateinit var arrow1DownRenderable: Renderable

    private lateinit var arrow10UpLinearLayout: LinearLayout
    private lateinit var arrow10DownLinearLayout: LinearLayout
    private lateinit var arrow10UpView: ImageView
    private lateinit var arrow10DownView: ImageView
    private lateinit var arrow10UpRenderable: Renderable
    private lateinit var arrow10DownRenderable: Renderable

    private lateinit var multipleDistanceTableLayout: TableLayout

    private var cubeRenderable: ModelRenderable? = null
    private var distanceCardViewRenderable: ViewRenderable? = null

    private lateinit var distanceModeSpinner: Spinner
    private val distanceModeArrayList = ArrayList<String>()
    private var distanceMode: String = ""

    private val placedAnchors = ArrayList<Anchor>()
    private val placedAnchorNodes = ArrayList<AnchorNode>()
    private val midAnchors: MutableMap<String, Anchor> = mutableMapOf()
    private val midAnchorNodes: MutableMap<String, AnchorNode> = mutableMapOf()
    private val fromGroundNodes = ArrayList<List<Node>>()

    private val multipleDistances = Array(Constants.maxNumMultiplePoints,
        {Array<TextView?>(Constants.maxNumMultiplePoints){null} })
    private lateinit var initCM: String

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG)
                .show()
        }

        setContentView(R.layout.activity_measurement)
        val distanceModeArray = resources.getStringArray(R.array.distance_mode)
        distanceModeArray.map{it->
            distanceModeArrayList.add(it)
        }
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?

        val intent = Intent(this,TemporaryFolder::class.java)
        downlodfolder.setOnClickListener {
            startActivity(intent)
        }
        initCM = resources.getString(R.string.initCM)


        initRenderable()
        isStoragePermissionGranted()
        createImageFile()
        val distancetext = distancetext.findViewById<TextView>(R.id.distancetext)



        arFragment!!.arSceneView.planeRenderer.isEnabled = false

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (cubeRenderable == null || distanceCardViewRenderable == null) return@setOnTapArPlaneListener
            // Creating Anchor.

            Log.d("aaaaaaaaaaaaaaaaaaaaaaaa" , hitResult.distance.toString())

            distancetext.text = (hitResult.distance * 100).roundToInt().toString()+"cm"

            saveButton()



        }


    }

    private fun initRenderable() {
        MaterialFactory.makeTransparentWithColor(
            this,
            arColor(Color.RED))
            .thenAccept { material: Material? ->
                cubeRenderable = ShapeFactory.makeSphere(
                    0.02f,
                    Vector3.zero(),
                    material)
                cubeRenderable!!.setShadowCaster(false)
                cubeRenderable!!.setShadowReceiver(false)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

        ViewRenderable
            .builder()
            .setView(this, R.layout.distance_text_layout)
            .build()
            .thenAccept{
                distanceCardViewRenderable = it
                distanceCardViewRenderable!!.isShadowCaster = false
                distanceCardViewRenderable!!.isShadowReceiver = false
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

    }

    private fun setMode(){
        distanceModeTextView!!.text = distanceMode
    }
    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted")
                true
            } else {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            true
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File? { // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        var imageFile: File?
        val storageDir = File(
            Environment.getExternalStorageDirectory().toString() + "/DCIM",
            "Temporary"
        )
        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString())
            storageDir.mkdirs()
        }
        imageFile = File(storageDir, imageFileName)
        imageFile.absolutePath
        return imageFile
    }
    private fun saveButton(){

        val now =
            SimpleDateFormat("yyyyMMdd_hhmmss").format(Date(System.currentTimeMillis()))
        Log.d("media path    " , Environment.getDownloadCacheDirectory().toString())
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"

        val fileName = "${now}.png"
        val savePath = File(rootPath,"/")
        savePath.mkdirs()
        savePath.absoluteFile

        Log.d("save mkdir   ", savePath.mkdirs().toString())
        Log.d("save Path   ",savePath.absolutePath)
        Log.d("save file   ", savePath.absoluteFile.toString())

        val file = File(savePath, fileName)
        if (file.exists()) file.delete()
        val view1 = findViewById<View>(R.id.total_view)
        //val view1 = arFragment!!.view
        Log.d("arFragment~~~~ : " , view1.width.toString())
        Log.d("arFragment~~~~ : " , view1.height.toString())
        clearAllAnchors()
        //onScreenCaptured()
        val bitmap = takeScreenshotOfRootView(view1)

        val locationOfViewInWindow = IntArray(2)
        val xCoordinate = locationOfViewInWindow[0]
        val yCoordinate = locationOfViewInWindow[1]
        val scope = Rect(
            xCoordinate,
            yCoordinate,
            xCoordinate + view1.width,
            yCoordinate + view1.height
        )

        savePath.absolutePath
        arFragment?.let {
            PixelCopy.request(it.arSceneView, bitmap, PixelCopy.OnPixelCopyFinishedListener {100
                if (it != PixelCopy.SUCCESS) {
                    /// Fallback when request fails...
                    return@OnPixelCopyFinishedListener
                }
                Log.d(TAG, "svBitmap w : ${bitmap.width} , h : ${bitmap.height}")

                /// Handle retrieved Bitmap...
                Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + view1.width,
                    locationOfViewInWindow[1] + view1.height
                )
            }, view1.handler)
        }

        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            val inflater = layoutInflater
// Custom 레이아웃 Imflatation '인플레이션', 레이아웃 메모리에 객체화
// Custom 레이아웃 Imflatation '인플레이션', 레이아웃 메모리에 객체화
            val layout: View = inflater.inflate(
                R.layout.custom_toast,
                findViewById<View>(R.id.custom_toast_layout) as ViewGroup?
            )
// 보여줄 메시지 설정 위해 TextView 객체 연결, 인플레이션해서 생성된 View를 통해 findViewById 실행
// 보여줄 메시지 설정 위해 TextView 객체 연결, 인플레이션해서 생성된 View를 통해 findViewById 실행
            val message: TextView = layout.findViewById(R.id.custom_toast_message)
            message.text = "Save Image"
// 보여줄 이미지 설정 위해 ImageView 연결
// 보여줄 이미지 설정 위해 ImageView 연결
//            val image: ImageView = layout.findViewById(R.id.custom_toast_image)
//            image.setBackgroundResource(R.drawable.gallery)

            //val a: Bitmap? = Bitmap.createBitmap(view1.width, view1.height, Bitmap.Config.ARGB_8888)
            val image = layout.findViewById<ImageView>(R.id.custom_toast_image)
            image.setImageBitmap(bitmap)


// Toast 객체 생성

// Toast 객체 생성
            val toast = Toast(this)
// 위치설정, Gravity - 기준지정(상단,왼쪽 기준 0,0) / xOffset, yOffset - Gravity기준으로 위치 설정
// 위치설정, Gravity - 기준지정(상단,왼쪽 기준 0,0) / xOffset, yOffset - Gravity기준으로 위치 설정
            toast.setGravity(Gravity.CENTER or  Gravity.CENTER_HORIZONTAL,0,0)
// Toast 보여줄 시간 'Toast.LENGTH_SHORT 짧게'
// Toast 보여줄 시간 'Toast.LENGTH_SHORT 짧게'
            toast.duration = Toast.LENGTH_SHORT
// CustomLayout 객체 연결
// CustomLayout 객체 연결
            toast.view = layout
// Toast 보여주기
// Toast 보여주기
            toast.show()
            Log.d("ddddddddddd",toast.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }





        /* val bitmap = Bitmap.createBitmap(
             view2.width,
             view2.height,
             Bitmap.Config.ARGB_8888
         )
         var canvas = Canvas(bitmap)
         view2.draw(canvas)
         if(bitmap == null) {
             return null!!
         }else {

             val imageFile = File(mPath)
             Log.d("bitmap~~~~" , imageFile.toString())
             val outputStream = FileOutputStream(imageFile)
             outputStream.use {
                 bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                 outputStream.flush()
             }
             outputStream.close()
         }*/

    }
     object Screenshot {
        private fun takeScreenshot(view: View): Bitmap {

            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.isDrawingCacheEnabled = false
            var canvas = Canvas(b)
            view.draw(canvas)
            return b

        }

        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v.rootView)

        }
    }



    private fun clearAllAnchors(){
        placedAnchors.clear()
        for (anchorNode in placedAnchorNodes){
            arFragment!!.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)
        }
        placedAnchorNodes.clear()
        midAnchors.clear()
        for ((k,anchorNode) in midAnchorNodes){
            arFragment!!.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)
        }
        midAnchorNodes.clear()
        for (i in 0 until Constants.maxNumMultiplePoints){
            for (j in 0 until Constants.maxNumMultiplePoints){
                if (multipleDistances[i][j] != null){
                    multipleDistances[i][j]!!.setText(if(i==j) "-" else initCM)
                }
            }
        }
        fromGroundNodes.clear()

    }


    private fun placeAnchor(hitResult: HitResult,
                            renderable: Renderable){
        val anchor = hitResult.createAnchor()
        placedAnchors.add(anchor)

        val anchorNode = AnchorNode(anchor).apply {
            isSmoothed = true
            setParent(arFragment!!.arSceneView.scene)
        }
        placedAnchorNodes.add(anchorNode)

        val node = TransformableNode(arFragment!!.transformationSystem)
            .apply{
                this.rotationController.isEnabled = false
                this.scaleController.isEnabled = false
                this.translationController.isEnabled = true
                this.renderable = renderable
                setParent(anchorNode)
            }

        arFragment!!.arSceneView.scene.addOnUpdateListener(this)
        arFragment!!.arSceneView.scene.addChild(anchorNode)
        node.select()
    }



    @SuppressLint("SetTextI18n")
    override fun onUpdate(frameTime: FrameTime) {
        when(distanceMode) {
            distanceModeArrayList[0] -> {
                measureDistanceFromCamera()
            }
            else -> {
                measureDistanceFromCamera()
            }
        }
    }


    private fun measureDistanceFromCamera(){
        val frame = arFragment!!.arSceneView.arFrame
        if (placedAnchorNodes.size >= 1) {
            val distanceMeter = calculateDistance(
                placedAnchorNodes[0].worldPosition,
                frame!!.camera.pose)
            measureDistanceOf2Points(distanceMeter)
        }
    }


    private fun measureDistanceOf2Points(distanceMeter: Float){
        val distanceTextCM = makeDistanceTextWithCM(distanceMeter)
        val textView = (distanceCardViewRenderable!!.view as LinearLayout)
            .findViewById<TextView>(R.id.distanceCard)
        textView.text = distanceTextCM
        Log.d(TAG, "distance: ${distanceTextCM}")
    }

    private fun makeDistanceTextWithCM(distanceMeter: Float): String{
        val distanceCM = changeUnit(distanceMeter, "cm")
        val distanceCMFloor = "%.2f".format(distanceCM)
        return "${distanceCMFloor} cm"
    }

    private fun calculateDistance(x: Float, y: Float, z: Float): Float{
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }


    private fun calculateDistance(objectPose0: Vector3, objectPose1: Pose): Float{
        return calculateDistance(
            objectPose0.x - objectPose1.tx(),
            objectPose0.y - objectPose1.ty(),
            objectPose0.z - objectPose1.tz()
        )
    }



    private fun changeUnit(distanceMeter: Float, unit: String): Float{
        return when(unit){
            "cm" -> distanceMeter * 100
            "mm" -> distanceMeter * 1000
            else -> distanceMeter
        }
    }


    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        val openGlVersionString =
            (Objects.requireNonNull(activity
                .getSystemService(Context.ACTIVITY_SERVICE)) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES ${MIN_OPENGL_VERSION} later")
            Toast.makeText(activity,
                "Sceneform requires OpenGL ES ${MIN_OPENGL_VERSION} or later",
                Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    override fun onScreenCaptured(path: String) {
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        // Do something when screen was captured
    }

    override fun onScreenCapturedWithDeniedPermission() {
        Toast.makeText(this, "Please grant read external storage permission for screenshot detection", Toast.LENGTH_SHORT).show()
        // Do something when screen was captured but read external storage permission has denied
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.getOrNull(0) == PackageManager.PERMISSION_DENIED) {
                    showReadExternalStoragePermissionDeniedMessage()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestReadExternalStoragePermission()
        }
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION)
    }

    private fun showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT).show()
    }
}