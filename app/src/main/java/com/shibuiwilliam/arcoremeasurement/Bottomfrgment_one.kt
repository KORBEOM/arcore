package com.shibuiwilliam.arcoremeasurement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Bottomfrgment_one : Fragment(R.layout.fragment_bottomfrgment_one) {

    private val fishInfo = listOf(
        FishData("꽃게", R.drawable.crab, "금어기: 일반, 특정해역 6.21~8.20 \n연편어장, 백령도, 대청도, 소청도 주변해역 7.1~8.31", "금지체장: 갑장 6.4cm 이하"),
        FishData("참조기", R.drawable.croaker,  "금어기: 7.1~7.31, 근해유자망 4.22~8.10", "금지체장: 전장 15cm 이하"),
        FishData("갈치", R.drawable.cutlessfish,  "금어기: 7.1~7.31 (근해채낚기, 연안복합 제외)", "금지체장: 항문장 18cm 이하"),
        FishData("고등어", R.drawable.mackerel2, "금어기: 4.1~6.30 중 1개월", "금지체장: 전장 21cm 이하"),
        FishData("전갱이", R.drawable.pompano,  "금어기: X", "금지체장: X"),
        FishData("붉은 대게", R.drawable.redcrab,  "금어기: 7.10~8.25, 강원 연안자망 6.1~7.10", "금지체장: X"),
        FishData("참홍어", R.drawable.redskate, "금어기: 6.1~7.15", "금지체장: 체반폭 42cm 이하"),
        FishData("도루묵", R.drawable.sandfish, "자율휴어기: 5.1~5.31", "금지체장: 전장 11cm 이하"),
        FishData("대게", R.drawable.snowcrab,  "금어기: 6.1~11.30\n(다만동경131도30분이동수역은 6.1~10.31까지)", "금지체장: 갑장 9cm 이하"),
        FishData("삼치", R.drawable.spanish, "금어기: 5.1~5.31", "금지체장: X"),
        FishData("오징어", R.drawable.squid,  "금어기: 4.1~5.31, 근해채낚기, 연안복합 4.1~4.30", "금지체장: 외투장 15cm 이하") ,
        FishData("멸치", R.drawable.myulchi, "금어기: 기선권현망 4.1~6.30", "대상수역: 연근해"),
        FishData("개조개", R.drawable.keyjo,  "자율휴어기: 7.1~8.31", "대상수역: 부산,경남,전남 연근해"),
        FishData("제주소라", R.drawable.jejusora, "금어기: 전남, 제주 6.1~8.31, 추자도 7.1~9.30, 울릉도,독도 6.1~9.30", "금지체장: 각고 7cm 이하(제주, 울릉 해당, 그 외 5cm 이하"),
        FishData("키조개", R.drawable.keyjogae, "금어기: 7.1~8.31", "금지체장: 각장 18cm 이하"),
        FishData("바지락", R.drawable.bagirak,  "자율휴어기: 8.1~8.31", "대상수역: 경남 연근해, 경남도지사 관리"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottomfrgment_one, container, false)

        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.fishRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = FishAdapter(fishInfo)

        return view
    }

    data class FishData(
        val name: String,
        val imageResId: Int,
        val habitat: String,
        val size: String
    )
}