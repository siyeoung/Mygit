package com.byunggil.project_team5

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.byunggil.project_team5.databinding.ActivityDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class DetailActivity : AppCompatActivity() {
    private var mBinding: ActivityDetailBinding? = null
    private val binding get() = mBinding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val memoId = getMemoIdFromIntent()
        val selectedDate = getSelectedDateFromIntent()

        if (memoId != null && selectedDate != null) {
            loadMemoDetail(memoId, selectedDate)
        } else {
            showErrorAndClose("메모 ID 또는 날짜를 가져오지 못했습니다.")
        }


        window.apply {
            //상태바
            statusBarColor = Color.parseColor("#F2F2F2")
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        binding.statView.setOnClickListener {
            toggleCompletedStatus(memoId, selectedDate)
        }



    }

    // 인텐트에서 메모 ID를 가져오는 함수
    private fun getMemoIdFromIntent(): String? {
        return intent.getStringExtra("MEMO_ID")
    }

    // 인텐트에서 선택한 날짜를 가져오는 함수
    private fun getSelectedDateFromIntent(): String? {
        return intent.getStringExtra("SELECTED_DATE")
    }

    // Firebase에서 메모 세부 정보를 불러오는 함수
    private fun loadMemoDetail(memoId: String, selectedDate: String) {
        val myRef = getMemoDatabaseReference().child(selectedDate)

        myRef.child(memoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val memo = snapshot.getValue(DataModel::class.java)
                        val modeStatus = snapshot.child("modeStatus").getValue(Boolean::class.java) ?: false
                        val amount = snapshot.child("amount").getValue(Int::class.java) ?: 0
                        val completed = snapshot.child("completed").getValue(Boolean::class.java) ?: false // completed 값 가져오기

                        if (memo != null) {
                            displayMemoDetails(memo)
                            startCountdown(memo) // 메모 로드 후 카운트다운 시작

                            // modeStatus 값을 확인하고 그에 따라 작업 수행
                            if (modeStatus) {
                                performTrueModeActions(amount)
                            } else {
                                performFalseModeActions()
                            }

                            // 현재 completed 상태를 UI에 반영
                            updateCompletedStatusUI(completed)

                        } else {
                            showErrorAndClose("메모를 로드하는 데 실패했습니다.")
                        }
                    } else {
                        showErrorAndClose("메모를 찾을 수 없습니다.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showErrorAndClose("데이터베이스 오류: ${error.message}")
                }
            })
    }

    // modeStatus가 true일 때 수행할 작업
    private fun performTrueModeActions(amount: Int) {
        Toast.makeText(this, "Mode Status is True, Amount: $amount", Toast.LENGTH_SHORT).show()
        binding.coinTv.text = "${amount}원"
        binding.coinView.visibility = View.VISIBLE

        val memoId = getMemoIdFromIntent()
        val selectedDate = getSelectedDateFromIntent()

        if (memoId != null && selectedDate != null) {
            val myRef = getMemoDatabaseReference().child(selectedDate).child(memoId)

            myRef.child("completed").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isCompleted = snapshot.getValue(Boolean::class.java) ?: false

                    binding.lottieView.setOnClickListener {
                        if (isCompleted) {
                            Toast.makeText(this@DetailActivity, "이미 완료한 상태입니다", Toast.LENGTH_SHORT).show()
                        } else {
                            openGallery()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailActivity, "Failed to check completion status", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // modeStatus가 false일 때 수행할 작업
    private fun performFalseModeActions() {
        Toast.makeText(this, "Mode Status is False", Toast.LENGTH_SHORT).show()
        binding.coinView.visibility = View.GONE

        val memoId = getMemoIdFromIntent()
        val selectedDate = getSelectedDateFromIntent()

        if (memoId != null && selectedDate != null) {
            val myRef = getMemoDatabaseReference().child(selectedDate).child(memoId)

            myRef.child("completed").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isCompleted = snapshot.getValue(Boolean::class.java) ?: false

                    binding.lottieView.setOnClickListener {
                        if (isCompleted) {
                            Toast.makeText(this@DetailActivity, "이미 완료한 상태입니다", Toast.LENGTH_SHORT).show()
                        } else {
                            updateCompletedStatusToTrue()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailActivity, "Failed to check completion status", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateCompletedStatusToTrue() {
        val memoId = getMemoIdFromIntent()
        val selectedDate = getSelectedDateFromIntent()

        if (memoId != null && selectedDate != null) {
            val myRef = getMemoDatabaseReference().child(selectedDate).child(memoId)

            myRef.child("completed").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "Completed status updated to True", Toast.LENGTH_SHORT).show()
                    updateCompletedStatusUI(true) // UI 업데이트
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update Completed status", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Firebase 경로를 가져오는 함수
    private fun getMemoDatabaseReference() =
        Firebase.database.getReference("User")
            .child(auth.currentUser!!.uid)
            .child("User_Memo")

    // 메모 세부 정보를 화면에 표시하는 함수
    private fun displayMemoDetails(memo: DataModel) {
        binding.memoToDoList.text = memo.toDoList
        binding.memoDate.text = "${memo.startYear}-${memo.startMonth}-${memo.startDate}"
        binding.memoStartTime.text = "${memo.startHour}:${memo.startMinute}"
        binding.memoEndTime.text = "${memo.endHour}:${memo.endMinute}"
    }

    // 남은 시간을 계산하고 화면에 표시하는 함수
    private fun startCountdown(memo: DataModel) {
        runnable = object : Runnable {
            override fun run() {
                val currentTime = Calendar.getInstance().timeInMillis
                val endTime = getEndTimeInMillis(memo)

                if (endTime > currentTime) {
                    val timeLeft = endTime - currentTime

                    // 시간을 초로 환산
                    val hours = TimeUnit.MILLISECONDS.toHours(timeLeft)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60

                    // 시간이 1시간 이상일 때
                    if (hours > 0) {
                        binding.remainingTime.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        binding.remainingTime.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    }

                    handler.postDelayed(this, 1000) // 1초 후 다시 실행
                } else {
                    // 시간 지남 표시
                    binding.remainingTime.text = "시간 지남"
                }
            }
        }
        handler.post(runnable!!)
    }

    // 메모의 종료 시간을 밀리초로 변환하는 함수
    private fun getEndTimeInMillis(memo: DataModel): Long {
        val calendar = Calendar.getInstance()
        calendar.set(memo.startYear, memo.startMonth - 1, memo.startDate, memo.endHour, memo.endMinute, 0)
        return calendar.timeInMillis
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable!!) // 액티비티 종료 시 핸들러 콜백 제거
    }

    // 오류 메시지를 표시하고 액티비티를 종료하는 함수
    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    // 현재 completed 상태에 따라 UI 업데이트
    private fun updateCompletedStatusUI(completed: Boolean) {
        binding.statTv.text = if (completed) "Completed" else "Not Completed"
    }

    // statView 버튼을 눌렀을 때 completed 필드를 토글하는 함수
    private fun toggleCompletedStatus(memoId: String?, selectedDate: String?) {
        if (memoId == null || selectedDate == null) return

        val myRef = getMemoDatabaseReference().child(selectedDate).child(memoId)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val completed = snapshot.child("completed").getValue(Boolean::class.java) ?: false
                    val newCompletedStatus = !completed // 현재 상태를 반전

                    // Firebase에 새로운 상태를 업데이트
                    myRef.child("completed").setValue(newCompletedStatus)
                        .addOnSuccessListener {
                            updateCompletedStatusUI(newCompletedStatus)
                            Toast.makeText(this@DetailActivity, "Status Updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@DetailActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // completed 필드가 없을 경우 새로 추가
                    myRef.child("completed").setValue(true)
                        .addOnSuccessListener {
                            updateCompletedStatusUI(true)
                            Toast.makeText(this@DetailActivity, "Status Created and Set to Completed", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@DetailActivity, "Failed to create status", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            uploadImageToFirebase(imageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    if (!isDestroyed && !isFinishing) {
                        saveImageUrlToDatabase(downloadUri.toString())
                    } else {
                        Toast.makeText(this, "Activity is not active anymore", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val memoId = getMemoIdFromIntent()
        val selectedDate = getSelectedDateFromIntent()

        if (memoId != null && selectedDate != null) {
            val myRef = getMemoDatabaseReference().child(selectedDate).child(memoId)
            val updates = hashMapOf<String, Any>(
                "url" to imageUrl,
                "completed" to true
            )

            myRef.updateChildren(updates)
                .addOnSuccessListener {
                    if (!isDestroyed && !isFinishing) {
                        Toast.makeText(this, "Image URL saved and completed status set to true", Toast.LENGTH_SHORT).show()
                        updateCompletedStatusUI(true) // UI 업데이트
                    }
                }
                .addOnFailureListener {
                    if (!isDestroyed && !isFinishing) {
                        Toast.makeText(this, "Failed to save image URL and update status", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


}