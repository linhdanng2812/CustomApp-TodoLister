package com.example.todoapp

import android.app.ActivityManager.TaskDescription
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {

    @Insert()
    suspend fun insertTask(todoModel: TodoModel): Long

    @Query("Select * from TodoModel where isFinished == 0")
    fun getTask(): LiveData<List<TodoModel>>

    @Query("Select * from TodoModel where isFinished == 1")
    fun getArchiveTask(): LiveData<List<TodoModel>>

    @Query("Update TodoModel Set isFinished = 1 where id=:uid")
    fun archiveTask(uid: Long)

    @Query("Delete from TodoModel where id=:uid")
    fun finishTask(uid: Long)

    @Query("Delete from TodoModel where id=:uid")
    fun permanentDeleteTask(uid: Long)

    @Query("Update TodoModel Set isFinished = 0 where id=:uid")
    fun restoreTask(uid: Long)

    @Update
    fun updateTask(task: TodoModel)

    @Query("SELECT * FROM TodoModel WHERE id=:uid")
    fun getEditTask(uid: Long): TodoModel

    @Query("DELETE FROM TodoModel")
    fun deleteAll()
}