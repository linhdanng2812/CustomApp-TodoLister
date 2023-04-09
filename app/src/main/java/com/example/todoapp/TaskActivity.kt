package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_task.*
import kotlinx.android.synthetic.main.activity_task.dateField
import kotlinx.android.synthetic.main.activity_task.btnSave
import kotlinx.android.synthetic.main.activity_task.dropdownCategory

import kotlinx.android.synthetic.main.activity_task.timeField
import kotlinx.android.synthetic.main.activity_task.timeInptLay
import kotlinx.android.synthetic.main.activity_task.titleInput
import kotlinx.android.synthetic.main.activity_task.toolbarAddTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

const val DB_NAME = "todo.db"

class TaskActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var myCalendar: Calendar
    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private val sdf = SimpleDateFormat("EEE, d MMM yyyy")
    private val now = System.currentTimeMillis()
    var finalDate = 0L
    var finalTime = 0L
    private var taskId = -1L
    private var isNew = true
    private var check = true
    private val labels = arrayListOf("Personal", "Family", "Friends", "Study")

    val db by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        dateField.setOnClickListener(this)
        timeField.setOnClickListener(this)
        btnSave.setOnClickListener(this)

        //val sharedPref = this.getSharedPreferences("TaskInfo", Context.MODE_PRIVATE)
        //taskId = sharedPref.getLong("id", -1L)

        //get the stored task ID from the MainActivity
        taskId = intent.getLongExtra("id", -1L)
        Log.i("taskID", taskId.toString())
        isNew = taskId == -1L
        setUpDropdownList()
        setDefaults()
    }

    //setup the dropdown category list
    private fun setUpDropdownList() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels)
        labels.sort()
        dropdownCategory.adapter = adapter
    }

    //handle click on different icons
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSave -> {
                if (check) { saveTodoTask() }
            }
            R.id.dateField -> {
                setDateListener()
            }
            R.id.timeField -> {
                setTimeListener()
            }
        }
    }

    //set the default value
    private fun setDefaults() {
        //when create a new task
        if (isNew) {
            toolbarAddTask.title = getString(R.string.new_task)
            btnSave.text = getString(R.string.save_new_task)
            dateField.setText(sdf.format(now))
            titleInput.editText?.requestFocus()
        }
        //when modify an existing task
        else {
            toolbarAddTask.title = getString(R.string.edit_task)
            btnSave.text = getString(R.string.save_edit_task)

            GlobalScope.launch(Dispatchers.Main) {
                val task =
                    withContext(Dispatchers.IO) {
                        return@withContext db.todoDao().getEditTask(taskId)
                    }
                titleInput.editText?.setText(task.title)
                doneInput.editText?.setText(task.description)
                dateField.isEnabled = true
                dateField.setText(sdf.format(task.date))
                Log.i("taskid", task.id.toString())
            }
        }
    }

    //when the 'Save' or 'Create' button is clicked
    private fun saveTodoTask() {
        val category = dropdownCategory.selectedItem.toString()
        val title = titleInput.editText?.text.toString()
        val description = doneInput.editText?.text.toString()

        //display the length errors
        if (!isValidateLength(titleInput.editText?.text.toString())) {
            titleInput.setError("Title must be between 1-15")
        }
        if (!isValidateLength(doneInput.editText?.text.toString())) {
            doneInput.setError("Description must be between 1-15")
        }
        //save new task
        if (isNew) {
            GlobalScope.launch(Dispatchers.Main) {
                val id = withContext(Dispatchers.IO) {
                    return@withContext db.todoDao().insertTask(
                        TodoModel(
                            title,
                            description,
                            category,
                            finalDate,
                            finalTime
                        )
                    )
                }
                finish()
            }
        }
        //save new changes to an existing task
        else {
                GlobalScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        val task = TodoModel(title, description, category, finalDate, finalTime, 0, taskId)
                        return@withContext db.todoDao().updateTask(task)
                    }
                    finish()
                }
            }
        }

    //setup the date selector
    private fun setDateListener() {
        myCalendar = Calendar.getInstance()
        dateSetListener =
            DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDate()
            }
        val datePickerDialog = DatePickerDialog(
            this, dateSetListener, myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    //update new date
    private fun updateDate() {
        //Mon, 5 Jan 2020
        val myformat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myformat)
        finalDate = myCalendar.time.time
        dateField.setText(sdf.format(myCalendar.time))
        timeInptLay.visibility = View.VISIBLE
    }

    //setup the time selector
    private fun setTimeListener() {
        myCalendar = Calendar.getInstance()
        timeSetListener =
            TimePickerDialog.OnTimeSetListener() { _: TimePicker, hourOfDay: Int, min: Int ->
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                myCalendar.set(Calendar.MINUTE, min)
                updateTime()
            }
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    //update new time
    private fun updateTime() {
        val format = "h:mm a"
        val sdf = SimpleDateFormat(format)
        finalTime = myCalendar.time.time
        timeField.setText(sdf.format(myCalendar.time))

    }

    //validate the length of entered value
    fun isValidateLength(str: String): Boolean {
        if (str.length in 1..15) {
            return true
        }
        return false
    }
}