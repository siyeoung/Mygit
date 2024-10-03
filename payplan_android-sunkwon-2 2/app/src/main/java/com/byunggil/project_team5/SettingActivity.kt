package com.byunggil.project_team5

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byunggil.project_team5.databinding.ActivitySettingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class SettingActivity : AppCompatActivity() {

    private var mBinding: ActivitySettingBinding? = null
    private val binding get() = mBinding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loginCheck()


        binding.logoutBtn.setOnClickListener {

            Firebase.auth.signOut()



            // 5초 후에 작업을 실행하기 위해 Handler를 사용합니다.
            Handler().postDelayed({

                // 현재 액티비티를 다시 시작하는 Intent 생성
                finishAffinity()
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                System.exit(0)
                Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_SHORT).show()


            }, 3000) // 3초 지연

        }



    }



    fun loginCheck() {
        val user = auth.currentUser
        user?.let {
            // 로그인 성공시 Firestore에서 닉네임 가져오기
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val myname = document.getString("name")
                    val myemail = document.getString("email")

                    binding.myNameTv.text = myname
                    binding.myEmailTv.text = myemail

                }
                .addOnFailureListener {

                }
        }
    }



}