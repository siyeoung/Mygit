package com.byunggil.project_team5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RankingAdapter(private var rankingList: List<RankingItem>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = rankingList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = rankingList.size

    fun updateData(newRankingList: List<RankingItem>) {
        rankingList = newRankingList
        notifyDataSetChanged()
    }

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankingTextView: TextView = itemView.findViewById(R.id.rankingTextView)
        private val nicknameTextView: TextView = itemView.findViewById(R.id.nicknameTextView)
        private val completionRateTextView: TextView = itemView.findViewById(R.id.completionRateTextView)

        fun bind(item: RankingItem) {
            rankingTextView.text = "${item.rank}위"
            nicknameTextView.text = item.nickname
            completionRateTextView.text = "${item.completionRate.toInt()}%" // 완료율 표시
        }
    }
}
