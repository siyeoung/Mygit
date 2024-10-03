package com.byunggil.project_team5

class DataModel(

    val toDoList: String = "",
    var startYear: Int = 0,
    var startMonth: Int = 0,
    var startDate: Int = 0,
    var endYear: Int = 0,
    var endMonth: Int = 0,
    var endDate: Int = 0,
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 0,
    val endMinute: Int = 0,
    //val isCompleted: Boolean = false,
    val completed: Boolean = false,
    val modeStatus: Boolean = false,
    val amount: Int = 0,
    var id: String? = null // Firebase에서 해당 메모의 ID를 저장할 필드

)