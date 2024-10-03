package com.byunggil.project_team5

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// modeStatus가 True일때 페이지.
class HighStakesRankingActivity : AppCompatActivity() {

    private lateinit var rankingRecyclerView: RecyclerView
    private lateinit var myRankingTextView: TextView
    private lateinit var myNicknameTextView: TextView
    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var database: DatabaseReference
    private lateinit var myNickname: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_stakes_ranking)

        rankingRecyclerView = findViewById(R.id.highStakesRankingRecyclerView)
        myRankingTextView = findViewById(R.id.myHighStakesRanking)
        myNicknameTextView = findViewById(R.id.myHighStakesNickname)
        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().reference.child("high_stakes_rankings")

        rankingRecyclerView.layoutManager = LinearLayoutManager(this)
        rankingAdapter = RankingAdapter(emptyList())
        rankingRecyclerView.adapter = rankingAdapter

        // FirebaseAuth에서 현재 사용자 닉네임 가져오기
        val user = FirebaseAuth.getInstance().currentUser
        myNickname = if (user != null) {
            user.displayName ?: "unknown"
        } else {
            "unknown"
        }

        fetchRankingData()
        loadRankings()
    }

    private fun loadRankings() {
        database.orderByChild("rank").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rankingList = mutableListOf<RankingItem>()
                for (data in snapshot.children) {
                    val nickname = data.child("nickname").getValue(String::class.java) ?: ""
                    val rank = data.child("rank").getValue(Int::class.java) ?: 0
                    val completionRate = data.child("completionRate").getValue(Double::class.java) ?: 0.0

                    val rankingItem = RankingItem(
                        nickname = nickname,
                        rank = rank,
                        completionRate = completionRate
                    )
                    rankingList.add(rankingItem)
                }
                rankingAdapter.updateData(rankingList)
                updateMyRanking(rankingList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HighStakesRankingActivity, "Failed to load rankings", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRankingData() {
        // Implement your data fetching logic here
    }

    private fun updateMyRanking(rankingList: List<RankingItem>) {
        val myRanking = rankingList.indexOfFirst { it.nickname == myNickname } + 1
        myRankingTextView.text = "My Ranking: ${if (myRanking > 0) myRanking else "No Rank"}"
        myNicknameTextView.text = myNickname
    }
}
