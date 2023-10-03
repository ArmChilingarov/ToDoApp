package com.example.todolist.presentation.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todolist.R
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.data.model.toJson
import com.example.todolist.data.model.toTaskInfo
import com.example.todolist.data.util.MAX_TIMESTAMP
import com.example.todolist.data.util.SECONDS_PAST_FROM_EPOCH
import com.example.todolist.data.util.convertDateToString
import com.example.todolist.data.util.getSecond
import com.example.todolist.databinding.FragmentNewTaskBinding
import com.example.todolist.presentation.MainActivity
import com.example.todolist.presentation.MainActivityViewModel
import com.example.todolist.presentation.br.AlarmReceiver
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Random

class NewTaskFragment : Fragment() {
    private lateinit var binding: FragmentNewTaskBinding
    private lateinit var navController: NavController
    private val args: NewTaskFragmentArgs by navArgs()
    private var taskInfo =
        TaskInfo(0, "", "", Date(MAX_TIMESTAMP), 0, false, CategoryInfo("", "#000000"))
    private var categoryInfo = CategoryInfo("", "#000000")
    private lateinit var viewModel: MainActivityViewModel
    private var colorString = "#000000"
    private lateinit var prevTaskCategory: TaskInfo
    private var isCategorySelected = false
    private lateinit var colorView: View
    private val permissionContract =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val areAllGranted = !it.values.contains(false)
            if (areAllGranted) {
                addTask()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_task, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        navController = findNavController()
        initUpdate(args.newTaskArg)
        setInitialValues()
        loadAllCategories()
    }

    private fun initUpdate(newTaskJson: String?) {
        newTaskJson.toTaskInfo()?.let {
            taskInfo = it
            categoryInfo = it.category
            binding.fab.tag = "Update"
            colorString = categoryInfo.color
            prevTaskCategory = taskInfo.copy()
            isCategorySelected = true
        }
    }

    private fun setInitialValues() {
        var str = taskInfo.date.convertDateToString()
        if (str == "N/A") str = "Due Date"

        binding.apply {
            etTitle.setText(taskInfo.title)
            etDescription.setText(taskInfo.description)
            dateAndTimePicker.text = str
            isCompleted.isChecked = taskInfo.status

            when (taskInfo.priority) {
                0 -> low.isChecked = true
                1 -> mid.isChecked = true
                else -> high.isChecked = true
            }

            //ClickListeners
            dateAndTimePicker.setOnClickListener { showDateTimePicker() }
            isCompleted.setOnCheckedChangeListener { _, it ->
                taskInfo = taskInfo.copy(status = it)
            }
            fab.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val resultNotif = ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (resultNotif == PackageManager.PERMISSION_GRANTED) {
                        addTask()
                    } else {

                        permissionContract.launch(
                            arrayOf(
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.USE_EXACT_ALARM
                            )
                        )

                    }
                } else {
                    addTask()
                }

            }
            priorityChipGroup.setOnCheckedStateChangeListener { chipGroup, i ->
                changePriority(chipGroup, i)
            }
            categoryChipGroup.setOnCheckedStateChangeListener { chipGroup, i ->
                listenToCategoryClick(chipGroup, i)
            }

        }
    }

    private fun loadAllCategories() {
        viewModel.getCategories().observe(viewLifecycleOwner) {
            for (category in it) {
                val chip = Chip(context)
                chip.text = category.categoryInformation
                val drawable = ChipDrawable.createFromAttributes(
                    requireContext(),
                    null,
                    0,
                    com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
                )
                chip.setChipDrawable(drawable)
                chip.tag = category.color
                chip.isChecked = chip.text == taskInfo.category.categoryInformation
                binding.categoryChipGroup.addView(chip)
            }
        }
    }

    private fun changePriority(chipGroup: ChipGroup, i: List<Int>) {
        val id = i[0]
        val chip = chipGroup.findViewById(id) as Chip

        val priority = when (chip.text) {
            "Low" -> 0
            "Medium" -> 1
            else -> 2
        }
        taskInfo = taskInfo.copy(priority = priority)
    }

    private fun listenToCategoryClick(chipGroup: ChipGroup, i: List<Int>) {
        val id = i[0]
        val chip = chipGroup.findViewById(id) as Chip
        if (chip.text.toString() == requireContext().getString(R.string.add_new_category)) {
            displayCategoryChooseDialog()
            isCategorySelected = false
        } else {
            categoryInfo = categoryInfo.copy(
                categoryInformation = chip.text.toString(),
                color = chip.tag.toString()
            )
            taskInfo = taskInfo.copy(category = categoryInfo)
            colorString = categoryInfo.color
            isCategorySelected = true
        }
    }

    private fun displayCategoryChooseDialog() {
        colorString = generateRandomColor()
        Log.d("DATA", colorString)
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.category_dialog)
        val editText = dialog.findViewById<TextInputEditText>(R.id.editText)
        val addCategory = dialog.findViewById<MaterialButton>(R.id.addCategory)
        val addColor = dialog.findViewById<MaterialButton>(R.id.addColor)
        colorView = dialog.findViewById<View>(R.id.viewColor)
        colorView.setBackgroundColor(Color.parseColor(colorString))
        addColor.setOnClickListener { displayColorPickerDialog() }
        addCategory.setOnClickListener {
            if (editText.text.isNullOrBlank())
                Snackbar.make(binding.root, "Please add category", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            else {
                addNewCategoryChip(editText.text.toString())
            }
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun generateRandomColor(): String {
        val random = Random()
        val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        return "#" + Integer.toHexString(color)
    }

    private fun displayColorPickerDialog() {
        ColorPickerDialogBuilder
            .with(context)
            .setTitle("Choose color")
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(12)
            .setOnColorSelectedListener { selectedColor ->
                colorString = "#" + Integer.toHexString(selectedColor)
            }
            .setPositiveButton("Ok") { _, _, _ ->
                colorView.setBackgroundColor(Color.parseColor(colorString))
            }
            .setNegativeButton("Cancel") { _, _ ->
                colorString = "#000000"
            }
            .build()
            .show()
    }

    private fun addNewCategoryChip(category: String) {
        val chip = Chip(context)
        val drawable = ChipDrawable.createFromAttributes(
            requireContext(),
            null,
            0,
            com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice
        )
        chip.apply {
            setChipDrawable(drawable)
            text = category
            isCheckable = true
            isChecked = true
            tag = colorString
        }
        categoryInfo = categoryInfo.copy(
            categoryInformation = chip.text.toString(),
            color = colorString
        )
        taskInfo = taskInfo.copy(category = categoryInfo)
        binding.categoryChipGroup.addView(chip)
        isCategorySelected = true
    }

    private fun addTask() {
        val date = Date()
        Log.d("DATA", taskInfo.date.getSecond().toString())
        taskInfo = taskInfo.copy(
            title = binding.etTitle.text.toString(),
            description = binding.etDescription.text.toString()
        )
        if (taskInfo.title.isBlank()) Snackbar.make(
            binding.root,
            "Please add title",
            Snackbar.LENGTH_SHORT
        ).setAction("Action", null).show()
        else if (taskInfo.category.categoryInformation.isBlank() || categoryInfo.categoryInformation.isBlank() || !isCategorySelected) Snackbar.make(
            binding.root,
            "Please select a category",
            Snackbar.LENGTH_SHORT
        ).setAction("Action", null).show()
        else {
            if (binding.fab.tag.equals("Update")) {
                updateTask()
            } else {
                val diff = (Date().time / 1000) - SECONDS_PAST_FROM_EPOCH
                taskInfo = taskInfo.copy(id = diff.toInt())
                viewModel.insertTaskAndCategory(taskInfo, categoryInfo)
                if (!taskInfo.status && taskInfo.date > date && taskInfo.date.getSecond() == 5)
                    setAlarm(taskInfo)
            }
            navController.popBackStack()
        }
    }

    private fun updateTask() {
        val date = Date()
        if (taskInfo.category == prevTaskCategory.category)
            viewModel.updateTaskAndAddCategory(taskInfo, categoryInfo)
        else {
            CoroutineScope(Main).launch {
                if (viewModel.getCountOfCategory(prevTaskCategory.category.categoryInformation) == 1) {
                    viewModel.updateTaskAndAddDeleteCategory(
                        taskInfo,
                        categoryInfo,
                        prevTaskCategory.category
                    )
                } else {
                    viewModel.updateTaskAndAddCategory(taskInfo, categoryInfo)
                }
            }
        }

        if (!taskInfo.status && taskInfo.date > date && taskInfo.date.getSecond() == 5)
            setAlarm(taskInfo)
        else removeAlarm(taskInfo)
    }

    private fun removeAlarm(taskInfo: TaskInfo) {
        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("task_info", taskInfo.toJson())
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            taskInfo.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun showDateTimePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(CLOCK_24H).build()
        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            taskInfo = taskInfo.copy(date = calendar.time)
            binding.dateAndTimePicker.text = taskInfo.date.convertDateToString()
            timePicker.show(childFragmentManager, "TAG")
        }

        timePicker.addOnPositiveButtonClickListener {
            val cal = Calendar.getInstance()
            cal.time = taskInfo.date
            cal.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            cal.set(Calendar.MINUTE, timePicker.minute)
            cal.set(Calendar.SECOND, 5)
            taskInfo = taskInfo.copy(date = cal.time)
            binding.dateAndTimePicker.text = taskInfo.date.convertDateToString()
        }
        datePicker.show(childFragmentManager, "TAG")
    }

    private fun setAlarm(taskInfo: TaskInfo) {
        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("task_info", taskInfo.toJson())
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            taskInfo.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val mainActivityIntent = Intent(requireContext(), MainActivity::class.java)
        val basicPendingIntent = PendingIntent.getActivity(
            requireContext(),
            taskInfo.id,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val clockInfo = AlarmManager.AlarmClockInfo(taskInfo.date.time, basicPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(clockInfo, pendingIntent)
            }
        }


    }
}