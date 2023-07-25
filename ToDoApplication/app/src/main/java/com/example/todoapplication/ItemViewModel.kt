package com.example.todoapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todo.Task
import java.util.Objects

class ItemViewModel : ViewModel() {

     var selectedItem : MutableLiveData<Task> = MutableLiveData<Task>()
     var checkedItem : MutableLiveData<Task> = MutableLiveData<Task>()


     fun setSelectItem(task : Task){
          selectedItem.value = task
     }
     fun setCheckedItem(task : Task){
          checkedItem.value = task
     }


}