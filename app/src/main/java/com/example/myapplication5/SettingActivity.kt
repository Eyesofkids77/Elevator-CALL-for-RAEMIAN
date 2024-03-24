package com.example.myapplication5

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView

class SettingActivity : AppCompatActivity() {

    // 각 위젯에 대한 참조 변수 선언
    private lateinit var localServerEdit: EditText
    private lateinit var yourFloorEdit: EditText
    private lateinit var updateIntervalEdit: EditText
    private lateinit var elevatorNumberEdit: EditText
    private lateinit var switchButton: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)

        // 각 위젯에 대한 참조 변수 초기화
        localServerEdit = findViewById(R.id.localserverEdit)
        yourFloorEdit = findViewById(R.id.yourfloorEdit)
        updateIntervalEdit = findViewById(R.id.updateintervalEdit)
        elevatorNumberEdit = findViewById(R.id.elevatornumberEdit)
        switchButton = findViewById(R.id.switchButton)

        // 저장된 설정값을 가져와 화면에 업데이트
        updateSettingValues()

        // 세이브 버튼에 대한 클릭 리스너 설정
        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            // 사용자의 입력 값을 가져와서 SharedPreferences에 저장
            saveSettings()

            // 로컬 서버 주소를 로그로 출력합니다.
            val localServerAddress = localServerEdit.text.toString()
            Log.d(ContentValues.TAG, "Local Server Address: $localServerAddress")

            // 메인 액티비티로 로컬 서버 주소를 전달합니다.
            val intent = Intent(this@SettingActivity, MainActivity::class.java)
            intent.putExtra("localServerAddress", localServerAddress)
            startActivity(intent)
        }

        // 백버튼
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // MainActivity로 이동하는 인텐트 생성
            val intent = Intent(this, MainActivity::class.java)
            // 새로운 액티비티 시작
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 화면에 표시될 때마다 최신 설정값으로 업데이트
        updateSettingValues()
    }

    //  후원링크
    fun onDonationClicked(view: View) {
        val url = "https://qr.kakaopay.com/281006011184199780005254" // 웹페이지 URL을 여기에 입력하세요.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }



    private fun updateSettingValues() {
        val sharedPreferences = getSharedPreferences("MySettings", Context.MODE_PRIVATE)
        localServerEdit.setText(
            sharedPreferences.getString(
                "localServerAddress",
                "http://10.2.1.11"
            )
        )
        yourFloorEdit.setText(
            sharedPreferences.getInt("yourFloorNumber", 24).toString()
        ) // getInt를 사용하여 값을 가져옴
        updateIntervalEdit.setText(sharedPreferences.getInt("updateInterval", 1000).toString())
        elevatorNumberEdit.setText(
            sharedPreferences.getInt("elevatorNumber", 1).toString()
        ) // getInt를 사용하여 값을 가져옴
        switchButton.isChecked = sharedPreferences.getBoolean("isAlarmEnabled", true)

    }

    private fun saveSettings() {
        val sharedPreferences = getSharedPreferences("MySettings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("localServerAddress", localServerEdit.text.toString())
        editor.putInt(
            "yourFloorNumber",
            yourFloorEdit.text.toString().toIntOrNull() ?: 24
        ) // 기본값은 24
        editor.putInt("updateInterval", updateIntervalEdit.text.toString().toIntOrNull() ?: 1000)
        editor.putInt(
            "elevatorNumber",
            elevatorNumberEdit.text.toString().toIntOrNull() ?: 1
        ) // 기본값은 1
        editor.putBoolean("isAlarmEnabled", switchButton.isChecked)
        editor.apply()
    }
}