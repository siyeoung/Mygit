package com.byunggil.project_team5

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.byunggil.project_team5.databinding.ActivitySplashBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var mBinding: ActivitySplashBinding? = null
    private val binding get() = mBinding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        window.apply {
            //상태바
            statusBarColor = Color.BLACK
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false
        }

        startLogin()


        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                if (password.isEmpty()) {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                login()
            }

        }

        binding.JoinBtn.setOnClickListener {
            val joingo = Intent(this, JoinActivity::class.java)
            startActivity(joingo)
        }







    }

    //로그인 체크 함수
    fun startLogin() {
        val currentUser = auth.currentUser
        if (currentUser != null) {

            val Realmaingo2 = Intent(this, MainActivity::class.java)
            startActivity(Realmaingo2)
            finish()
        } else {


        }
    }

    fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        // Firebase Authentication을 통해 로그인 시도
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // 로그인 성공시 Firestore에서 닉네임 가져오기
                        firestore.collection("users").document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                val nickname = document.getString("name")
                                Toast.makeText(this, "로그인 성공! 닉네임: $nickname", Toast.LENGTH_SHORT).show()
                                val Realmaingo = Intent(this, MainActivity::class.java)
                                startActivity(Realmaingo)

                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this,
                                    "닉네임 가져오기 실패: $exception",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "로그인 실패: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    // 화면 터치 시 키보드 내리기
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return super.dispatchTouchEvent(ev)
    }


}