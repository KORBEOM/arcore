package com.shibuiwilliam.arcoremeasurement

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.activity_settings.*


class Bottomfragment_two : Fragment() {
    private lateinit var logoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bottomtwo, container, false)

        logoutButton = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            performLogout()
        }

        return view
    }
    private fun performLogout() {
        // 로그아웃 관련 작업 수행 (예: 사용자 데이터 삭제)
        val sharedPreferences = activity?.getSharedPreferences("AppPreferences", android.content.Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.clear()?.apply()

        // ArcoreMeasurement 액티비티로 이동
        val intent = Intent(activity, ArcoreMeasurement::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finishAffinity() // 모든 액티비티 종료
    }


}