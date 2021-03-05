package com.example.week04

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.week04.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class Alarm(var time: String, val day: String, var alarmCheck: Boolean)

class MainActivity : AppCompatActivity() {

    var alarmList = arrayListOf<Alarm>()
    val SP_NAME = "my_sp_storage"

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmList = readSharedPreference("Alarm")

        binding.lvMain.adapter = AlarmAdaptor(this, alarmList)

        getTime(binding.tvMainScheduled, this)

        (binding.lvMain.adapter as AlarmAdaptor).notifyDataSetChanged()
    }

    fun getTime(textView: TextView, context: Context) {

        val cal = Calendar.getInstance()

        // TimePicker 다이얼로그 확인 누름 -> item 추가
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            val time:String = SimpleDateFormat("HH:mm").format(cal.time)

            textView.text = time // 다음 알림에 시간 표시
            alarmList.add(Alarm(time, "주중", false))
            writeSharedPreference("key", alarmList)
        }

        // 플로팅버튼 클릭시 TimePicker 다이얼로그 표시
       binding.fbMain.setOnClickListener {
            TimePickerDialog(
                context, AlertDialog.THEME_HOLO_LIGHT, timeSetListener, cal.get(
                    Calendar.HOUR_OF_DAY
                ), cal.get(Calendar.MINUTE), true
            ).show()
        }
    }

    // SP write
    fun writeSharedPreference(key: String, list: ArrayList<Alarm>){
        val sp = getSharedPreferences(SP_NAME, MODE_PRIVATE)
        val editor = sp.edit()
        var gson = Gson()
        var json:String = gson.toJson(list) // gson을 json 형식으로 변환
        editor.putString("Alarm", json) // json 객체 저장
        Log.d("저장소", "저장 완료.")
        editor.apply()
    }

    // SP read
    fun readSharedPreference(key: String): ArrayList<Alarm>{
        val sp = getSharedPreferences(SP_NAME, MODE_PRIVATE)
        var gson = Gson()
        var json:String = sp.getString("Alarm", "")?: ""
        val type = object :TypeToken<ArrayList<Alarm>>() {}.type
        var obj: ArrayList<Alarm> = gson.fromJson(json, type)
        return obj
    }
}