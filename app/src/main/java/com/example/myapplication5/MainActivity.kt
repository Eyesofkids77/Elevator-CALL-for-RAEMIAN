package com.example.myapplication5

import android.animation.AnimatorSet
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.IOException
import android.media.AudioManager
import android.media.SoundPool
import android.media.MediaPlayer
import android.view.View
import android.widget.ImageView

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent

import android.graphics.Typeface
import android.view.Gravity
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import android.widget.SeekBar


class MainActivity : AppCompatActivity() {
    private var wayTextView: TextView? = null
    private var wallpadFloorTextView: TextView? = null
    private var stateTextView: TextView? = null
    private var statusTextView: TextView? = null
    private var floorTextView: TextView? = null
    private var dongTextView: TextView? = null
    private var hoTextView: TextView? = null
    private var doorStateTextView: TextView? = null
    private lateinit var callButton: Button

    private lateinit var doorImageView1: ImageView
    private lateinit var doorImageView2: ImageView
    private lateinit var lineImageView: ImageView

    private lateinit var statusImageView: ImageView
    private lateinit var statusImageView1: ImageView

    // DoorState 열거형 정의
    enum class DoorState {
        OPEN, CLOSE
    }

    // MainActivity 클래스 내부에 DoorState 열거형과 관련된 변수 추가
    private var doorState: DoorState = DoorState.CLOSE // 초기 문 상태는 닫혀있음
    private var previousDoorState: DoorState = DoorState.CLOSE // 이전 도어 상태 변수 추가


    // State 열거형 정의
    enum class State {
        NORMAL,
        ABNORMAL
    }

    // 운행상태관련 변수 추가
    private var previousState: State? = null

    // 정상운행 상태에 대한 애니메이션 처리 함수
    private fun animateNormalState() {
        rotateAndChangeBrightness(statusImageView1)
        rotateAndChangeBrightness(statusImageView)
    }

    // 정상운행 상태가 아닐 때의 애니메이션 처리 함수
    private fun animateOtherState() {
        // StatusImageView1의 애니메이션을 제거하고 숨깁니다.
        statusImageView1.clearAnimation()
        statusImageView1.visibility = View.GONE
    }

    // SoundPool 초기화
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    // MediaPlayer 초기화
    private var mediaPlayer: MediaPlayer? = null

    private var isFloorDetectionEnabled = false

    private lateinit var powerManager: PowerManager

    // 콜 버튼 누른 순간의 층 값을 저장할 변수 선언
    private var previousFloor: Int = 1

    // TAG 변수 선언
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TextViews
        wayTextView = findViewById(R.id.wayTextView)
        wallpadFloorTextView = findViewById(R.id.wallpadFloorTextView)
        stateTextView = findViewById(R.id.stateTextView)
        statusTextView = findViewById(R.id.statusTextView)
        floorTextView = findViewById(R.id.floorTextView)
        dongTextView = findViewById(R.id.dongTextView)
        hoTextView = findViewById(R.id.hoTextView)
        doorStateTextView = findViewById(R.id.doorStateTextView)

        doorImageView1 = findViewById(R.id.doorImageView1)
        doorImageView2 = findViewById(R.id.doorImageView2)
        lineImageView = findViewById(R.id.lineImageView)

        statusImageView1 = findViewById(R.id.StatusImageView1)
        statusImageView = findViewById(R.id.StatusImageView)

        // Initialize SeekBar
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val slideText = findViewById<TextView>(R.id.slidetext) // slidetext TextView 참조
        val comingText = findViewById<TextView>(R.id.coming) // coming TextView 참조
        val progressImageView = findViewById<ImageView>(R.id.progressbar_call)


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // SeekBar의 상태 변화 감지
                if (progress >= 80) {
                    // SeekBar의 Thumb이 80% 이상으로 이동하면 "Coming..." 텍스트 애니메이션 시작
                    callElevator()
                    animateComingText()
                    // 시크바의 Thumb 위치를 100으로 고정
                    seekBar?.progress = 100
                    slideText.visibility = View.INVISIBLE // slidetext TextView 숨기기
                    animateProgressBarVisibility(progressImageView, true)
                } else {
                    // slideText.visibility = View.VISIBLE // slidetext TextView 보이기
                    animateProgressBarVisibility(progressImageView, false)
                }
            }


            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // SeekBar 터치 시작시 동작
                slideText.visibility = View.INVISIBLE // slidetext TextView 숨기기
                stopComingTextAnimation()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // SeekBar 터치 종료시 동작
                if (seekBar?.progress == 100) {
                    // 프로그래스가 100에 도달하면 터치를 막음

                    seekBar.setOnTouchListener { _, _ -> true }

                } else if (seekBar?.progress!! < 80) {
                    // SeekBar의 Thumb이 80% 미만으로 이동한 경우 텀을 0으로 되돌립니다.
                    seekBar.progress = 0
                    // 터치를 풀어줌
                    seekBar.setOnTouchListener(null)
                }
                slideText.visibility = if (seekBar.progress < 100) View.VISIBLE else View.INVISIBLE // slidetext TextView 다시 보이거나 숨기기
                comingText.visibility = View.INVISIBLE
            }
        })

            // ImageView에 클릭 리스너 추가
            val settingIconImageView = findViewById<ImageView>(R.id.setting_iconImageView)
            settingIconImageView.setOnClickListener {
                // 클릭 이벤트 발생 시 설정 페이지로 이동하는 코드 작성
                val intent = Intent(this@MainActivity, SettingActivity::class.java) // SettingActivity는 설정 페이지의 액티비티 클래스명입니다.
                startActivity(intent)
            }

        // 초기 상태에서 문과 선을 정가운데에 배치
        alignDoorsAndLine()

        // 여기서 doorState를 초기화합니다.
        doorState = DoorState.CLOSE

        // 폰트를 로드하고 TextView에 적용
        //val customFont = Typeface.createFromAsset(assets, "impact.ttf")
        //floorTextView?.typeface = customFont

        // Initialize Call Button
        callButton = findViewById(R.id.callButton)
        callButton.setOnClickListener {
            callElevator()
        }

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        // SoundPool 초기화
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool.load(this, R.raw.sound_file, 1)

        fetchData()
    }

    private fun alignDoorsAndLine() {
        // 문과 선을 정가운데에 배치
        doorImageView1.x = (lineImageView.x - doorImageView1.width).toFloat()
        doorImageView2.x = lineImageView.x + lineImageView.width
    }


    private var comingTextAnimation: Animation? = null // 애니메이션 객체를 저장할 변수

    private fun animateComingText() {
        val comingText = findViewById<TextView>(R.id.coming)
        val textArray = arrayOf("Coming", "Coming.", "Coming..","Coming...") // 변경될 텍스트 배열
        var currentIndex = 0 // 텍스트 배열의 현재 인덱스

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                super.applyTransformation(interpolatedTime, t)
                // 애니메이션 진행 중에 텍스트 변경
                if (interpolatedTime >= 1) {
                    // 애니메이션 한 사이클이 끝날 때마다 다음 텍스트로 변경
                    comingText.text = textArray[currentIndex]
                    currentIndex = (currentIndex + 1) % textArray.size // 다음 인덱스로 변경
                }
            }
        }

        animation.duration = 500 // 애니메이션 한 사이클의 지속 시간을 0.5초로 설정
        animation.repeatCount = Animation.INFINITE // 무한 반복

        comingText.startAnimation(animation) // 애니메이션 시작
    }


    private fun stopComingTextAnimation() {
        val comingText = findViewById<TextView>(R.id.coming)
        comingText.clearAnimation() // comingText 뷰에 적용된 모든 애니메이션을 제거합니다.
    }

    // Call시 프로그래스바 색상 변경
    private fun animateProgressBarVisibility(imageView: ImageView, visible: Boolean) {
        val finalAlpha = if (visible) 0.8f else 0.1f
        val finalVisibility = if (visible) View.VISIBLE else View.GONE

        // 투명도 애니메이션 설정
        val alphaAnim = ObjectAnimator.ofFloat(imageView, "alpha", imageView.alpha, finalAlpha)
        alphaAnim.duration = 600 // 애니메이션 지속 시간 설정 (3초)
        alphaAnim.repeatCount = ObjectAnimator.INFINITE // 무한 반복
        alphaAnim.repeatMode = ObjectAnimator.REVERSE // 반복 모드 설정 (순방향 → 역방향)

        // Visibility 설정
        imageView.visibility = finalVisibility

        // 애니메이션 시작
        alphaAnim.start()
    }



    // updateDoorAnimation 함수 내부에 도어 상태 업데이트 로직 추가
    private fun updateDoorAnimation(newDoorState: DoorState) {
        if (previousDoorState != newDoorState) {
            // 이전 도어 상태와 현재 도어 상태가 다를 때만 애니메이션을 처리합니다.
            if (newDoorState == DoorState.OPEN) {
                // 문이 열린 상태일 때의 애니메이션 처리
                animateDoorOpen()
            } else {
                // 문이 닫힌 상태일 때의 애니메이션 처리
                animateDoorClose()
            }
            previousDoorState = newDoorState // 이전 도어 상태 업데이트
        }
    }

    // 문이 열린 상태일 때의 애니메이션 처리 함수
    private fun animateDoorOpen() {
        // 문이 열렸을 때의 애니메이션을 구현합니다.
        doorImageView1.translationX = 0f // 초기 위치
        doorImageView2.translationX = doorImageView2.x // 초기 위치

        val anim = ObjectAnimator.ofFloat(
            doorImageView1,
            "translationX",
            0f,
            -doorImageView1.width.toFloat()
        )
        anim.duration = 1500 // 애니메이션 지속 시간을 설정합니다. (1000밀리초 = 1초)
        anim.start()

        val anim2 = ObjectAnimator.ofFloat(
            doorImageView2,
            "translationX",
            0f,
            doorImageView2.width.toFloat()
        )
        anim2.duration = 1500
        anim2.start()
    }

    // 문이 닫힌 상태일 때의 애니메이션 처리 함수
    private fun animateDoorClose() {
        // 문이 닫혔을 때의 애니메이션을 구현합니다.
        doorImageView1.translationX = -doorImageView1.width.toFloat() // 초기 위치
        doorImageView2.translationX = doorImageView2.width.toFloat() // 초기 위치

        val anim = ObjectAnimator.ofFloat(
            doorImageView1,
            "translationX",
            -doorImageView1.width.toFloat(),
            0f
        )
        anim.duration = 1500
        anim.start()

        val anim2 = ObjectAnimator.ofFloat(
            doorImageView2,
            "translationX",
            doorImageView2.width.toFloat(),
            0f
        )
        anim2.duration = 1500
        anim2.start()
    }

    // updateStateAnimation 함수 내부에 상태 업데이트 로직 추가
    private fun updateStateAnimation(newState: State) {
        if (previousState != newState) {
            // 이전 상태와 현재 상태가 다를 때만 애니메이션을 처리합니다.
            if (newState == State.NORMAL) {
                // 정상 상태일 때의 애니메이션 처리
                animateNormalState()
            } else {
                // 정상 상태가 아닐 때의 애니메이션 처리
                animateOtherState()
            }
            previousState = newState // 이전 상태 업데이트
        }
    }

    // 밝기 조절 애니메이션 함수
    private fun rotateAndChangeBrightness(imageView: ImageView) {
        // 이미지뷰 회전 애니메이션 추가
        val rotationAnim = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f)
        rotationAnim.duration = 5000 // 애니메이션 지속 시간 설정 (1000밀리초 = 1초)
        rotationAnim.repeatCount = ValueAnimator.INFINITE // 무한대로 반복

        // 이미지뷰 밝기 조절 애니메이션 추가
        val brightnessAnim = ObjectAnimator.ofFloat(imageView, "alpha", 0.2f, 1.0f, 0.2f)
        brightnessAnim.duration = 4000 // 애니메이션 지속 시간 설정 (1000밀리초 = 1초)
        brightnessAnim.repeatCount = ValueAnimator.INFINITE // 무한대로 반복

        // 애니메이션 세트 생성
        val animSet = AnimatorSet()
        animSet.interpolator = LinearInterpolator()
        animSet.playTogether(rotationAnim, brightnessAnim) // 회전 애니메이션과 밝기 조절 애니메이션 동시에 실행
        animSet.start() // 애니메이션 세트 시작
    }



    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    // 화면의 상태를 체크하여 데이터를 가져옵니다.
                    if (!isScreenOff()) {
                        Log.d(TAG, "Fetching data from URL...")
                        val responseData =
                            sendRequest("http://10.2.1.11/seoulapp/ezon_v2/common/elevator.do?method=ezon_v2.common.elevator.StateXML&hogi1=8")
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "Data fetched successfully.")

                            parseData(responseData) // 파싱 결과를 사용하여 애니메이션 업데이트
                        }
                    } else {
                        // 화면이 꺼져있을 때는 데이터를 가져오지 않습니다.
                        Log.d(TAG, "Screen is off. Skipping data fetch.")
                        delay(1000) // 1초 대기 후 다시 확인
                        continue // 다음 반복으로 넘어감
                    }
                    // Wait for 1 second before fetching data again
                    delay(1000)
                } catch (e: IOException) {
                    Log.e(TAG, "Error fetching data", e)
                }
            }
        }
    }


    private fun callElevator() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response =
                    sendRequest("http://10.2.1.11/seoulapp/ezon_v2/common/elevator.do?method=ezon_v2.common.elevator.CallXML&flag=down&hogi=8")
                withContext(Dispatchers.Main) {
                    handleResponse(response)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error calling elevator", e)
            }
        }
    }

    private fun sendRequest(requestUrl: String): String {
        val url = URL(requestUrl)
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val inputStream = urlConnection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream, "EUC-KR")) // EUC-KR 인코딩 사용
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            return response.toString()
        } finally {
            urlConnection.disconnect()
        }
    }

    private fun handleResponse(response: String) {
        val rootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(InputSource(StringReader(response))).documentElement
        val elevatorCallFlag =
            rootElement.getElementsByTagName("elevatorCallFlag").item(0).textContent
        if (elevatorCallFlag.equals("true", ignoreCase = true)) {
            showToast("엘리베이터를 호출하였습니다.", 1000)
            isFloorDetectionEnabled = true
            previousFloor = floorTextView!!.text.toString().toIntOrNull() ?: 1 // previousFloor 업데이트

            Log.d(TAG, "Previous Floor updated: $previousFloor") // 로그로 이전 층 값 출력
        }
    }

    private fun showToast(message: String, duration: Long) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()
        GlobalScope.launch {
            delay(duration) // 1초 또는 2초 동안 토스트 메시지 노출
            toast.cancel()
        }
    }


    private var currentState: State = State.ABNORMAL

    private fun parseData(responseData: String) {
        try {
            val rootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(InputSource(StringReader(responseData))).documentElement
            val itemNode = rootElement.getElementsByTagName("item").item(0) as Element
            val way = itemNode.getElementsByTagName("way").item(0).textContent
            val wallpadFloor =
                itemNode.getElementsByTagName("wallpadFloor").item(0).textContent.toInt(16)
                    .toString()
            val state = itemNode.getElementsByTagName("state").item(0).textContent
            val status = itemNode.getElementsByTagName("status").item(0).textContent
            val floor = itemNode.getElementsByTagName("floor").item(0).textContent
            val dong = itemNode.getElementsByTagName("dong").item(0).textContent
            val ho = itemNode.getElementsByTagName("ho").item(0).textContent
            val doorStateString = itemNode.getElementsByTagName("doorState").item(0).textContent

            wayTextView!!.text = "way: $way"
            wallpadFloorTextView!!.text = "Wallpad Floor: $wallpadFloor"
            stateTextView!!.text = "State: $state"
            statusTextView!!.text = "Status: $status"
            floorTextView!!.text = "$floor" // 층수만 표시
            dongTextView!!.text = "Dong: $dong"
            hoTextView!!.text = "Ho: $ho"
            doorStateTextView!!.text = "Door : $doorStateString"

            val seekBar = findViewById<SeekBar>(R.id.seekBar)
            seekBar.max = 100 // SeekBar의 최대값을 100으로 설정
            val slideText = findViewById<TextView>(R.id.slidetext) // slidetext TextView 참조
            val comingText = findViewById<TextView>(R.id.coming)

            // 도착 검사 및 알림
            if (isFloorDetectionEnabled) {
                val floorInt = floor.toInt()
                if (previousFloor > 18) {
                    if (floorInt == 18) {
                        Log.d(TAG, "Elevator has arrived: previousFloor > 18")
                        showToast("엘리베이터가 도착하였습니다.", 3000)
                        playSound()
                        isFloorDetectionEnabled = false // 다시 floor 감지 비활성화

                        // 시크바를 초기 상태로 되돌립니다.
                        seekBar.progress = 0
                        stopComingTextAnimation()
                        slideText.visibility = View.VISIBLE // slidetext TextView 보이기
                        comingText.visibility = View.INVISIBLE // coming TextView 숨기기
                        // 터치를 풀어줌
                        seekBar.setOnTouchListener(null)

                    }
                } else if (floorInt == 18 && way == "down") {
                    Log.d(TAG, "Elevator has arrived: previousFloor < 18 && way == 'down'")
                    showToast("엘리베이터가 도착하였습니다.", 3000)
                    playSound()
                    isFloorDetectionEnabled = false // 다시 floor 감지 비활성화

                    // 시크바를 초기 상태로 되돌립니다.
                    seekBar.progress = 0
                    stopComingTextAnimation()
                    slideText.visibility = View.VISIBLE // slidetext TextView 보이기
                    comingText.visibility = View.INVISIBLE // coming TextView 숨기기
                    // 터치를 풀어줌
                    seekBar.setOnTouchListener(null)

                }
            }

            // way 값에 따라 이미지를 표시하거나 숨김
            val upImageView = findViewById<ImageView>(R.id.upImageView)
            val downImageView = findViewById<ImageView>(R.id.downImageView)

            when (way) {
                "up" -> {
                    upImageView.visibility = View.VISIBLE
                    downImageView.visibility = View.GONE
                    blinkImageView(upImageView) // 깜빡이는 효과 적용
                    stopBlink(downImageView) // downImageView의 깜빡임 애니메이션 중지
                }

                "down" -> {
                    upImageView.visibility = View.GONE
                    downImageView.visibility = View.VISIBLE
                    blinkImageView(downImageView) // 깜빡이는 효과 적용
                    stopBlink(upImageView) // upImageView의 깜빡임 애니메이션 중지
                }

                else -> {
                    upImageView.visibility = View.GONE
                    downImageView.visibility = View.GONE
                    stopBlink(upImageView) // upImageView의 깜빡임 애니메이션 중지
                    stopBlink(downImageView) // downImageView의 깜빡임 애니메이션 중지
                }
            }

            // 도어 상태에 따른 애니메이션 업데이트
            val newDoorState = if (doorStateString.equals("open", ignoreCase = true)) {
                DoorState.OPEN
            } else {
                DoorState.CLOSE
            }
            updateDoorAnimation(newDoorState)

            // 데이터 파싱 및 상태 확인
            val newState = if (status == "정상운행") State.NORMAL else State.ABNORMAL

            // 상태가 변경되지 않았을 때는 이전 상태를 유지
            if (newState != currentState) {
                updateStateAnimation(newState)
                currentState = newState // 상태 업데이트
            } else {
                // 상태가 변경되지 않은 경우에는 이전 상태를 유지하도록 처리
                updateStateAnimation(currentState)
            }


        } catch (e: Exception) {
            Log.e(TAG, "Error parsing XML", e)
        }
    }

    // 화면이 꺼져있는지 확인하는 함수
    private fun isScreenOff(): Boolean {
        return !powerManager.isInteractive
    }

    // 사운드 재생 함수
    private fun playSound() {
        try {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.sound_file)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound", e)
        }
    }

    // 이미지뷰에 깜빡이는 효과를 줄 함수
    private fun blinkImageView(imageView: ImageView) {
        val anim = AlphaAnimation(0.0f, 1.0f) // 투명도를 0에서 1로 변화시키는 애니메이션
        anim.duration = 500 // 애니메이션 지속 시간 (500밀리초 = 0.5초)
        anim.repeatMode = Animation.REVERSE // 애니메이션 반복 모드를 설정하여 깜빡이는 효과를 만듭니다.
        anim.repeatCount = Animation.INFINITE // 무한 반복
        imageView.startAnimation(anim) // 애니메이션을 이미지뷰에 적용
    }

    // 이미지뷰 깜빡임 애니메이션 중지 함수
    private fun stopBlink(imageView: ImageView) {
        imageView.clearAnimation()
    }

}
