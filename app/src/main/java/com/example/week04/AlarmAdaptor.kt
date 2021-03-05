package com.example.week04

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.example.week04.databinding.ItemAlarmBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.coroutineContext

class AlarmAdaptor(context: Context, private val alarmList:ArrayList<Alarm>) : BaseAdapter() {

    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    lateinit var binding: ItemAlarmBinding
    val SP_NAME = "my_sp_storage"
    val mContext: Context = context

    // 리스트 size 반환
    override fun getCount(): Int  = alarmList.size

    // 현재 위치의 item 반환
    override fun getItem(position: Int): Any  = alarmList[position]

    // 현재 위치 반환
    override fun getItemId(position: Int): Long  = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        binding = ItemAlarmBinding.inflate(inflater, parent, false)

        binding.tvAlarmTime.text = alarmList[position].time
        binding.tvAlarmDay.text = alarmList[position].day

        binding.cbAlarm.setChecked(alarmList[position].alarmCheck)

        // check box 눌렀을 경우 -> 리스트뷰 맨 위로
        binding.cbAlarm.setOnClickListener(){
            changeStatus(position)
            val temp: Alarm = alarmList[position]
            // temp을 이용해서 코드를 짜보자.
            deleteLv(position)

            // check -> 리스트뷰 맨 위로
            if(temp.alarmCheck){
                alarmList.add(0, temp)
                Log.d("리스트",alarmList[position].time+" "+position)
            }
            // uncheck -> 다시 밑으로
            else{
                alarmList.add(temp)
            }
            writeSharedPreference("key", alarmList)
            notifyDataSetChanged()
        }

        // ellipsis 버튼 눌렀을 경우 -> 팝업메뉴(수정, 삭제)
        binding.ibAction.setOnClickListener {
            val popup = PopupMenu(mContext, it)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {

                    // 알람 수정 기능
                    R.id.modify ->{
                        val cal = Calendar.getInstance()

                        // TimePicker 다이얼로그 확인 누름
                        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                            cal.set(Calendar.HOUR_OF_DAY, hour)
                            cal.set(Calendar.MINUTE, minute)

                            val time:String = SimpleDateFormat("HH:mm").format(cal.time)

                            alarmList[position].time = time
                            writeSharedPreference("key", alarmList)
                            notifyDataSetChanged()
                            Toast.makeText(mContext, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                        }

                        //  클릭시 TimePicker 다이얼로그 표시
                        TimePickerDialog(binding.ibAction.context, AlertDialog.THEME_HOLO_LIGHT, timeSetListener, cal.get(
                                Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }

                    // 알람 삭제 기능
                    R.id.delete ->{
                        deleteLv(position)
                        Toast.makeText(mContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            })
            popup.show()
        }

        return binding.root
    }

    fun changeStatus(position: Int){
        val newState: Boolean = !alarmList[position].alarmCheck
        alarmList[position].alarmCheck = newState
        writeSharedPreference("Alarm", alarmList)
        notifyDataSetChanged()
    }

    // 리스트뷰 item 삭제 & SP에서도 삭제
    fun deleteLv(position:Int){
        alarmList.removeAt(position)
        writeSharedPreference("Alarm", alarmList)
        notifyDataSetChanged()
    }

    // SP write
    fun writeSharedPreference(key:String, list: ArrayList<Alarm>){
        val sp:SharedPreferences = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        var gson = Gson()
        var json:String = gson.toJson(list) // gson을 json 형식으로 변환
        editor.putString("Alarm", json) // json 객체 저장
        Log.d("저장소", "저장 완료.")
        editor.apply()
    }

    // SP read
    fun readSharedPreference(key:String): ArrayList<Alarm>{
        val sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        var gson = Gson()
        var json:String = sp.getString("Alarm","")?: ""
        val type = object : TypeToken<ArrayList<Alarm>>() {}.type
        var obj: ArrayList<Alarm> = gson.fromJson(json, type)
        return obj
    }
}