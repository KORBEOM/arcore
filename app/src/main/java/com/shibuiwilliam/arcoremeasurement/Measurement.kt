package com.shibuiwilliam.arcoremeasurement

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akexorcist.screenshotdetection.ScreenshotDetectionDelegate
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.TransformableNode
import com.shibuiwilliam.arcoremeasurement.Measurement.Screenshot.takeScreenshotOfRootView
import kotlinx.android.synthetic.main.activity_measurement.*
import kotlinx.android.synthetic.main.activity_measurement.view.*
import kotlinx.android.synthetic.main.custom_toast.*
import kotlinx.android.synthetic.main.temporary_folder.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import com.google.ar.sceneform.rendering.Color as arColor

open class Measurement : AppCompatActivity(), Scene.OnUpdateListener,
    ScreenshotDetectionDelegate.ScreenshotDetectionListener {
    private val MIN_OPENGL_VERSION = 3.0
    private val TAG: String = Measurement::class.java.getSimpleName()
    private var arFragment: CustomArFragment? = null

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
    val soundPool = SoundPool.Builder().build()
    var soundId : Int = 0
    private val multipleDistances = Array(Constants.maxNumMultiplePoints,
        { Array<TextView?>(Constants.maxNumMultiplePoints) { null } })
    private lateinit var initCM: String
    var whichcode: String = "0000"
    private var cameraIntrinsics: CameraIntrinsics? = null
    private var cameraconfig : CameraConfig? = null

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 3009
    }


    @SuppressLint("ServiceCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG)
                .show()
        }

        setContentView(R.layout.activity_measurement)
        whichcode = intent.getStringExtra("whichCode").toString()
        val distanceModeArray = resources.getStringArray(R.array.distance_mode)
        distanceModeArray.map { it ->
            distanceModeArrayList.add(it)
        }
        val view1 = findViewById<View>(R.id.total_view)
        arFragment =
            supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as CustomArFragment?
        //arFragment =(CustomArFragment) supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment?

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        val intent = Intent(this, TemporaryFolder::class.java)
        val intent2 = Intent(this,SuccessFolder::class.java)
        val intent3 = Intent(this,FailFolder::class.java)


        //val displayMetrics = DisplayMetrics()
        val displayMetrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        //displayMetrics.densityDpi =DisplayMetrics.DENSITY_HIGH

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val displayRotation = windowManager.defaultDisplay.rotation

        back_btn.setOnClickListener {
            onBackPressed()
        }


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

        fun onBackPressed() {
            super.onBackPressed()
            finish()
        }

//        if (checkIsSupportedDeviceOrFinish()) {                       //arcore 세션 생성
//            try {
//
//                var arsession = Session(this)
//                var sharedSession = Session(this,EnumSet.of(Session.Feature.SHARED_CAMERA ))
//
//
//               var sharedCamera = sharedSession.sharedCamera
//
//               var cameraId = sharedSession.cameraConfig.cameraId
//
//                //configureARCoreCamera(arsession)
//                val filter = CameraConfigFilter(sharedSession)
//                //filter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_60)
//                //filter.facingDirection = CameraConfig.FacingDirection.FRONT
//                val config = sharedSession.config
//                val displayMetrics = DisplayMetrics()
//                windowManager.defaultDisplay.getMetrics(displayMetrics)
//                Log.d("초점1", config.focusMode.toString())
//                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
//                config.focusMode = Config.FocusMode.AUTO
//                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
//                config.imageStabilizationMode = Config.ImageStabilizationMode.EIS
//                config.streetscapeGeometryMode = Config.StreetscapeGeometryMode.ENABLED
//                config.geospatialMode = Config.GeospatialMode.ENABLED
//                //arsession.cameraConfig.imageSize
//
//                sharedSession.cameraConfig.fpsRange.extend(60,60)
//                config.semanticMode = Config.SemanticMode.ENABLED
//
//
//                //filter.depthSensorUsage = EnumSet.of(DepthSensorUsage.REQUIRE_AND_USE)
//                //val cameraConfigList = arsession.getSupportedCameraConfigs(filter)
//                //arsession.cameraConfig = cameraConfigList[0]
//                //config.imageSize = Size(screenWidth , screenHeight)
//                //arCoreConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
//
//                //arsession.cameraConfig.imageSize.height
//                //arsession.cameraConfig.imageSize.width
//                Log.d("session",cameraId.toString())
//                Log.d("imasize", sharedSession.cameraConfig.fpsRange.toString())
//                Log.d("imasize", sharedSession.cameraConfig.imageSize.toString())
//                //Log.d("surface", sharedCamera.arCoreSurfaces.size.toString())
//
//                Log.d("초점", config.focusMode.toString())
//                Log.d("조명", config.lightEstimationMode.toString())
//                Log.d("핸드폰 화면 해상도 widthPixels",screenWidth.toString())
//                Log.d("핸드폰 화면 해상도 heightPixels",screenHeight.toString())
//                sharedSession.update()
//                sharedSession.close()
//                //arFragment!!.arSceneView.setupSession(sharedSession)
//                //Log.d("surface " , arFragment!!.arSceneView.session?.sharedCamera?.arCoreSurfaces.toString())
//                //maxResolution.width
//                //maxResolution.height
//                //arsession?.setCameraConfig(filter)
//                //Log.d("max width", arsession.cameraConfig.imageSize.height.toString())
//                //Log.d("max height",arsession.cameraConfig.imageSize.width.toString())
//            } catch (e: Exception) {
//                Log.e("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" , e.toString())
////                Toast.makeText(this, "Failed to create AR session", Toast.LENGTH_SHORT).show()
//            }
//        }


        initCM = resources.getString(R.string.initCM)

        isStoragePermissionGranted()
        initRenderable()
        createImageFile()
        createImageFile2()
        createImageFile3()
        val distancetext = distancetext.findViewById<TextView>(R.id.distancetext)
        //val button = findViewById<Button>(R.id.button2)

        downlodfolder.setOnClickListener {
            startActivity(intent)
        }
        success_folder.setOnClickListener{
            startActivity(intent2)
        }
        fail_folder.setOnClickListener{
            startActivity(intent3)
        }


        //arFragment!!.arSceneView.session?.sharedCamera
        //arFragment!!.arSceneView.session?.setDisplayGeometry(displayRotation, screenWidth, screenHeight)


        arFragment!!.arSceneView.planeRenderer.isEnabled = false

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (cubeRenderable == null || distanceCardViewRenderable == null) return@setOnTapArPlaneListener
            // Creating Anchor.


            val arFrame = arFragment?.arSceneView?.arFrame
            val screenwidth_pos = screenWidth / 3
            val screenheight_pos = screenHeight / 2
            var hitTestresult = arFrame!!.hitTest(screenwidth_pos.toFloat(), screenheight_pos.toFloat())
            var hitTestresult2 = arFrame!!.hitTest( (screenwidth_pos+200).toFloat(), screenheight_pos.toFloat())
            Log.d("1번 좌좌좌표", screenwidth_pos.toString())
            Log.d("2번 좌좌좌표", screenheight_pos.toString())
            Log.d("3번 좌좌좌표" , (screenwidth_pos+200).toFloat().toString())


            var anchor1 = hitTestresult[0].createAnchor()
            var anchorNode1 = AnchorNode(anchor1)
            var anchor2 = hitTestresult2[0].createAnchor()
            var anchorNode2 = AnchorNode(anchor2)
            var distance1 = calculateDistance2(anchorNode1.worldPosition, anchorNode2.worldPosition)
            var resultt = distance1 / 2
            Log.d("1번 좌좌좌표", distance1.toString())
            Log.d("2번 좌좌좌표", resultt.toString())

//            arFragment!!.arSceneView.session?.setDisplayGeometry(displayMetrics..arFragment!!.arSceneView.session?.rotation, screenWidth, screenHeight)
            var distanceMeter: Float = 0.0F
            if (arFrame != null) {

                val camera = arFrame.camera
                var anchor = hitResult.createAnchor()
                var anchorNode = AnchorNode(anchor)
                cameraconfig = arFragment!!.arSceneView.session!!.cameraConfig



                //Log.d("qweqweqweqwewqe", streamConfigurationMap.toString())
                cameraIntrinsics = camera.imageIntrinsics           //핸드폰 기기 카메라 해상도 가져오기
                distanceMeter = calculateDistance(
                    anchorNode.worldPosition,
                    arFrame!!.camera.pose
                )


                Log.d("hitresult_distance", distanceMeter.toString())
            } else {
                Log.d(
                    TAG,
                    "ARCore camera not available or not tracking ${arFragment?.arSceneView!!.arFrame}"
                )
            }

            Log.d("지원되는 기기", ArCoreApk.Availability.SUPPORTED_INSTALLED.toString()) // 지원되는 기기인지 확인
            //Log.d("핸드폰 화면 해상도 widthPixels", view1.width.toString())
            //Log.d("핸드폰 화면 해상도 heightPixels",  view1.height.toString()) //핸드폰 해상도 지원


            var result: Float = 0.0f

//            Log.d("hitresult_distance" , test?.get(1)?.distance.toString()+"m")

            // 픽셀 대 거리 비율 계산
            if (cameraIntrinsics != null) {
                val screenAspectRatio =
                    arFragment!!.arSceneView.width.toFloat() / arFragment!!.arSceneView.height.toFloat()
                val verticalFov =
                    2.0f * Math.atan(Math.tan((cameraIntrinsics!!.focalLength[1] / 2.0f).toDouble()) * screenAspectRatio)
                        .toFloat()
                val horizontalFov =
                    2.0f * Math.atan2(cameraIntrinsics!!.focalLength[0].toDouble()/ 10 , cameraconfig!!.imageSize.width.toDouble()) //* screenAspectRatio
                val verticalFov1 =
                    2.0f * Math.atan(Math.tan(cameraIntrinsics!!.focalLength[1].toDouble()) * screenAspectRatio).toFloat()
                val horizontalFov1 =
                    2.0f * Math.atan(Math.tan(cameraIntrinsics!!.focalLength[0].toDouble()) * screenAspectRatio).toFloat()


                //val imageWidth = cameraIntrinsics!!.imageDimensions[0].toFloat()
                val imageWidth = 640f
                //val imageHeight = cameraIntrinsics!!.imageDimensions[1].toFloat()
                val imageHeight = 480f
                val diagonalAngle = atan(
                    sqrt(
                        imageWidth.toDouble().pow(2.0) + imageHeight.toDouble()
                            .pow(2.0)
                    ) * tan(verticalFov1 / 2.0) / imageWidth.toDouble()
                )
                val diagonalDistance = distanceMeter * tan(diagonalAngle / 2.0).toFloat() * 2.0f
                //  val testDistance = Math.tan(horizontalFov / 2) * cameraIntrinsics!!.focalLength[0].toDouble() / 10/ distanceMeter
                val pixelsToDistanceRatio = sqrt(
                    imageWidth.toDouble().pow(2.0) + imageHeight.toDouble()
                        .pow(2.0)
                ).toFloat() / diagonalDistance
                result =
                    abs((1 / pixelsToDistanceRatio) * 100).toFloat()            //전체 픽셀을 한 개의 픽셀 값
                Log.d("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk", result.toString())
                Log.d(TAG, "Pixels-to-distance ratio: $imageWidth pixels/meter")    //장치의 카메라 해상도
                Log.d(TAG, "Pixels-to-distance ratio: $imageHeight pixels/meter")
                //  Log.d(TAG, "diagonalangle: $testDistance pixels/meter")
                Log.d(TAG, "Pixels-to-distance ratio: $diagonalDistance pixels/meter")
                Log.d(
                    "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                    cameraIntrinsics!!.imageDimensions[0].toFloat().toString()
                )
                Log.d(
                    "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRR",
                    cameraIntrinsics!!.imageDimensions[1].toFloat().toString()
                )


            } else {
                Log.d(TAG, "Camera intrinsics not available.")
            }


            distancetext.text = (distanceMeter * 100).roundToInt().toString() + "cm"
            //distancetext.text = result.toString()
            //distancetext.text = motionEvent?.x.toString()
            Log.d("motionEvent x", motionEvent?.x.toString())
            Log.d("motionEvent y", motionEvent?.y.toString())

            val x = motionEvent?.x.toString().toFloat()
            val y = motionEvent?.y.toString().toFloat()

            saveButton(whichcode, distanceMeter, resultt,x, y)
        }


    }


    private fun initRenderable() {
        MaterialFactory.makeTransparentWithColor(
            this,
            arColor(Color.RED)
        )
            .thenAccept { material: Material? ->
                cubeRenderable = ShapeFactory.makeSphere(
                    0.02f,
                    Vector3.zero(),
                    material
                )
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
            .thenAccept {
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

    private fun setMode() {
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

    fun createImageFile2(): File? { // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        var imageFile: File?
        val storageDir = File(
            Environment.getExternalStorageDirectory().toString() + "/DCIM",
            "Success"
        )
        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath2", storageDir.toString())
            storageDir.mkdirs()
        }
        imageFile = File(storageDir, imageFileName)
        imageFile.absolutePath
        return imageFile
    }
    fun createImageFile3(): File? { // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        var imageFile: File?
        val storageDir = File(
            Environment.getExternalStorageDirectory().toString() + "/DCIM",
            "Fail"
        )
        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath3", storageDir.toString())
            storageDir.mkdirs()
        }
        imageFile = File(storageDir, imageFileName)
        imageFile.absolutePath
        return imageFile
    }

    private fun saveButton(
        code: String, distance: Float, result: Float,x: Float,y: Float) {

        val now =
            SimpleDateFormat("yyyyMMdd_hhmmss").format(Date(System.currentTimeMillis()))
        Log.d("media path    ", Environment.getDownloadCacheDirectory().toString())
        val rootPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Temporary"

        val fileName =
            "${code}_${now}_${(distance * 100).roundToInt()}_${(result.toString()).substring(2)}_${x.toInt()}_${y.toInt()}.png"
        val savePath = File(rootPath, "/")
        savePath.mkdirs()
        savePath.absoluteFile

        Log.d("save mkdir   ", savePath.mkdirs().toString())
        Log.d("save Path   ", savePath.absolutePath)
        Log.d("save file   ", savePath.absoluteFile.toString())

        val file = File(savePath, fileName)
        if (file.exists()) file.delete()
        //val new_test = arFragment!!.arSceneView
        val vieww = arFragment!!.arSceneView
        soundId = soundPool.load(this,R.raw.camera,0)
        //val view1 = arFragment!!.view

        clearAllAnchors()
        //onScreenCaptured()
        val bitmap = takeScreenshotOfRootView(vieww)



        val locationOfViewInWindow = IntArray(2)
        val xCoordinate = locationOfViewInWindow[0]
        val yCoordinate = locationOfViewInWindow[1]


        savePath.absolutePath
        arFragment?.let {
            PixelCopy.request(it.arSceneView, bitmap, PixelCopy.OnPixelCopyFinishedListener {
                100
                if (it != PixelCopy.SUCCESS) {
                    /// Fallback when request fails...
                    return@OnPixelCopyFinishedListener
                }
                Log.d(TAG, "svBitmap w : ${bitmap.width} , h : ${bitmap.height}")

                /// Handle retrieved Bitmap...
                Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + vieww.width,
                    locationOfViewInWindow[1] + vieww.height
                )
            }, vieww.handler)
        }

        try {
            soundPool.play(soundId,1.0f,1.0f,0,0,1.5f)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)

            out.flush()
            out.close()

            val inflater = layoutInflater
// Custom 레이아웃 Imflatation '인플레이션', 레이아웃 메모리에 객체화
// Custom 레이아웃 Imflatation '인플레이션', 레이아웃 메모리에 객체화
//            val layout: View = inflater.inflate(
//                R.layout.custom_toast,
//                findViewById<View>(R.id.custom_toast_layout) as ViewGroup?
//            )

// 보여줄 이미지 설정 위해 ImageView 연결
// 보여줄 이미지 설정 위해 ImageView 연결
//            val image: ImageView = layout.findViewById(R.id.custom_toast_image)
//            image.setBackgroundResource(R.drawable.gallery)

            //val a: Bitmap? = Bitmap.createBitmap(view1.width, view1.height, Bitmap.Config.ARGB_8888)
//            val image = layout.findViewById<ImageView>(R.id.custom_toast_image)
//            image.setImageBitmap(bitmap)


// Toast 객체 생성

// Toast 객체 생성
//            val toast = Toast(this)
// 위치설정, Gravity - 기준지정(상단,왼쪽 기준 0,0) / xOffset, yOffset - Gravity기준으로 위치 설정
// 위치설정, Gravity - 기준지정(상단,왼쪽 기준 0,0) / xOffset, yOffset - Gravity기준으로 위치 설정
//            toast.setGravity(Gravity.CENTER or Gravity.FILL, 0, 0)
// Toast 보여줄 시간 'Toast.LENGTH_SHORT 짧게'
// Toast 보여줄 시간 'Toast.LENGTH_SHORT 짧게'

// CustomLayout 객체 연결
// CustomLayout 객체 연결
//            toast.view = layout
// Toast 보여주기
// Toast 보여주기
//            toast.show()
//            Handler().postDelayed(Runnable{
//                run(){
//                    toast.cancel()
//                }
//            },100)
//            Log.d("ddddddddddd", toast.toString())

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
            view.isFocusable = true
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            Log.d("arFragment~~~~ : ", view.width.toString())
            Log.d("arFragment~~~~ : ", view.height.toString())
            view.isDrawingCacheEnabled = false
            var canvas = Canvas(b)
            view.draw(canvas)
            return b

        }

        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v)

        }
    }


    private fun clearAllAnchors() {
        placedAnchors.clear()
        for (anchorNode in placedAnchorNodes) {
            arFragment!!.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)
        }
        placedAnchorNodes.clear()
        midAnchors.clear()
        for ((k, anchorNode) in midAnchorNodes) {
            arFragment!!.arSceneView.scene.removeChild(anchorNode)
            anchorNode.isEnabled = false
            anchorNode.anchor!!.detach()
            anchorNode.setParent(null)
        }
        midAnchorNodes.clear()
        for (i in 0 until Constants.maxNumMultiplePoints) {
            for (j in 0 until Constants.maxNumMultiplePoints) {
                if (multipleDistances[i][j] != null) {
                    multipleDistances[i][j]!!.setText(if (i == j) "-" else initCM)
                }
            }
        }
        fromGroundNodes.clear()

    }


    private fun placeAnchor(
        hitResult: HitResult,
        renderable: Renderable
    ) {
        val anchor = hitResult.createAnchor()
        placedAnchors.add(anchor)

        val anchorNode = AnchorNode(anchor).apply {
            isSmoothed = true
            setParent(arFragment!!.arSceneView.scene)
        }
        placedAnchorNodes.add(anchorNode)

        val node = TransformableNode(arFragment!!.transformationSystem)
            .apply {
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
        when (distanceMode) {
            distanceModeArrayList[0] -> {
                measureDistanceFromCamera()
            }
            else -> {
                measureDistanceFromCamera()
            }
        }
    }


    private fun measureDistanceFromCamera() {
        val frame = arFragment!!.arSceneView.arFrame
        if (placedAnchorNodes.size >= 1) {
            val distanceMeter = calculateDistance(
                placedAnchorNodes[0].worldPosition,
                frame!!.camera.pose
            )
            measureDistanceOf2Points(distanceMeter)
        }
    }


    private fun measureDistanceOf2Points(distanceMeter: Float) {
        val distanceTextCM = makeDistanceTextWithCM(distanceMeter)
        val textView = (distanceCardViewRenderable!!.view as LinearLayout)
            .findViewById<TextView>(R.id.distanceCard)
        textView.text = distanceTextCM
        Log.d(TAG, "distance: ${distanceTextCM}")
    }

    private fun makeDistanceTextWithCM(distanceMeter: Float): String {
        val distanceCM = changeUnit(distanceMeter, "cm")
        val distanceCMFloor = "%.2f".format(distanceCM)
        return "${distanceCMFloor} cm"
    }

    private fun calculateDistance(x: Float, y: Float, z: Float): Float {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }


    private fun calculateDistance(objectPose0: Vector3, objectPose1: Pose): Float {
        return calculateDistance(
            objectPose0.x - objectPose1.tx(),
            objectPose0.y - objectPose1.ty(),
            objectPose0.z - objectPose1.tz()
        )
    }

    private fun calculateDistance2(objectPose0: Vector3, objectPose1: Vector3): Float {
        return calculateDistance(
            objectPose0.x - objectPose1.x,
            objectPose0.y - objectPose1.y,
            objectPose0.z - objectPose1.z
        )
    }


    private fun changeUnit(distanceMeter: Float, unit: String): Float {
        return when (unit) {
            "cm" -> distanceMeter * 100
            "mm" -> distanceMeter * 1000
            else -> distanceMeter
        }
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        val openGlVersionString =
            (Objects.requireNonNull(
                activity
                    .getSystemService(ACTIVITY_SERVICE)
            ) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES ${MIN_OPENGL_VERSION} later")
            Toast.makeText(
                activity,
                "Sceneform requires OpenGL ES ${MIN_OPENGL_VERSION} or later",
                Toast.LENGTH_LONG
            )
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
        Toast.makeText(
            this,
            "Please grant read external storage permission for screenshot detection",
            Toast.LENGTH_SHORT
        ).show()
        // Do something when screen was captured but read external storage permission has denied
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestReadExternalStoragePermission()
        }
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION
        )
    }

    private fun showReadExternalStoragePermissionDeniedMessage() {
        Toast.makeText(this, "Read external storage permission has denied", Toast.LENGTH_SHORT)
            .show()
    }

    fun configureARCoreCamera(session: Session) {
        val filter = CameraConfigFilter(session)
        val configs = session.getSupportedCameraConfigs(filter)

        // Check if the camera configs are available
        if (configs.isNotEmpty()) {
            // Select a desired camera configuration (you can modify this according to your needs)
            val desiredConfig = findBestCameraConfig(configs)

            // Set the desired camera configuration
            session.cameraConfig = desiredConfig

            // Restart the AR session for the changes to take effect
            session.configure(Config(session))
        } else {
            // Handle the case when no camera configurations are available
            // You can display an error message or fallback to a default configuration
        }
    }

    // Function to find the best camera configuration based on your preferences
    fun findBestCameraConfig(configs: List<CameraConfig>): CameraConfig {
        // Here, you can implement your own logic to select the best camera configuration
        // This example simply selects the first available configuration
        return configs.first()
    }


    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        try {
            when (ArCoreApk.getInstance().checkAvailability(this)) {
                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    Toast.makeText(this, "This device does not support AR", Toast.LENGTH_SHORT)
                        .show()
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