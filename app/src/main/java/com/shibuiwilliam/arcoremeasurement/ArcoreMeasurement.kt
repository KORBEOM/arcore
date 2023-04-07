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
    //val spinnermain = resources.getStringArray(R.array.spinner_mainop)
    var languages = arrayOf("Java", "PHP", "Kotlin", "Javascript", "Python", "Swift")
    var seleted : String = "귤"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcore_measurement)
        var listHash = HashMap<String , Int>()
        listHash.put("귤" , R.array.test)
        listHash.put("파인애플" , R.array.test2)
        listHash.put("샤인머스캣" , R.array.test3)
        listHash.put("무화과" , R.array.test4)

        val adapter = ArrayAdapter.createFromResource(this,R.array.fruit, R.layout.spinner_item_go)

        //activity_main.xml에 입력된 spinner에 어댑터를 연결한다.
        val spinner = findViewById<Spinner>(R.id.spinner)
        spinner.adapter = adapter

        val adapter2 = ArrayAdapter.createFromResource(this , R.array.test, R.layout.spinner_item_go)
        val adapter3 = ArrayAdapter.createFromResource(this , R.array.test2, R.layout.spinner_item_go)

        val spinner2 = findViewById<Spinner>(R.id.spinner2)
        spinner2.adapter = adapter2
        //activity_main안에 이미 adapter 속성이 있다. 해당 속성과 위에서 만든 adapter를 연결.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                textView.text = "선택됨: $position ${spinner.getItemAtPosition(position)}"
                seleted = "${spinner.getItemAtPosition(position)}"
                if(seleted == "귤"){
                    spinner2.adapter = adapter2
                    adapter2.notifyDataSetChanged()
                }
                if(seleted == "파인애플"){
                    spinner2.adapter = adapter3
                    adapter3.notifyDataSetChanged()
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                seleted = "귤"
            }
        }

        spinner2.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                seleted = "귤"
            }
        }


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
        val fruit = resources.getStringArray(R.array.fruit)

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
        when (view?.id) {
            1 -> showToast22(message = "Spinner 2 Position:${position} and language: ${languages[position]}")
            else -> {
                showToast22(message = "Spinner 1 Position:${position} and language: ${languages[position]}")
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast22(message = "Nothing selected")

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

}
