package com.shibuiwilliam.arcoremeasurement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fish_data.*
import kotlinx.android.synthetic.main.fragment_bottomfrgment_one.*
import kotlinx.android.synthetic.main.fish_data.*

class HomeActivity : AppCompatActivity() {

    private val bottomFragmentOne = Bottomfrgment_one()// Fragment instance 생성
    private val bottomFragmentTwo = Bottomfragment_two()
    private lateinit var whichCode: String
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_navigiation)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        whichCode = intent.getStringExtra("whichCode") ?: ""
        name = intent.getStringExtra("name") ?: ""
        bottomFragmentOne.setName(name)
        bottomFragmentTwo.setName(name)
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("MeasurementScreenOpened", true)
            putString("LastEnteredName", name)
            putString("LastSelectedWhichCode",whichCode)
            apply()
        }
        Log.d("whichCode123" , whichCode)
        Log.d("name123" , name)
        bottomNavigation.setOnNavigationItemSelectedListener  { item ->
            when(item.itemId) {
                R.id.page_1 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, bottomFragmentOne) // 올바른 Fragment로 교체
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.page_2 -> {
                    val measurementIntent = Intent(this, Measurement::class.java).apply {
                        putExtra("whichCode", whichCode)
                        putExtra("name", name)
                        Log.d("whichCode" , whichCode)
                        Log.d("name" , name)
                    }
                    startActivity(measurementIntent)
                    true
                }
                R.id.page_3 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, bottomFragmentTwo) // 올바른 Fragment로 교체
                        .addToBackStack(null)
                        .commit()
                    true
                }

                else -> false
            }
        }

        // 초기 프래그먼트 로드
        loadFragment(bottomFragmentOne)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}