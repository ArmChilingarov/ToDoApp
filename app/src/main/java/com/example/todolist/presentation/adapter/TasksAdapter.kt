package com.example.todolist.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.databinding.ItemTaskBinding

class TasksAdapter:
    RecyclerView.Adapter<TasksAdapter.MyViewHolder>() {

    private val callback = object : DiffUtil.ItemCallback<TaskInfo>() {
        override fun areItemsTheSame(
            oldItem: TaskInfo,
            newItem: TaskInfo
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TaskInfo,
            newItem: TaskInfo
        ): Boolean {
            return oldItem==newItem
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_task, parent, false
        ))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class MyViewHolder(private val itemTaskBinding: ItemTaskBinding) :
        RecyclerView.ViewHolder(itemTaskBinding.root) {
        fun bind(taskInfo: TaskInfo) {
            itemTaskBinding.taskInfo = taskInfo
            itemTaskBinding.executePendingBindings()

            itemTaskBinding.isCompleted.setOnCheckedChangeListener { _, it ->
                val task = taskInfo.copy(status = it)
                onTaskStatusChangedListener?.let {
                    it(task)
                }
            }

            itemTaskBinding.root.setOnClickListener {
                onItemClickListener?.let {
                    it(taskInfo)
                }
            }

        }
    }

    private var onItemClickListener: ((TaskInfo) -> Unit)? = null
    fun setOnItemClickListener(listener: (TaskInfo) -> Unit) {
        onItemClickListener = listener
    }

    private var onTaskStatusChangedListener: ((TaskInfo) -> Unit)? = null
    fun setOnTaskStatusChangedListener(listener: (TaskInfo) -> Unit) {
        onTaskStatusChangedListener = listener
    }

}