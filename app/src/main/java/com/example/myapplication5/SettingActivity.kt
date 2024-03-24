package com.example.myapplication5

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)

        // XML 레이아웃 파일에서 설정한 ID를 가져올 수 있습니다.
    //    val testView = findViewById<View>(R.id.test)
    //    val testId = testView.id
    //    println(testId)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // MainActivity로 이동하는 인텐트 생성
            val intent = Intent(this, MainActivity::class.java)
            // 새로운 액티비티 시작
            startActivity(intent)
        }

    }
}