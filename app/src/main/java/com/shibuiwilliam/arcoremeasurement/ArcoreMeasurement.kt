package com.shibuiwilliam.arcoremeasurement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
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
    var seleted : String = "위판장"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcore_measurement)


        val adapter = ArrayAdapter.createFromResource(this,R.array.위판장, R.layout.spinner_item_go)
        adapter.setDropDownViewResource(R.layout.spinner_drop_go)

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
        //activity_main안에 이미 adapter 속성이 있다. 해당 속성과 위에서 만든 adapter를 연결.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                textView.text = "선택됨: $position ${spinner.getItemAtPosition(position)}"
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
                    "위판장" -> spinner2.adapter = default
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
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                seleted = "위판장"
            }
        }

//        spinner2.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                seleted = "경기도"
//            }
//        }


        val buttonArray = resources.getStringArray(R.array.arcore_measurement_buttons)

        buttonArray.map { it ->
            buttonArrayList.add(it)
        }
        toMeasurement = findViewById(R.id.to_measurement)

        toMeasurement.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(application, Measurement::class.java)
                startActivity(intent)
            }
        })
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
        val textView = findViewById<TextView>(R.id.textView)


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

}
