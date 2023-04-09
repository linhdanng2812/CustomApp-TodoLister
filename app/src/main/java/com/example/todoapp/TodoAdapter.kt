package com.example.todoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.row_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(val list: List<TodoModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_layout, parent, false)

        return TodoViewHolder(itemView)
    }


    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(list[position]) // passing the object of the list that we made in the ToDoModel.kt
    }

    //get the ID of the clicked item
    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    //when an item is clicked
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    //display the list as the RecyclerView
    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        //initialize
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
        //display the task details
        fun bind(todoModel: TodoModel) {
            with(itemView) {
                txtShowTitle.text = todoModel.title
                txtDescription.text = todoModel.description
                txtCategory.text = todoModel.category
                updateTime(todoModel.time)
                updateDate(todoModel.date)
            }
        }
        //display the time on the time row
        private fun updateTime(time: Long) {
            val myformat = "h:mm a"
            val sdf = SimpleDateFormat(myformat)
            itemView.txtTime.text = sdf.format(Date(time))
        }
        //display the date on the item row
        private fun updateDate(time: Long) {
            //Mon, 5 Jan 2020
            val myformat = "EEE, d MMM yyyy"
            val sdf = SimpleDateFormat(myformat)
            itemView.txtDate.text = sdf.format(Date(time))
        }
    }
}


