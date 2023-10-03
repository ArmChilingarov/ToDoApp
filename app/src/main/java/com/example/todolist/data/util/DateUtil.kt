package com.example.todolist.data.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

const val MAX_TIMESTAMP = 8640000000000000
const val SECONDS_PAST_FROM_EPOCH = 1640908800 // no of seconds passed from 1970 to 2021

@SuppressLint("SimpleDateFormat")
fun Date.convertDateToString(): String {
    val format1 = "MMM dd, yyyy"
    val format2 = "MMM dd, yyyy, hh:mm aaa"
    val dateInfinity = Date(MAX_TIMESTAMP)
    return if (dateInfinity.compareTo(this) == 0) "N/A"
    else if (this.getSecond() == 0) {
        val df = SimpleDateFormat(format1)
        df.format(this)
    } else {
        val df = SimpleDateFormat(format2)
        df.format(this)
    }
}


fun Date.getSecond(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.SECOND)
}
