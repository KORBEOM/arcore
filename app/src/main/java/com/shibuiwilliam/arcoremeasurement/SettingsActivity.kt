package com.shibuiwilliam.arcoremeasurement

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val notificationSettingsButton: Button = findViewById(R.id.notificationSettingsButton)

        val logoutButton: Button = findViewById(R.id.logoutButton)

        // 사용자 정보 설정 (실제 앱에서는 데이터베이스나 SharedPreferences에서 가져와야 함)

        notificationSettingsButton.setOnClickListener {
            // 알림 설정 화면으로 이동
        }



        logoutButton.setOnClickListener {
            // 로그아웃 처리
            // UserManager.logout()
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
        }
    }
}