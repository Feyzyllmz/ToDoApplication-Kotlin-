package com.example.todoapplication

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.example.todo.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseViewModel(itemViewModel: ItemViewModel) : ViewModel() {

    val viewModel = itemViewModel
    var countTasks = 0
    val database = FirebaseDatabase.getInstance()
    val taskReference = database.getReference("tasks")
    val countReference = database.getReference("count")

    fun setCount(){
        countReference.get().addOnSuccessListener {
            countTasks=it.getValue(Int::class.java)!!
        }

    }

    fun allTasks(fragmentManager: FragmentManager){
        val taskListenerStart = (object  : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach {
                        createFragment(fragmentManager,it.getValue(Task::class.java)!!, false) }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        taskReference.addListenerForSingleValueEvent(taskListenerStart)

    }

    fun deleteTask(){
        val selectedTask = viewModel.selectedItem.value
        val taskListener = (object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    dataSnapshot.children.filter { it.getValue(Task::class.java)?.taskId==selectedTask?.taskId  }
                        .forEach { _ -> taskReference.child(selectedTask?.taskId.toString()).removeValue() }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        taskReference.addListenerForSingleValueEvent(taskListener)

    }
    fun updateTask(){
        val checkedTask =viewModel.checkedItem.value
        val taskListener2 = (object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val updateTaskMap = HashMap<String?, Any?>()
                updateTaskMap["complete"] = checkedTask?.isComplete
                updateTaskMap["content"] = checkedTask?.content
                updateTaskMap["taskId"] = checkedTask?.taskId
                taskReference.child(checkedTask?.taskId.toString()).updateChildren(updateTaskMap)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        taskReference.addListenerForSingleValueEvent(taskListener2)

    }
    fun addTask(fragmentManager: FragmentManager,context : Context ){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_add_popup_screen)
        val closeButton = dialog.findViewById<ImageButton>(R.id.imageButtonClose)
        val saveButton = dialog.findViewById<Button>(R.id.buttonSave)
        saveButton.setOnClickListener {
            val txtToDo = dialog.findViewById<TextView>(R.id.editTextTaskContent).text
            val isDone = false
            val newTask = Task(txtToDo.toString(), isDone, countTasks)
            taskReference.child(countTasks.toString()).setValue(newTask)
            createFragment(fragmentManager,newTask,false)
            countTasks++
            countReference.setValue(countTasks)
            dialog.dismiss()
        }
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }
    fun createFragment(fragmentManager: FragmentManager, task: Task, search : Boolean) {
        val fragMan = fragmentManager
        val fragTransaction = fragMan.beginTransaction()
        val myFrag = FragmentTask(task, search)
        fragTransaction.add(R.id.scrollableLinearLayout, myFrag, "$countTasks")
        fragTransaction.commit()

    }

}