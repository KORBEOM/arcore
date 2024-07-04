package com.shibuiwilliam.arcoremeasurement

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

import com.google.ar.core.Point
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mecofarid.dynamicspinner.adapter.DynamicSpinnerAdapter
import com.mecofarid.dynamicspinner.model.ItemSpinner
import com.shibuiwilliam.arcoremeasurement.model.City
import com.shibuiwilliam.arcoremeasurement.model.Country
import com.shibuiwilliam.arcoremeasurement.model.Planet
import kotlinx.android.synthetic.main.activity_arcore_measurement.*
import retrofit2.Response


class ArcoreMeasurement : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    AdapterView.OnItemClickListener {
    private val TAG = "ArcoreMeasurement"
    private val buttonArrayList = ArrayList<String>()
    private lateinit var toMeasurement: Button
    private lateinit var nameEditText: EditText
    var seleted : String = "지역"
    var which : String = "위판장"
    var whichCode : String = "0000"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcore_measurement)

        val one_text : TextView = findViewById(R.id.title)
        val two_text : TextView = findViewById(R.id.title2)
        val logo : ImageView = findViewById(R.id.imageView2)

        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getRealSize(size)
        val width = size.x
        val height = size.y

        toMeasurement = findViewById(R.id.to_measurement)
        nameEditText = findViewById(R.id.nameEditText)
        Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" , width.toString())
        Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" , height.toString())

        val adapter = ArrayAdapter.createFromResource(this,R.array.위판장, R.layout.spinner_item_go)
        adapter.setDropDownViewResource(R.layout.spinner_drop_go)
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("ArcoreMeasurement", "Before Text Changed: $s")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("ArcoreMeasurement", "On Text Changed: $s")
            }

            override fun afterTextChanged(s: Editable?) {
                // Enable the toMeasurement button if EditText is not empty
                Log.d("ArcoreMeasurement", "After Text Changed: $s")
                val isOnlyKorean = s?.all {
                    it in '\uAC00'..'\uD7AF' || // Hangul Syllables
                            it in '\u1100'..'\u11FF' || // Hangul Jamo
                            it in '\u3130'..'\u318F' || // Hangul Compatibility Jamo
                            it in '\uA960'..'\uA97F' || // Hangul Jamo Extended-A
                            it in '\uD7B0'..'\uD7FF'    // Hangul Jamo Extended-B
                } ?: false
                toMeasurement.isEnabled = !s.isNullOrEmpty() && isOnlyKorean
                if (!isOnlyKorean && !s.isNullOrEmpty()) {
                    val snackbar = Snackbar.make(nameEditText, "한글만 입력해주세요", Snackbar.LENGTH_LONG)

                    // Customize Snackbar Background Color
                    snackbar.view.setBackgroundColor(Color.DKGRAY)
                    // Set an Action Button (Optional)
                    snackbar.setAction("OK") {
                        // Action when "OK" is clicked
                    }.setActionTextColor(Color.YELLOW) // Customize Action Button Text Color

                    snackbar.show()
                }
            }

        })
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("MeasurementScreenOpened", false)) {
            val nameText = sharedPreferences.getString("LastEnteredName", "")
            val whichCode = sharedPreferences.getString("LastSelectedWhichCode", "DefaultCode")
            val intent = Intent(this, Measurement::class.java).apply {
                putExtra("whichCode", whichCode)
                putExtra("name", nameText)
            }
            startActivity(intent)
            finish() // Finish ArcoreMeasurement activity
        }
        //activity_main.xml에 입력된 spinner에 어댑터를 연결한다.
        val spinner = findViewById<Spinner>(R.id.spinner)
        spinner.adapter = adapter
        val default =  ArrayAdapter.createFromResource(this , R.array.고정, R.layout.spinner_item_go)
        default.setDropDownViewResource(R.layout.spinner_drop_go)
        val gyeonggi = ArrayAdapter.createFromResource(this , R.array.경기, R.layout.spinner_item_go)
        gyeonggi.setDropDownViewResource(R.layout.spinner_drop_go)
        val incheon = ArrayAdapter.createFromResource(this , R.array.인천, R.layout.spinner_item_go)
        incheon.setDropDownViewResource(R.layout.spinner_drop_go)
        val chungnam = ArrayAdapter.createFromResource(this , R.array.충남, R.layout.spinner_item_go)
        chungnam.setDropDownViewResource(R.layout.spinner_drop_go)
        val geonam = ArrayAdapter.createFromResource(this , R.array.전남, R.layout.spinner_item_go)
        geonam.setDropDownViewResource(R.layout.spinner_drop_go)
        val geobuk = ArrayAdapter.createFromResource(this , R.array.전북, R.layout.spinner_item_go)
        geobuk.setDropDownViewResource(R.layout.spinner_drop_go)
        val gangwon = ArrayAdapter.createFromResource(this , R.array.강원, R.layout.spinner_item_go)
        gangwon.setDropDownViewResource(R.layout.spinner_drop_go)
        val woolsan = ArrayAdapter.createFromResource(this , R.array.울산, R.layout.spinner_item_go)
        woolsan.setDropDownViewResource(R.layout.spinner_drop_go)
        val gyeongbuk = ArrayAdapter.createFromResource(this , R.array.경북, R.layout.spinner_item_go)
        gyeongbuk.setDropDownViewResource(R.layout.spinner_drop_go)
        val gyeongnam = ArrayAdapter.createFromResource(this , R.array.경남, R.layout.spinner_item_go)
        gyeongnam.setDropDownViewResource(R.layout.spinner_drop_go)
        val boosan = ArrayAdapter.createFromResource(this , R.array.부산, R.layout.spinner_item_go)
        boosan.setDropDownViewResource(R.layout.spinner_drop_go)
        val jejoo = ArrayAdapter.createFromResource(this , R.array.제주, R.layout.spinner_item_go)
        jejoo.setDropDownViewResource(R.layout.spinner_drop_go)

        val spinner2 = findViewById<Spinner>(R.id.spinner2)
        spinner2.adapter = adapter

        isStoragePermissionGranted()

        //activity_main안에 이미 adapter 속성이 있다. 해당 속성과 위에서 만든 adapter를 연결.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                textView.text = "선택됨: $position ${spinner.getItemAtPosition(position)}"
                seleted = "${spinner.getItemAtPosition(position)}"


//                if(seleted == "경기도"){
//                    spinner2.adapter = gyeonggi
//                    gyeonggi.notifyDataSetChanged()
//                }
//                if(seleted == "인천"){
//                    spinner2.adapter = incheon
//                    incheon.notifyDataSetChanged()
//                }
                when(seleted){
                    "지역" -> spinner2.adapter = default
                    "경기도" ->  spinner2.adapter = gyeonggi
                    "인천" ->  spinner2.adapter = incheon
                    "충청남도" ->  spinner2.adapter = chungnam
                    "전라남도" ->  spinner2.adapter = geonam
                    "전라북도" ->  spinner2.adapter = geobuk
                    "강원도" ->  spinner2.adapter = gangwon
                    "울산" ->  spinner2.adapter = woolsan
                    "경상북도" ->  spinner2.adapter = gyeongbuk
                    "경상남도" ->  spinner2.adapter = gyeongnam
                    "부산" ->  spinner2.adapter = boosan
                    "제주도" ->  spinner2.adapter = jejoo
                }
//                val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
//                with(sharedPreferences.edit()) {
//                    putString("LastSelectedWhichCode", whichCode)
//                    apply()
//                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                seleted = "위판장"
            }
        }


        spinner2.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                which = "${spinner2.getItemAtPosition(position)}"

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                seleted = "경기도"
            }
        }



        val buttonArray = resources.getStringArray(R.array.arcore_measurement_buttons)

        buttonArray.map { it ->
            buttonArrayList.add(it)
        }


        toMeasurement.setOnClickListener {
            val nameText = nameEditText.text.toString()
            if (nameText.isNotEmpty()) {
                when (which) {
                    "부산공동어시장" -> whichCode = "0001"
                    "부산시수협 다대공판장" -> whichCode = "0002"
                    "부산시수협 자갈치공판장" -> whichCode = "0003"
                    "부산시수협 민락위판장" -> whichCode = "0004"
                    "기장수협 대변위판장" -> whichCode = "0005"
                    "기장수협 학리위판장" -> whichCode = "0006"
                    "제1,2구 잠수기 수협 부산위판장" -> whichCode = "0007"
                    "부산국제수산물도매시장" -> whichCode = "0008"
                    "수협중앙회 인천위판장" -> whichCode = "0009"
                    "인천수협 연안위판장" -> whichCode = "0010"
                    "인천수협 소래위판장" -> whichCode = "0011"
                    "옹진 수협 연안위판장" -> whichCode = "0012"
                    "영흥수협 영흥위판장" -> whichCode = "0013"
                    "경인북부수협 새우젓산지(외포리) 위판장" -> whichCode = "0014"
                    "울산수협 울산수협공판장" -> whichCode = "0015"
                    "울산수협 방어진 위판장" -> whichCode = "0016"
                    "울산수협 강동위판장" -> whichCode = "0017"
                    "경기남부수협 궁평리위판장" -> whichCode = "0018"
                    "옹진수협 대부(방아머리) 위판장" -> whichCode = "0019"
                    "옹진수협 대부도(탄도) 위판장" -> whichCode = "0020"
                    "속초시수협 청호항위판장" -> whichCode = "0021"
                    "속초시수협 동명항위판장" -> whichCode = "0022"
                    "강릉시수협 주문진항위판장" -> whichCode = "0023"
                    "강릉시수협 사천진항위판장" -> whichCode = "0024"
                    "동해시수협 묵호항위판장" -> whichCode = "0025"
                    "삼척수협 삼척위판장" -> whichCode = "0026"
                    "삼척수협 장호항위판장" -> whichCode = "0027"
                    "원덕수협 임원항위판장" -> whichCode = "0028"
                    "대포수협 대포항위판장" -> whichCode = "0029"
                    "양양군수협 남애항위판장" -> whichCode = "0030"
                    "양양군수협 동산항위판장" -> whichCode = "0031"
                    "양양군수협 기사문항위판장" -> whichCode = "0032"
                    "양양군수협 물치위판장" -> whichCode = "0033"
                    "강원 고성군수협 아야진항위판장" -> whichCode = "0034"
                    "고성군수협 거진항위판장" -> whichCode = "0035"
                    "고성군수협 대진항위판장" -> whichCode = "0036"
                    "죽왕수협 가진항위판장" -> whichCode = "0037"
                    "죽왕수협 공현진항위판장" -> whichCode = "0038"
                    "죽왕수협 오호항위판장" -> whichCode = "0039"
                    "죽왕수협 문암1리위판장" -> whichCode = "0040"
                    "죽왕수협 문암2리위판장" -> whichCode = "0041"
                    "서산수협 안흥위판장" -> whichCode = "0042"
                    "서산수협 모항위판장" -> whichCode = "0043"
                    "서산수협 채석포 위판장" -> whichCode = "0044"
                    "태안남부수협 몽산포위판장" -> whichCode = "0045"
                    "태안남부수협 마검포위판장" -> whichCode = "0046"
                    "태안남부수협 드르니 위판장" -> whichCode = "0047"
                    "태안남부수협 당암위판장" -> whichCode = "0048"
                    "안면도수협 백사장위판장" -> whichCode = "0049"
                    "안면도수협 영목위판장" -> whichCode = "0050"
                    "대천서부수협 대천항위판장" -> whichCode = "0051"
                    "서천서부수협 홍원위판장" -> whichCode = "0052"
                    "서천서부수협 마량위판장" -> whichCode = "0053"
                    "보령수협 오천사업소위판장" -> whichCode = "0054"
                    "보령수협 대천항 위판장" -> whichCode = "0055"
                    "제 3,4,구 잠수기수협 오천위판장" -> whichCode = "0056"
                    "보령수협 무창포어촌계위판장" -> whichCode = "0057"
                    "서천군수협 장항위판장" -> whichCode = "0058"
                    "보령수협 삼길포항 위판장" -> whichCode = "0059"
                    "군산시수협 해망동위판장" -> whichCode = "0060"
                    "군산시수협 비응항위판장" -> whichCode = "0061"
                    "부안수협 격포위판장" -> whichCode = "0062"
                    "군산시수협 선유도위판장" -> whichCode = "0063"
                    "여수수협 군내위판장" -> whichCode = "0064"
                    "여수수협 국동위판장" -> whichCode = "0065"
                    "목포수협 동부위판장" -> whichCode = "0066"
                    "목포수협 서부위판장" -> whichCode = "0067"
                    "제3,4구 잠수기수협 국동위판장" -> whichCode = "0068"
                    "완도금일수협 활선어위판장" -> whichCode = "0069"
                    "고흥군수협 녹동위판장" -> whichCode = "0070"
                    "나로도수협 나로도위판장" -> whichCode = "0071"
                    "강진군수협 마량(활선어)위판장" -> whichCode = "0072"
                    "신안군수협 송도위판장" -> whichCode = "0073"
                    "신안군수협 흑산도 위판장" -> whichCode = "0074"
                    "거문도수협 거문도위판장" -> whichCode = "0075"
                    "영광군수협 법성위판장" -> whichCode = "0076"
                    "진도군수협 서망위판장" -> whichCode = "0077"
                    "장흥군수협 정남진위판장" -> whichCode = "0078"
                    "후포수협 후포위판장" -> whichCode = "0079"
                    "후포수협 구산위판장" -> whichCode = "0080"
                    "후포수협 사동위판소" -> whichCode = "0081"
                    "강구수협 강구위판장" -> whichCode = "0082"
                    "구룡포수협 구룡포 위판장" -> whichCode = "0083"
                    "구룡포수협 호미곶위판장" -> whichCode = "0084"
                    "구룡포수협 장기위판장" -> whichCode = "0085"
                    "포항수협 죽도위판장" -> whichCode = "0086"
                    "포항수협 송도활어위판장" -> whichCode = "0087"
                    "죽변수협 죽변위판장" -> whichCode = "0088"
                    "죽변수협 오산위판장" -> whichCode = "0089"
                    "경주시수협 감포위판장" -> whichCode = "0090"
                    "영덕북부수협 축산위판장" -> whichCode = "0091"
                    "울릉군수협 저동위판장" -> whichCode = "0092"
                    "울릉군수협 태하위판장" -> whichCode = "0093"
                    "울릉군수협 천부위판장" -> whichCode = "0094"
                    "울릉군수협 현포위판장" -> whichCode = "0095"
                    "삼천포수협 삼천포(선어)위판장" -> whichCode = "0096"
                    "거제수협 장승포위판장" -> whichCode = "0097"
                    "거제수협 성포위판장" -> whichCode = "0098"
                    "통영수협 통영위판장" -> whichCode = "0099"
                    "마산수협 마산위판장" -> whichCode = "0100"
                    "제1,2구 잠수기수협 거제위판장" -> whichCode = "0101"
                    "제1,2구 잠수기수협 남해위판장" -> whichCode = "0102"
                    "제1.,2구 잠수기수협 통영위판장" -> whichCode = "0103"
                    "제1,2구 잠수기수협 마산위판장" -> whichCode = "0104"
                    "멸치권현망수협 통영위판장" -> whichCode = "0105"
                    "멸치권현말수협 마산위판장" -> whichCode = "0106"
                    "남해군수협 미조(선어)위판장" -> whichCode = "0107"
                    "진해시수협 속천위판장" -> whichCode = "0108"
                    "의창수협 용원위판장" -> whichCode = "0109"
                    "의창수협 안골위판장" -> whichCode = "0110"
                    "경남고성군수협 남포항(수남)위판장" -> whichCode = "0111"
                    "사천수협 활어위판장" -> whichCode = "0112"
                    "사량수협 사량위판장" -> whichCode = "0113"
                    "하동군수협 노량위판장" -> whichCode = "0114"
                    "제주시수협 제주위판장" -> whichCode = "0115"
                    "한림수협 한림위판장" -> whichCode = "0116"
                    "서귀포수협 수산물유통센터" -> whichCode = "0117"
                    "서귀포수협 태흥(남원)위판장" -> whichCode = "0118"
                    "성산포수협 성산포위판장" -> whichCode = "0119"
                    "추자도수협 추자항위판장" -> whichCode = "0120"
                    "모슬포수협 모슬포 위판장" -> whichCode = "0121"

                }
                val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean("MeasurementScreenOpened", true)
                    putString("LastEnteredName", nameText)
                    putString("LastSelectedWhichCode",whichCode)
                    apply()
                }
                val intent = Intent(application, HomeActivity::class.java)
                intent.putExtra("whichCode", whichCode)
                intent.putExtra("name",nameText)
                Log.d("whichCode" , whichCode)
                Log.d("name" , nameText)
                startActivity(intent)

            } else {
                // Show a Snackbar to prompt the user to enter their name
                Snackbar.make(nameEditText, "이름을 입력해주세요!", Snackbar.LENGTH_LONG)
                    .setAction("OK") {
                        // Action when "OK" is clicked, if needed
                    }
                    .setActionTextColor(Color.YELLOW)
                    .show()
            }
        }
    }
    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(ContentValues.TAG, "Permission is granted")
                true
            } else {
                Log.v(ContentValues.TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(ContentValues.TAG, "Permission is granted")
            true
        }
    }

    private fun setupSpinnerMain() {
        val fruit = resources.getStringArray(R.array.위판장)

        //fruit이란 변수 안에 value 폴더 안에있는 array.xml,
        //array의 이름인 fruit을 입력하여 불러온다.

        //layout 폴더안에 있는 item_spinner.xml, 을 어댑터에 적용한다.
        //fruit이란 매개변수는 30번째 줄에 선언된 fruit.

    }

    private fun setupSpinnerSub(list : HashMap<String , Int>) {


    }
    fun setupSpinnerHandler2(){
        val spinner = findViewById<Spinner>(R.id.spinner2)


    }
    fun setupSpinnerHandler(adapter : ArrayAdapter<CharSequence>){
        val spinner = findViewById<Spinner>(R.id.spinner)



    }


    private fun showToast22( message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, message, duration).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, "Selected item: ${message}", Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast22(message = "Nothing selected")

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }
    private fun showToast2(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
