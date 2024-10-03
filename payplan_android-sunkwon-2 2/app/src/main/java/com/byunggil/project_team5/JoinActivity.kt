package com.byunggil.project_team5

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.byunggil.project_team5.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class JoinActivity : AppCompatActivity() {
    private var mBinding: ActivityJoinBinding? = null
    private val binding get() = mBinding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        window.apply {
            //상태바
            statusBarColor = Color.BLACK
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false
        }

        binding.signUpButton.setOnClickListener {
            signUp()
        }


    }


    private fun signUp() {


        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val name = binding.nameEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (password != confirmPassword) {
            // Handle password mismatch error
            Log.e("SignUp", "Passwords do not match")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignUp", "createUserWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        // Save name and email to Firestore
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email
                        )
                        firestore.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Log.d("SignUp", "DocumentSnapshot successfully written!")
                                onBackPressed()
                            }
                            .addOnFailureListener { e ->
                                Log.e("SignUp", "Error writing document", e)
                            }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignUp", "createUserWithEmail:failure", task.exception)
                    // Handle sign up failure
                    Toast.makeText(
                        this,
                        "회원가입 실패",
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