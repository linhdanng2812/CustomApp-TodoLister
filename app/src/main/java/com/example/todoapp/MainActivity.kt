package com.example.todoapp

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


//Student ID: 103488557
//Student Name: Nguyen Linh Dan
class MainActivity : AppCompatActivity(), TodoAdapter.OnItemClickListener {

    val list = arrayListOf<TodoModel>()
    var adapter = TodoAdapter(list, this)

    val db by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        todoRv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

//        GlobalScope.launch(Dispatchers.IO) {
//            db.todoDao().clearPrimaryKey()
//        }

        ImplementSwipeFunction()

        db.todoDao().getTask().observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }else{
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })

        val sortTime = findViewById<Button>(R.id.sortBtn)
        val sortAZ = findViewById<Button>(R.id.azsortBtn)
        //sort by time
        sortTime.setOnClickListener {
            db.todoDao().getOrderedTaskTime().observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    list.clear()
                    list.addAll(it)
                    adapter.notifyDataSetChanged()
                }else{
                    list.clear()
                    adapter.notifyDataSetChanged()
                }
            })
        }
        //sort by A-Z
        sortAZ.setOnClickListener {
            db.todoDao().getOrderedTaskAlpha().observe(this, Observer {
                if (!it.isNullOrEmpty()) {
                    list.clear()
                    list.addAll(it)
                    adapter.notifyDataSetChanged()
                }else{
                    list.clear()
                    adapter.notifyDataSetChanged()
                }
            })
        }
    }


    override fun onItemClick(position: Int) {
        val sharedPref = this.getSharedPreferences("TaskInfo", Context.MODE_PRIVATE)
        sharedPref.edit().apply() {
            putLong("transfer", list[position].id)
        }.apply()
        //Toast.makeText(this, "Item $position clicked", Toast.LENGTH_SHORT).show()

        val i = Intent(this, TaskActivity::class.java)
        i.putExtra("id", list[position].id)
        startActivity(i)
        //Log.i("state", position.toString())
        Log.i("state", list[position].id.toString())
        adapter.notifyItemChanged(position)
    }

    //handle the swipe action
    fun ImplementSwipeFunction() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                //swipe to the right -> restore
                if (direction == ItemTouchHelper.RIGHT) {
                    GlobalScope.launch(Dispatchers.IO) {
                        db.todoDao().finishTask(adapter.getItemId(position))
                    }
                }
                //swipe to the left -> delete -> move to the archive
                else if (direction == ItemTouchHelper.LEFT) {
                    GlobalScope.launch(Dispatchers.IO) {
                        db.todoDao().archiveTask(adapter.getItemId(position))
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) { //draw a green box for complete
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val paint = Paint()
                    val icon: Bitmap
                    if (dX > 0) {
                        icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_check_white_png)
                        paint.color = Color.parseColor("#388E3C")
                        canvas.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                        )
                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat(),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }
                    else { //draw a red box for archiving
                        icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_delete_white_png)
                        paint.color = Color.parseColor("#D32F2F")
                        canvas.drawRect(
                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                        )
                        canvas.drawBitmap(
                            icon,
                            itemView.right.toFloat() - icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }
                    viewHolder.itemView.translationX = dX
                }
                else {
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(todoRv) //attach the swipe to the RecyclerView
    }

    //display the toolbar with menu icon and search bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        item.setOnActionExpandListener(object :MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }
        })
        //react when user enter sth in the search view
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrEmpty()){
                    displayTodo(newText)
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    //display the search result
    fun displayTodo(newText: String = "") {
        db.todoDao().getTask().observe(this, Observer {
            if(it.isNotEmpty()){
                list.clear()
                list.addAll(
                    it.filter { todo ->
                        todo.title.contains(newText,true)
                    }
                )
                adapter.notifyDataSetChanged()
            }
        })
    }

    // display the dropdown menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //open the TaskActivity with all blank fields
    fun openNewTask(v: View) {
       val i = (Intent(this, TaskActivity::class.java))
        startActivity(i)

//        val sharedPref = this.getSharedPreferences("TaskInfo", Context.MODE_PRIVATE)
//        sharedPref.edit().apply() {
//            putLong("id", -1L)
//        }.apply()
    }

//    fun openEditTask(v: View) {
//        val sharedPref = this.getSharedPreferences("TaskInfo", Context.MODE_PRIVATE)
//        val transfer = sharedPref.getLong("transfer", -1L)
//        val i = (Intent(this, TaskActivity::class.java))
//        i.putExtra("id", transfer)
//        startActivity(i)
//    }

}