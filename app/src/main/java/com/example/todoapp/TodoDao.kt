package com.example.todoapp

import android.app.ActivityManager.TaskDescription
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {

    @Insert()
    suspend fun insertTask(todoModel: TodoModel): Long

    //show list on the MainActivity
    @Query("Select * from TodoModel where isFinished == 0")
    fun getTask(): LiveData<List<TodoModel>>

    //show list on the HistoryActivity
    @Query("Select * from TodoModel where isFinished == 1")
    fun getArchiveTask(): LiveData<List<TodoModel>>

    //call when swipe to the delete option (Main)
    @Query("Update TodoModel Set isFinished = 1 where id=:uid")
    fun archiveTask(uid: Long)

    //call when swipe to the complete option (Main)
    @Query("Delete from TodoModel where id=:uid")
    fun finishTask(uid: Long)

    //call when swipe to the delete option (History)
    @Query("Delete from TodoModel where id=:uid")
    fun permanentDeleteTask(uid: Long)

    //call when swipe to the restore option (History)
    @Query("Update TodoModel Set isFinished = 0 where id=:uid")
    fun restoreTask(uid: Long)

    //call when modify the task and save
    @Update
    fun updateTask(task: TodoModel)

    //display inforamtion of the clicked item row
    @Query("SELECT * FROM TodoModel WHERE id=:uid")
    fun getEditTask(uid: Long): TodoModel

    //sub function to restore the database
    @Query("DELETE FROM TodoModel")
    fun deleteAll()

    @Query("UPDATE sqlite_sequence SET seq = 1 WHERE name = 'TodoModel'")
    fun clearPrimaryKey();
}