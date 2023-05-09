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
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraOfflineSession
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
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
import kotlin.math.*
import com.google.ar.sceneform.rendering.Color as arColor
import com.google.ar.core.Session
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException

class Measurement : AppCompatActivity(), Scene.OnUpdateListener, ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    private val MIN_OPENGL_VERSION = 3.0
    private val TAG: String = Measurement::class.java.getSimpleName()
  //  private val screenshotDetectionDelegate = ScreenshotDetectionDelegate(this, this)

    private var arFragment: ArFragment? = null

    private var distanceModeTextView: TextView? = null


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
    var whichcode : String = "0000"

    private var cameraIntrinsics: CameraIntrinsics? = null


    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009
    }


    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG)
                .show()
        }

        setContentView(R.layout.activity_measurement)
        whichcode = intent.getStringExtra("whichCode").toString()
        val distanceModeArray = resources.getStringArray(R.array.distance_mode)
        distanceModeArray.map{it->
            distanceModeArrayList.add(it)
        }
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        val intent = Intent(this,TemporaryFolder::class.java)

        //val displayMetrics = DisplayMetrics()
        val displayMetrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val displayRotation = windowManager.defaultDisplay.rotation

        /*val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.find { cameraId ->                    //후면 카메라 가져오기
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            lensFacing == CameraCharacteristics.LENS_FACING_BACK
        } ?: throw RuntimeException("Unable to find rear camera.")
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val streamConfigurationMap =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) //주어진 카메라 장치에 대해 지원되는 모든 이미지 크기 및 형식 목록을 얻을 수 있다
        val resolutions = streamConfigurationMap!!.getOutputSizes(ImageFormat.JPEG)
        val maxResolution = resolutions?.maxBy { it.width * it.height } ?: Size(1920, 1080)*/



        if (checkIsSupportedDeviceOrFinish()) {                       //arcore 세션 생성
            try {
                //val arsession = Session(this)
                var sharedSession = Session(this,EnumSet.of(Session.Feature.SHARED_CAMERA ))
                var sharedCamera = sharedSession.sharedCamera
                var cameraId = sharedSession.cameraConfig.cameraId

                //val config = arsession.config
                //config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                //val filter = CameraConfigFilter(arsession)
                //filter.depthSensorUsage = EnumSet.of(CameraConfig.DepthSensorUsage.REQUIRE_AND_USE)
                //val cameraConfigList = arsession.getSupportedCameraConfigs(filter)
                //arsession.cameraConfig = cameraConfigList[0]
                //rCoreConfig.imageSize = Size(screenWidth , screenHeight)
                //arCoreConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                //arCoreConfig.focusMode = Config.FocusMode.AUTO
                //arsession.cameraConfig.imageSize.height
                //arsession.cameraConfig.imageSize.width
                Log.d("후면 카메라 height", sharedCamera.toString())
                Log.d("후면 카메라 width", cameraId.toString())
                //maxResolution.width
                //maxResolution.height
                //arsession?.setCameraConfig(filter)
                //Log.d("max width", arsession.cameraConfig.imageSize.height.toString())
                //Log.d("max height",arsession.cameraConfig.imageSize.width.toString())
            } catch (e: Exception) {
                Log.e("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" , e.toString())
//                Toast.makeText(this, "Failed to create AR session", Toast.LENGTH_SHORT).show()
            }
        }



        initCM = resources.getString(R.string.initCM)

        isStoragePermissionGranted()
        initRenderable()
        createImageFile()
        val distancetext = distancetext.findViewById<TextView>(R.id.distancetext)


        downlodfolder.setOnClickListener {
            startActivity(intent)
        }



        arFragment!!.arSceneView.session?.setDisplayGeometry(displayRotation, screenWidth, screenHeight)




        arFragment!!.arSceneView.planeRenderer.isEnabled = false

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (cubeRenderable == null || distanceCardViewRenderable == null) return@setOnTapArPlaneListener
            // Creating Anchor.

            val arFrame = arFragment?.arSceneView?.arFrame


//            val displayMetrics = DisplayMetrics()
//            windowManager.defaultDisplay.getMetrics(displayMetrics)
//            val screenWidth = displayMetrics.widthPixels
//            val screenHeight = displayMetrics.heightPixels
//            arFragment!!.arSceneView.session?.setDisplayGeometry(displayMetrics..arFragment!!.arSceneView.session?.rotation, screenWidth, screenHeight)
            var distanceMeter : Float = 0.0F
            if (arFrame != null ){

                val camera = arFrame.camera
                var anchor = hitResult.createAnchor()
                var anchorNode = AnchorNode(anchor)


                //Log.d("qweqweqweqwewqe", streamConfigurationMap.toString())
                cameraIntrinsics = camera.imageIntrinsics           //핸드폰 기기 카메라 해상도 가져오기

                distanceMeter = calculateDistance(
                    anchorNode.worldPosition,
                    arFrame!!.camera.pose)


                Log.d("hitresult_distance",distanceMeter.toString())
            }else{
                Log.d(TAG, "ARCore camera not available or not tracking ${arFragment?.arSceneView!!.arFrame}")
            }

            Log.d("지원되는 기기", ArCoreApk.Availability.SUPPORTED_INSTALLED.toString()) // 지원되는 기기인지 확인
            Log.d("핸드폰 화면 해상도 widthPixels",screenWidth.toString())
            Log.d("핸드폰 화면 해상도 heightPixels",screenHeight.toString()) //핸드폰 해상도 지원


            var result : Float =  0.0f

//            Log.d("hitresult_distance" , test?.get(1)?.distance.toString()+"m")

            // 픽셀 대 거리 비율 계산
            if (cameraIntrinsics != null) {
                val screenAspectRatio = arFragment!!.arSceneView.width.toFloat() / arFragment!!.arSceneView.height.toFloat()
                val verticalFov = 2.0f * Math.atan(Math.tan((cameraIntrinsics!!.focalLength[1] / 2.0f).toDouble()) * screenAspectRatio).toFloat()
                val horizontalFov = 2.0f * Math.atan(Math.tan((cameraIntrinsics!!.focalLength[0] / 2.0f).toDouble()) * screenAspectRatio).toFloat()
                //val imageWidth = cameraIntrinsics!!.imageDimensions[0].toFloat()
                val imageWidth = screenWidth.toFloat()
                //val imageHeight = cameraIntrinsics!!.imageDimensions[1].toFloat()
                val imageHeight = screenHeight.toFloat()
                val diagonalAngle = atan(sqrt(imageWidth.toDouble().pow(2.0) + imageHeight.toDouble()
                    .pow(2.0)
                ) * tan(horizontalFov / 2.0) / imageWidth.toDouble())
                val diagonalDistance = distanceMeter * tan(diagonalAngle / 2.0).toFloat() * 2.0f
                val pixelsToDistanceRatio = sqrt(imageWidth.toDouble().pow(2.0) + imageHeight.toDouble()
                    .pow(2.0)
                ).toFloat() / diagonalDistance
                result = abs((1 / pixelsToDistanceRatio) * 100 ).toFloat()            //전체 픽셀을 한 개의 픽셀 값
                Log.d("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk" , result.toString())
                Log.d(TAG, "Pixels-to-distance ratio: $imageWidth pixels/meter")    //장치의 카메라 해상도
                Log.d(TAG, "Pixels-to-distance ratio: $imageHeight pixels/meter")
                Log.d("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRR", (imageWidth / screenWidth.toFloat()).toString() )


            } else {
                Log.d(TAG, "Camera intrinsics not available.")
            }


            distancetext.text = (distanceMeter*100).roundToInt().toString()+"cm"
            //distancetext.text = result.toString()
            //distancetext.text = motionEvent?.x.toString()
            saveButton(whichcode , distanceMeter)
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
    private fun saveButton(code : String , distance : Float){

        val now =
            SimpleDateFormat("yyyyMMdd_hhmmss").format(Date(System.currentTimeMillis()))
        Log.d("media path    " , Environment.getDownloadCacheDirectory().toString())
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"

        val fileName = "${code}_${now}_${(distance*100).roundToInt()}.png"
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
            toast.setGravity(Gravity.CENTER or  Gravity.FILL,0,0)
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
                .getSystemService(ACTIVITY_SERVICE)) as ActivityManager)
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

    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        try {
            when (ArCoreApk.getInstance().checkAvailability(this)) {
                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    Toast.makeText(this, "This device does not support AR", Toast.LENGTH_SHORT).show()
                    finish()
                    return false
                }
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    Toast.makeText(this, "Please install ARCore", Toast.LENGTH_SHORT).show()
                    try {
                        ArCoreApk.getInstance().requestInstall(this, true)
                    } catch (e: UnavailableUserDeclinedInstallationException) {
                        // Display request for user to install ARCore
                        Toast.makeText(this, "Please install ARCore", Toast.LENGTH_SHORT).show()
                        finish()
                        return false
                    }
                    return false
                }
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD, ArCoreApk.Availability.UNKNOWN_ERROR, ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {
                    Toast.makeText(this, "Please update ARCore", Toast.LENGTH_SHORT).show()
                    finish()
                    return false
                }
            }
            return true
        } catch (e: UnavailableArcoreNotInstalledException) {
            Toast.makeText(this, "Please install ARCore", Toast.LENGTH_SHORT).show()
            finish()
            return false
        } catch (e: UnavailableApkTooOldException) {
            Toast.makeText(this, "Please update ARCore", Toast.LENGTH_SHORT).show()
            finish()
            return false
        } catch (e: UnavailableSdkTooOldException) {
            Toast.makeText(this, "Please update this app", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
    }
}
