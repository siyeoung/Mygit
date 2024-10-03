package com.byunggil.project_team5

// 얘가 랭킹메인
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class RankingActivity : AppCompatActivity() {

    private lateinit var rankingRecyclerView: RecyclerView
    private lateinit var myRankingTextView: TextView
    private lateinit var myNicknameTextView: TextView
    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var database: DatabaseReference
    private lateinit var myNickname: String
    private lateinit var auth: FirebaseAuth
    private lateinit var rankingListView: ListView

    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        rankingRecyclerView = findViewById(R.id.rankingRecyclerView)
        myRankingTextView = findViewById(R.id.myRanking) //내 현재 랭킹
        myNicknameTextView = findViewById(R.id.myNickname) // 내 닉넴
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        rankingRecyclerView.layoutManager = LinearLayoutManager(this)
        rankingAdapter = RankingAdapter(emptyList())
        rankingRecyclerView.adapter = rankingAdapter

        // Firebase 초기화
        database = FirebaseDatabase.getInstance().reference.child("rankings")

        // FirebaseAuth에서 현재 사용자 닉네임 가져오기
        val user = FirebaseAuth.getInstance().currentUser
        myNickname = if (user != null) {
            user.displayName ?: "unknown"
        } else {
            "unknown"
        }
        loginCheck{
            fetchRankingData()
            loadRankings(period = toString())
        }


    }

    // 주간, 월간, 연간 랭킹별로 순위를 로드
    private fun loadRankings(period: String) {
        val rankingPath = when (period) {
            "weekly" -> "weekly"
            "monthly" -> "monthly"
            "yearly" -> "yearly"
            else -> "weekly"
        }

        database.child(rankingPath).orderByChild("ranking").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rankingList = mutableListOf<RankingItem>()
                for (data in snapshot.children) {
                    val nickname = data.child("nickname").getValue(String::class.java) ?: ""
                    val rank = data.child("ranking").getValue(Int::class.java) ?: 0
                    val completionRate = data.child("completionRate").getValue(Double::class.java) ?: 0.0

                    val rankingItem = RankingItem(
                        nickname = nickname,
                        rank = rank,
                        completionRate = completionRate
                    )
                    rankingList.add(rankingItem)
                }
                rankingAdapter.updateData(rankingList)
                updateMyRanking(rankingList)  // 내 랭킹 업데이트
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RankingActivity, "Failed to load rankings", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 사용자별로 작업 완료율 계산
    private fun fetchRankingData() {
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>
        database = FirebaseDatabase.getInstance().reference.child("User")
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rankings = mutableListOf<Pair<String, Double>>()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    // !!날짜 별 모든 항목을 세야 함
                    var totalTasks = 0
                    for (dateSnapshot in userSnapshot.child("User_Memo").children) {
                        for (cnt in dateSnapshot.children) {
                            totalTasks++
                            Log.d("data1", dateSnapshot.getValue().toString())  // 데이터
                        }
                    }
                    Log.d("percent1", "${totalTasks}")

                    var completedTasks = 0
                    for (dateSnapshot in userSnapshot.child("User_Memo").children) {
                        for (taskSnapshot in dateSnapshot.children) {
                            val task = taskSnapshot.getValue(DataModel::class.java)
                            if (task?.completed == true) {
                                completedTasks++
                            }
                        }
                    }
                    Log.d("percent2", "${completedTasks}")

                    val completionRate = if (totalTasks > 0) {
                        completedTasks.toDouble() / totalTasks.toDouble() * 100.0
                    } else {
                        0.0
                    }
                    Log.d("percent3", "${completionRate}")

                    // UID 대신 닉네임 가져오기
                    firestore.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            val nickname = document.getString("name") ?: userId
                            rankings.add(Pair(nickname, completionRate))

                            // 모든 유저 데이터가 처리된 후에 랭킹을 표시
                            if (rankings.size == snapshot.childrenCount.toInt()) {
                                rankings.sortByDescending { it.second }
                                displayRankings(rankings)
                            }
                        }
                        .addOnFailureListener {
                            rankings.add(Pair(userId, completionRate))

                            // 모든 유저 데이터가 처리된 후에 랭킹을 표시
                            if (rankings.size == snapshot.childrenCount.toInt()) {
                                rankings.sortByDescending { it.second }
                                displayRankings(rankings)
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RankingActivity, "Failed to load rankings", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // 랭킹을 UI에 표시하고, 내 랭킹을 업데이트
    private fun displayRankings(rankings: List<Pair<String, Double>>) {
        val rankingList = rankings.mapIndexed { index, pair ->
            RankingItem(nickname = pair.first, rank = index + 1, completionRate = pair.second.toDouble())
        }
        rankingAdapter.updateData(rankingList)
        updateMyRanking(rankingList)
    }

    // 내 랭킹을 업데이트하는 함수
    private fun updateMyRanking(rankingList: List<RankingItem>) {
        val myRanking = rankingList.indexOfFirst { it.nickname.equals(myNickname, ignoreCase = true) } + 1

        myRankingTextView.text = "내 랭킹: ${if (myRanking > 0) "$myRanking 위" else "순위 없음"}"


    }

    // 로그인 확인 및 닉네임 설정 함수
    private fun loginCheck(onComplete: () -> Unit) {
        val user = auth.currentUser
        user?.let {
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val myname = document.getString("name")?.trim()
                    myNickname = myname ?: "unknown" // myNickname 초기화
                    myNicknameTextView.text = myNickname
                    onComplete() // 닉네임을 가져온 후에 다음 작업 수행
                }
                .addOnFailureListener {
                    myNickname = "unknown" // 닉네임을 가져오지 못했을 때 기본값 설정
                    onComplete()
                }
        } ?: run {
            myNickname = "unknown"
            onComplete()
        }
    }
}