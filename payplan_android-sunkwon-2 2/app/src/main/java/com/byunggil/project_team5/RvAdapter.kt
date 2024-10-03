package com.byunggil.project_team5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// MainActivity의 RecyclerView를 관리할 Adapter 생성
// rv.adapter가 사용자에게 적합한 RecyclerView를 제공하기 위해 내부 메서드를 적절히 실행
class RvAdapter (val items : MutableList<DataModel>) : RecyclerView.Adapter<RvAdapter.ViewHolder>(){

    // 새로운 ViewHolder를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder, parent, false)
        // rv_item.xml의 구조를 ViewHolder의 구조로 채택
        return ViewHolder(view)
    }

    // MainActivity.kt에 ItemView 클릭 시 구현할 동작을 정의하기 위해 interface 정의
    interface ItemClick {
        fun onClick(view: View, position: Int, id: String?)
    }
    // ItemClick interface를 상속 받은 객체를 참조할 변수 선언
    var itemClick : ItemClick? = null // null 값으로 초기화

    // 생성된 ViewHolder를 전달 받아, item(테이터)을 바인딩
    override fun onBindViewHolder(holder: RvAdapter.ViewHolder, position: Int) {
        if (itemClick != null) {
            holder.itemView.setOnClickListener { v ->
                val id = items[position].id // DataModel의 id를 가져옴
                itemClick?.onClick(v, position, id)
            }
        }
        holder.bindItems(items[position].toDoList)
    }

    // 전체 Itemview 개수를 Adapter에 전달
    override fun getItemCount(): Int {
        return items.size
    }

    // Adapter가 생성할 ViewHolder의 특성 정의
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item : String) {
            val rv_text = itemView.findViewById<TextView>(R.id.rvItem)
            // ViewHolder는 RecyclerView.ViewHolder의 상속을 받아 만들어 짐
            // RecyclerView.ViewHolder가 관리하는 뷰는 itemView라는 이름으로 정의되어 있음
            // 따라서 findViewById 함수는 View 클래스의 멤버 함수로서 명확히 범위를 itemView 지정해 주어야 함.
            // MainActivity 클래스는 AppCompatActivity의 상속을 통해
            // 클래스 전체가 사용자가 정의한 View로 이미 자동 지정 되어 있기 때문에 itemView와 같은 범위 선언이 필요 없음
            rv_text.text = item
        }
    }

}