package com.example.todoapp

import androidx.room.*

// we are making list for each task
@Entity
data class TodoModel(
    var title:String,
    var description:String,
    var category: String,
    var date:Long,
    var time:Long,
    var isFinished : Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0
)
