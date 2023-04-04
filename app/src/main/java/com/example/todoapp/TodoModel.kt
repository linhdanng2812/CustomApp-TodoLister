package com.example.todoapp

import androidx.room.*
import kotlin.time.Duration
import kotlin.time.toDuration

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

@Entity(
    foreignKeys =
    [
        ForeignKey(
            entity = TodoModel::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Action(
    var task_id: Long,
    var timestamp: Long,
    @PrimaryKey(autoGenerate = true) var id: Long = 0
)

data class TaskWithActions(
    @Embedded val task: TodoModel,
    @Relation(parentColumn = "id", entityColumn = "task_id") val actions: List<Action>
) {


}

data class ActionWithTask(
    @Relation(parentColumn = "task_id", entityColumn = "id") val task: TodoModel,
    @Embedded val action: Action
)
