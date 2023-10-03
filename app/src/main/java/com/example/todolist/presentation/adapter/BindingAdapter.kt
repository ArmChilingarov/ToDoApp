package com.example.todolist.presentation.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.example.todolist.R
import com.example.todolist.data.util.convertDateToString
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import java.util.*

@SuppressLint("SetTextI18n")
@BindingAdapter("count")
fun setCount(materialTextView: MaterialTextView, count: Int) {
    if (count == 1) materialTextView.text = "$count Task"
    else materialTextView.text = "$count Tasks"
}

@SuppressLint("SetTextI18n")
@BindingAdapter("chip_text")
fun setChipText(chip: Chip, text: String) {
    chip.text = text
}

@BindingAdapter("view_color")
fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
}

@BindingAdapter("set_stroke_color")
fun setStrokeColor(chip: Chip, color: String) {
    chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor(color))
}

@BindingAdapter("check_status", "view_color")
fun setCheckStatus(materialCheckBox: MaterialCheckBox, status: Boolean, color: String) {
    materialCheckBox.setOnCheckedChangeListener(null)
    materialCheckBox.isChecked = status
    CompoundButtonCompat.setButtonTintList(
        materialCheckBox,
        ColorStateList.valueOf(Color.parseColor(color))
    )
}

@SuppressLint("SetTextI18n")
@BindingAdapter("set_date")
fun setDate(dueDate: MaterialTextView, date: Date) {
    dueDate.text = "Due : " + date.convertDateToString()
}

@SuppressLint("SetTextI18n")
@BindingAdapter("priority")
fun setPriority(textView: MaterialTextView, priority: Int) {
    when (priority) {
        0 -> {
            textView.text = "Low"
            TextViewCompat.setCompoundDrawableTintList(
                textView,
                ColorStateList.valueOf(ContextCompat.getColor(textView.context, R.color.flag_low))
            )
        }

        1 -> {
            textView.text = "Medium"
            TextViewCompat.setCompoundDrawableTintList(
                textView,
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        textView.context,
                        R.color.flag_medium
                    )
                )
            )
        }

        else -> {
            textView.text = "High"
            TextViewCompat.setCompoundDrawableTintList(
                textView,
                ColorStateList.valueOf(ContextCompat.getColor(textView.context, R.color.flag_high))
            )
        }
    }
}