package com.shibuiwilliam.arcoremeasurement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
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


class ArcoreMeasurement : AppCompatActivity(), DynamicSpinnerAdapter.SpinnerItemSelectedListener {
    private val TAG = "ArcoreMeasurement"
    private val buttonArrayList = ArrayList<String>()
    private lateinit var toMeasurement: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arcore_measurement)

        val planetStructureJson = "{\n" +
        //        "             \"planetList\" : [\n"+
           //     "{\n" +
           //     " \"name\" : \"gg\" ,\n" +
                "            \"countryList\": [\n" +
                "            {\n" +
                "                \"code\": 0,\n" +
                "                \"name\": \"부산\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"위판장 번호\",\n" +
                "                    \"code\": 111\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"120\",\n" +
                "                    \"code\": 2\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"144420\",\n" +
                "                    \"code\": 2\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"13320\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"인천\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"139\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"울산\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"333\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"경기도\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"222\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"강원\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"충청남도\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"전라북도\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"전라남도\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"경상북도\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"경상남도\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"code\": 1,\n" +
                "                \"name\": \"제주\",\n" +
                "                \"cityList\": [\n" +
                "                {\n" +
                "                    \"name\": \"Tallin\",\n" +
                "                    \"code\": 2\n" +
                "                }\n" +
                "                ]\n" +
                "            }\n" +
                "            ]\n" +
      //          "            }\n" +
    //            "            ]\n" +

         //       "]\n" +
                "        }"
        val planetStructure =
            "{\"planetList\":[{\"countryList\":[{\"code\":1,\"cityList\":[{\"name\":\"West madow\",\"code\":1000,\"avenueList\":[{\"name\":\"Azdlig ave.\",\"code\":10000,\"streetList\":[{\"name\":\"Lev Landau\",\"code\":100000,\"alleyList\":[{\"name\":\"Hajibekov alley\",\"code\":1000000,\"buildingList\":[{\"name\":\"Hyatt\",\"code\":1000002,\"entanceList\":[{\"name\":\"Entrance 1\",\"code\":1000003,\"storeyList\":[{\"name\":\"1st Floor\",\"code\":1000004,\"apartmentList\":[{\"name\":\"Apartment 109\",\"code\":1000005,\"roomList\":[{\"name\":\"Bedroom\",\"code\":1000006,\"itemList\":[{\"name\":\"Laptop\",\"code\":1000008,\"folderList\":[{\"name\":\"Folder -1\",\"code\":1000009,\"fileList\":[{\"name\":\"Rok.txt\",\"code\":1000010,\"contentList\":[{\"name\":\"Letter A\",\"code\":1000011,\"byteList\":[{\"name\":\"-1kb\",\"code\":1000012}]}]}]}]}]}]}]}]}]}]}]},{\"name\":\"East Krakow\",\"code\":1001,\"avenueList\":[{\"name\":\"Bashir Sefereov ave.\",\"code\":10001,\"streetList\":[{\"name\":\"Nikita Labov\",\"code\":100001}]},{\"name\":\"Prehis Albania ave\",\"code\":10003,\"streetList\":[]}]},{\"name\":\"Roden highs\",\"code\":1002,\"avenueList\":[]}]},{\"name\":\"Binagadi\",\"code\":101}]},{\"name\":\"Ganja\",\"code\":11,\"boroughList\":[{\"name\":\"South Ganja\",\"code\":110},{\"name\":\"North Ganja\",\"code\":111}]}],\"name\":\"Azerbaijan\"},{\"code\":2,\"cityList\":[{\"name\":\"Dubai\",\"code\":20},{\"name\":\"Abu Dhabi\",\"code\":21}],\"name\":\"UAE\"},{\"code\":3,\"cityList\":[{\"name\":\"Bangkok\",\"code\":30,\"boroughList\":[{\"name\":\"Kingtown\",\"code\":300}]},{\"name\":\"Pattaya\",\"code\":31}],\"name\":\"Thailand\"},{\"code\":4,\"cityList\":[{\"name\":\"Almaty\",\"code\":40},{\"name\":\"Astana\",\"code\":41}],\"name\":\"Kazakhstan\"},{\"code\":5,\"cityList\":[{\"name\":\"Moscow\",\"code\":50},{\"name\":\"Saint Peterburg\",\"code\":51}],\"name\":\"Russia\"},{\"code\":6,\"cityList\":[{\"name\":\"Tallin\",\"code\":60},{\"name\":\"Tartu\",\"code\":61}],\"name\":\"Estonia\"},{\"code\":7,\"cityList\":[{\"name\":\"Izmir\",\"code\":10},{\"name\":\"Istanbul\",\"code\":11}],\"name\":\"Turkey\"},{\"code\":8,\"cityList\":[{\"name\":\"Rustavi\",\"code\":80},{\"name\":\"Tbilisi\",\"code\":81}],\"name\":\"Georgia\"},{\"code\":1,\"cityList\":[{\"name\":\"Busan\",\"code\":10},{\"name\":\"Daegu\",\"code\":11}],\"name\":\"South Korea\"}],\"name\":\"Earth\"},{\"countryList\":[{\"name\":\"Mercurium country\",\"code\":-1,\"cityList\":[{\"name\":\"Merca city\",\"code\":-2}]}],\"name\":\"Mercury\"},{\"countryList\":[],\"name\":\"Venus\"},{\"countryList\":[],\"name\":\"Mars\"}]}"



        val list = Gson().fromJson<com.shibuiwilliam.arcoremeasurement.model.Response>(
            planetStructureJson, com.shibuiwilliam.arcoremeasurement.model.Response::class.java
        ).countryList



        list?.let {
            dynamic_spinner.adapter = DynamicSpinnerAdapter(it, this, R.layout.item_spinner)
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

    override fun onItemSelected(itemSpinner: ItemSpinner) {
        //Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaa" , itemSpinner.itemSpinnerLevel.toString())
        when (itemSpinner.itemSpinnerLevel) {

            0 -> showToast((itemSpinner as Country).name)
            1 -> showToast((itemSpinner as City).name)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, "Selected item: ${message}", Toast.LENGTH_SHORT).show()
    }

}
