package com.byunggil.project_team5

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private val database = Firebase.database
    // FirebaseDatabase 클래스의 객체 database 생성
    private var myRef = database.getReference("User").child(Firebase.auth.currentUser!!.uid)
    // Firebase Realtime Database 서버에 저장된 경로 중, User/UID를 참조하는 변수 생성
    // 만약, 아직 그 경로가 존재하지 않는다면, myRef로 데이터 저장 시, User/UID 라는 디렉토리 생성 후,
    // 그 밑으로 데이터를 저장

    // 실제 시간을 관리하는 Calender 클래스로부터 현재 시각을 받아 옴
    private val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
    private var year = calendar.get(Calendar.YEAR)
    private var month = calendar.get(Calendar.MONTH) + 1 // Month 값은 0부터 시작
    private var date = calendar.get(Calendar.DATE)
    private var hour = calendar.get(Calendar.HOUR)
    private var minute = calendar.get(Calendar.MINUTE)

    // topAppBar에 날짜를 표시하기 위한 변수 설정
    private val todayYear = year
    private val todayMonth = month
    private val todayDate = date

    // 인증 모드 설정
    private var modeStatus = false

    // 인증 모드 ON일 시, 사용자가 설정한 금액과 누적 금액
    private var amount = 0
    private var totalAmount = 0


    // 사용자가 앱 실행 시, onCreate 함수 호출
    @SuppressLint("SetTextI18n", "UseSwitchCompatOrMaterialCode", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv = findViewById<RecyclerView>(R.id.recyclerView)

        val settingsbtn = findViewById<ImageButton>(R.id.settings)
        settingsbtn.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        // MainActivity의 RecyclerView를 rv로 참조
        val items = mutableListOf<DataModel>()
        val rvAdapter = RvAdapter(items)
        // 만들어둔 RvAdapter 클래스 객체 생성

        rv.adapter = rvAdapter
        // rv(MainActivity의 RecyclerView)의 Adapter에 생성한 rvAdapter(RvAdapter 클래스 객체)를 연결
        rv.layoutManager = LinearLayoutManager(this)
        // rv의 layoutManager를 LinearLayoutManager로 설정
        rvAdapter.itemClick = object : RvAdapter.ItemClick {
            override fun onClick(view: View, position: Int, id: String?) {
                id?.let {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("MEMO_ID", it)
                    intent.putExtra("SELECTED_DATE", "${year}-${month}-${date}")
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(baseContext, "ID is null", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 앱 실행 시, 데이터를 불러 옴
        myRef.child("User_Memo/${year}-${month}-${date}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    items.clear()
                    for (data in snapshot.children) {
                        val model = data.getValue(DataModel::class.java)
                        model?.let {
                            it.id = data.key
                            items.add(it)
                        }
                    }
                    rvAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        // topAppbar에 날짜 표시 및 날짜 선택 기능 구현
        val date_indicator = findViewById<Button>(R.id.date_indicator)
        if (todayYear == year && todayMonth == month && todayDate == date) {
            date_indicator.text = "Today"
        }
        else {
            date_indicator.text = "${year}-${month}-${date}"
        }
        date_indicator.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(p0: DatePicker?, t1: Int, t2: Int, t3: Int) {
                    year = t1
                    month = t2
                    date = t3
                    if (todayYear == year && todayMonth == month && todayDate == date) {
                        date_indicator.text = "Today"
                    }
                    else {
                        date_indicator.text = "${year}-${month}-${date}"
                    }
                    myRef.child("User_Memo/${year}-${month}-${date}")
                        .addListenerForSingleValueEvent(object : ValueEventListener {

                            @SuppressLint("NotifyDataSetChanged")
                            override fun onDataChange(snapshot: DataSnapshot) {
                                items.clear()
                                for (data in snapshot.children) {
                                    items.add(data.getValue(DataModel::class.java)!!)
                                }
                                rvAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO()
                            }
                        })
                }
            }, year, month, date)
            datePickerDialog.show()
        }

        // 누적 금액 정보 표시 버튼 동작 설정
        val amountInfo_Btn = findViewById<FloatingActionButton>(R.id.amountInfo_Btn)
        amountInfo_Btn.setOnClickListener {
            calculateTotalAmount { totalAmount ->
                showAmountDialog(totalAmount)
            }
        }

        // BottomAppBar의 FAB 동작 설정
        val add_Btn = findViewById<FloatingActionButton>(R.id.add_Btn)
        add_Btn.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val builder = AlertDialog.Builder(this).setView(dialogView)
            val dialog = builder.create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            var startYear = 0
            var startMonth = 0
            var startDate = 0
            var endYear = 0
            var endMonth = 0
            var endDate = 0
            var startHour = 0
            var startMinute = 0
            var endHour = 0
            var endMinute = 0
            modeStatus = false
            amount = 0

            // 기간 설정
            val setPeriod = dialogView.findViewById<TextView>(R.id.period)
            setPeriod.setOnClickListener {
                val periodSetDialogView = LayoutInflater.from(this).inflate(R.layout.set_period_dialog, null)
                // set_period_dialog.xml 파일을 뷰로 변환하여 변수 periodSetDialogView로 저장
                val builder2 = AlertDialog.Builder(this).setView(periodSetDialogView)
                // periodSetDialogView를 기반으로 AlertDialog를 구성하는 데 사용할 Builder 객체 생성
                val periodSetDialog = builder2.create()
                //  최종적으로 생성된 AlertDialog 객체

                periodSetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                periodSetDialog.show()

                // 시작일 설정
                val setStartDate = periodSetDialogView.findViewById<TextView>(R.id.start_date)
                setStartDate.text = "${year}-${month}-${date}"
                setStartDate.setOnClickListener {
                    val datePickerDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(p0: DatePicker?, t1: Int, t2: Int, t3: Int) {
                            startYear = t1
                            startMonth = t2
                            startDate = t3
                            setStartDate.text = "${startYear}-${startMonth}-${startDate}"
                        }
                    }, year, month, date)
                    datePickerDialog.show()
                }

                // 종료일 설정
                val setEndDate = periodSetDialogView.findViewById<TextView>(R.id.end_date)
                setEndDate.text = "${year}-${month}-${date}"
                setEndDate.setOnClickListener {
                    val datePickerDialog = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(p0: DatePicker?, t1: Int, t2: Int, t3: Int) {
                            endYear = t1
                            endMonth = t2
                            endDate = t3
                            setEndDate.text = "${endYear}-${endMonth}-${endDate}"
                        }
                    }, year, month, date)
                    datePickerDialog.show()
                }

                // 저장 버튼 설정
                val savePeriod_Btn = periodSetDialog.findViewById<ImageButton>(R.id.savePeriod_Btn)
                savePeriod_Btn?.setOnClickListener {
                    if (startYear == 0 || startMonth == 0 || startDate == 0) {
                        startYear = year
                        startMonth = month
                        startDate = date
                    }
                    if (endYear == 0 || endMonth == 0 || endDate == 0) {
                        endYear = year
                        endMonth = month
                        endDate = date
                    }
                    setPeriod.text="${startYear}-${startMonth}-${startDate} ~ ${endYear}-${endMonth}-${endDate}"
                    periodSetDialog.dismiss()
                }
            }
            // 시작 시간 설정
            val setStartTime_Btn = dialogView.findViewById<ImageButton>(R.id.setStartTime_Btn)
            setStartTime_Btn.setOnClickListener {
                hour = calendar.get(Calendar.HOUR)
                minute = calendar.get(Calendar.MINUTE)

                val timePickerListener = object : TimePickerDialog.OnTimeSetListener {
                    override fun onTimeSet(p: TimePicker?, t1: Int, t2: Int) {
                        val start_time = dialogView.findViewById<TextView>(R.id.start_Time)
                        start_time.setText("${t1}시 ${t2}분")
                        startHour = t1
                        startMinute = t2
                    }
                }

                val timePickerDialog = TimePickerDialog(this@MainActivity, timePickerListener,
                    hour, minute, false)
                timePickerDialog.show()
            }

            // 종료 시간 설정
            val setEndTime_Btn = dialogView.findViewById<ImageButton>(R.id.setEndTime_Btn)
            setEndTime_Btn.setOnClickListener {
                hour = calendar.get(Calendar.HOUR)
                minute = calendar.get(Calendar.MINUTE)

                val timePickerListener = object : TimePickerDialog.OnTimeSetListener {
                    override fun onTimeSet(p: TimePicker?, t1: Int, t2: Int) {
                        val end_Time = dialogView.findViewById<TextView>(R.id.end_Time)
                        end_Time.setText("${t1}시 ${t2}분")
                        endHour = t1
                        endMinute = t2
                    }
                }

                val timePickerDialog = TimePickerDialog(this@MainActivity, timePickerListener,
                    hour, minute, false)
                timePickerDialog.show()
            }

            // 토글 버튼 설정
            val switch_Btn = dialogView.findViewById<Switch>(R.id.switch_Btn)
            val amountSettingContainer = dialogView.findViewById<CardView>(R.id.amountSettingContainer)
            switch_Btn.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // ON 상태
                    modeStatus = true
                    Toast.makeText(this, "Switched ON", Toast.LENGTH_SHORT).show()
                    amountSettingContainer.visibility = View.VISIBLE
                } else {
                    // OFF 상태
                    modeStatus = false
                    Toast.makeText(this, "Switched OFF", Toast.LENGTH_SHORT).show()
                    amountSettingContainer.visibility = View.GONE
                }
            }

            // 저장 버튼 설정
            val saveList_Btn = dialogView.findViewById<ImageButton>(R.id.saveList_Btn)
            saveList_Btn.setOnClickListener {
                val To_Do_list = dialogView.findViewById<EditText>(R.id.To_Do_List).text.toString()
                if (startYear == 0 || startMonth == 0 || startDate == 0) {
                    startYear = year
                    startMonth = month
                    startDate = date
                }
                if (endYear == 0 || endMonth == 0 || endDate == 0) {
                    endYear = year
                    endMonth = month
                    endDate = date
                }

                val startCalendar = Calendar.getInstance()
                startCalendar.set(startYear, startMonth - 1, startDate)
                val endCalendar = Calendar.getInstance()
                endCalendar.set(endYear, endMonth - 1, endDate)

                // 모든 날짜를 리스트에 추가
                val datesToCheck = mutableListOf<Calendar>()
                while (!startCalendar.after(endCalendar)) {
                    datesToCheck.add(startCalendar.clone() as Calendar)
                    startCalendar.add(Calendar.DATE, 1)
                }

                if (To_Do_list.isEmpty()) {
                    Toast.makeText(this, "내용을 입력해 주세요.", Toast.LENGTH_LONG).show()
                }
                else {
                    if (modeStatus) {
                        val amountValue = dialogView.findViewById<EditText>(R.id.amountInput).text.toString()
                        if (amountValue.isEmpty()) {
                            Toast.makeText(this, "금액을 입력해 주세요.", Toast.LENGTH_LONG).show()
                        }
                        else {
                            amount = amountValue.toInt()
                            if (amount >= 500) {
                                val dataModel = DataModel(
                                    toDoList = To_Do_list,
                                    startYear = startYear,
                                    startMonth = startMonth,
                                    startDate = startDate,
                                    endYear = endYear,
                                    endMonth = endMonth,
                                    endDate = endDate,
                                    startHour = startHour,
                                    startMinute = startMinute,
                                    endHour = endHour,
                                    endMinute = endMinute,
                                    completed = false, // 기본값이 필요하다면 여기서 명시적으로 설정
                                    modeStatus = modeStatus,
                                    amount = amount
                                )

                                checkDatesForDuplicates(datesToCheck, dataModel, To_Do_list)
                                dialog.dismiss() // 다이얼로그 닫기
                            }
                            else {
                                Toast.makeText(this, "최소 금액은 500원 입니다.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    else {
                        val dataModel = DataModel(
                            toDoList = To_Do_list,
                            startYear = startYear,
                            startMonth = startMonth,
                            startDate = startDate,
                            endYear = endYear,
                            endMonth = endMonth,
                            endDate = endDate,
                            startHour = startHour,
                            startMinute = startMinute,
                            endHour = endHour,
                            endMinute = endMinute,
                            completed = false, // 기본값이 필요하다면 여기서 명시적으로 설정
                            modeStatus = modeStatus,
                            amount = amount
                        )

                        checkDatesForDuplicates(datesToCheck, dataModel, To_Do_list)
                        dialog.dismiss() // 다이얼로그 닫기
                    }
                }

                // 새로운 항목 추가 시, 자동 실행되는 부분
                myRef.child("User_Memo/${year}-${month}-${date}")
                    .addValueEventListener(object : ValueEventListener{

                        @SuppressLint("NotifyDataSetChanged")
                        override fun onDataChange(snapshot: DataSnapshot) {
                            items.clear()
                            for (data in snapshot.children) {
                                items.add(data.getValue(DataModel::class.java)!!)
                            }
                            rvAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO()
                        }

                    })
            }
        }

        // topAppbar의 왼쪽 버튼 동작 구현
        val previousDay_Btn = findViewById<ImageButton>(R.id.previousDay_Btn)
        previousDay_Btn.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH) + 1
            date = calendar.get(Calendar.DATE)

            if (todayYear == year && todayMonth == month && todayDate == date) {
                date_indicator.text = "Today"
            }
            else {
                date_indicator.text = "${year}-${month}-${date}"
            }

            myRef.child("User_Memo/${year}-${month}-${date}")
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        items.clear()
                        for (data in snapshot.children) {
                            val model = data.getValue(DataModel::class.java)
                            model?.let {
                                it.id = data.key
                                items.add(it)
                            }
                        }
                        rvAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO()
                    }
                })

        }

        // topAppbar의 오른쪽 버튼 동작 구현
        val nextDay_Btn = findViewById<ImageButton>(R.id.nextDay_Btn)
        nextDay_Btn.setOnClickListener {
            calendar.add(Calendar.DATE, +1)
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH) + 1
            date = calendar.get(Calendar.DATE)

            if (todayYear == year && todayMonth == month && todayDate == date) {
                date_indicator.text = "Today"
            }
            else {
                date_indicator.text = "${year}-${month}-${date}"
            }

            myRef.child("User_Memo/${year}-${month}-${date}")
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        items.clear()
                        for (data in snapshot.children) {
                            val model = data.getValue(DataModel::class.java)
                            model?.let {
                                it.id = data.key
                                items.add(it)
                            }
                        }
                        rvAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO()
                    }
                })

        }

        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        // RankingActivity로 이동하는 버튼 설정
        val rankingButton = findViewById<ImageButton>(R.id.rankingButton)
        rankingButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    }

    // MainActivity -> add_FAB -> dialogView -> 저장 버튼 설정 시, 호출되는 함수
    fun checkDatesForDuplicates(datesToCheck: List<Calendar>, dataModel: DataModel, To_Do_list : String) {
        if (datesToCheck.isEmpty()) {
            return
        }

        val currentDate = datesToCheck.first()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH) + 1
        val date = currentDate.get(Calendar.DATE)

        myRef.child("User_Memo/${year}-${month}-${date}")
            .orderByChild("toDoList")
            .equalTo(To_Do_list)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@MainActivity, "${year}-${month}-${date}에 중복된 항목이 있습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        myRef
                            .child("User_Memo/${year}-${month}-${date}")
                            .push()
                            .setValue(dataModel)
                    }

                    // 다음 날짜 처리
                    checkDatesForDuplicates(datesToCheck.drop(1), dataModel, To_Do_list)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@MainActivity, "데이터베이스 오류: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    // 다음 날짜 처리
                    checkDatesForDuplicates(datesToCheck.drop(1), dataModel, To_Do_list)
                }
            })
        //>>>>>>>>>>>>>>>>>>>>
        updateCompletionRate() // 완료율 업데이트
        //>>>>>>>>>>>>>>>>>>>>
    }

    // MainActivity -> amountInfo_btn 클릭 시, 호출되는 함수1
    fun calculateTotalAmount(callback: (Int) -> Unit) {
        var totalAmount = 0

        myRef.child("User_Memo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 모든 날짜의 데이터를 탐색
                for (dateSnapshot in snapshot.children) {
                    // 날짜 경로 밑의 항목들을 탐색
                    for (itemSnapshot in dateSnapshot.children) {
                        val completed = itemSnapshot.child("completed").getValue(Boolean::class.java) ?: false
                        val amount = itemSnapshot.child("amount").getValue(Int::class.java) ?: 0

                        // completed false인 항목의 amount 값을 합산
                        if (!completed) {
                            totalAmount += amount
                        }
                    }
                }
                // 콜백을 통해 누적 금액 전달
                callback(totalAmount)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })
    }

    // MainActivity -> amountInfo_btn 클릭 시, 호출되는 함수2
    @SuppressLint("MissingInflatedId")
    private fun showAmountDialog(totalAmount: Int) {
        val amountInfoDialogView = layoutInflater.inflate(R.layout.amount_info_dialog, null)
        val builder = AlertDialog.Builder(this).setView(amountInfoDialogView)
        val amountInfoDialog = builder.create()

        // 총 누적 금액을 다이얼로그의 TextView에 설정
        val totalAmountInfo = amountInfoDialogView.findViewById<TextView>(R.id.totalAmountInfo)
        totalAmountInfo.text = "누적 금액: ${totalAmount}원"

        amountInfoDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        amountInfoDialog.show()

        // check_Btn 설정
        val check_Btn = amountInfoDialogView.findViewById<ImageButton>(R.id.check_Btn)
        check_Btn.setOnClickListener {
            amountInfoDialog.dismiss()
        }
    }



    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // 사용자가 할일을 추가하고 완료율 계산후 CompletionRate를 업데이트함.
    private fun updateCompletionRate() {
        myRef.child("User_Memo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalTasks = 0
                var completedTasks = 0

                for (dateSnapshot in snapshot.children) {
                    var dateTotalTasks = 0
                    var dateCompletedTasks = 0

                    for (taskSnapshot in dateSnapshot.children) {
                        dateTotalTasks++
                        val isCompleted = taskSnapshot.child("isCompleted").getValue(Boolean::class.java) ?: false
                        if (isCompleted) {
                            dateCompletedTasks++
                        }
                    }

                    totalTasks += dateTotalTasks
                    completedTasks += dateCompletedTasks
                }

                val completionRate = if (totalTasks > 0) {
                    completedTasks.toDouble() / totalTasks.toDouble() * 100.0
                } else {
                    0.0
                }

                val completionRateRef = Firebase.database.getReference("CompletionRate").child(Firebase.auth.currentUser!!.uid)
                val updates = mapOf(
                    "weekly" to completionRate,
                    "monthly" to completionRate,
                    "yearly" to completionRate
                )
                completionRateRef.updateChildren(updates)
                    .addOnSuccessListener {
                        updateRankings() // 랭킹 업데이트
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@MainActivity, "완료율 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //완료율 업데이트 하고 랭킹을 갱신함.
    private fun updateRankings() {
        // 전체 사용자 목록을 가져와서 완료율을 기준으로 정렬한 후, 랭킹을 업데이트하는 로직을 구현.
        val rankingsRef = Firebase.database.getReference("rankings")

        // 현재 사용자와 완료율을 기준으로 업데이트
        val userId = Firebase.auth.currentUser!!.uid
        val completionRateRef = Firebase.database.getReference("CompletionRate").child(userId)

        completionRateRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weeklyRate = snapshot.child("weekly").getValue(Double::class.java) ?: 0.0
                val monthlyRate = snapshot.child("monthly").getValue(Double::class.java) ?: 0.0
                val yearlyRate = snapshot.child("yearly").getValue(Double::class.java) ?: 0.0

                val updates = mapOf(
                    "$userId/weekly" to weeklyRate,
                    "$userId/monthly" to monthlyRate,
                    "$userId/yearly" to yearlyRate
                )

                rankingsRef.updateChildren(updates)
                    .addOnSuccessListener {
                        Log.d("RankingUpdate", "랭킹이 성공적으로 업데이트되었습니다.")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@MainActivity, "랭킹 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



}