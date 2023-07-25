package com.example.todo

import android.text.SpannableString
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(var content : String?, var isComplete : Boolean?, var taskId : Int?) : java.io.Serializable {
    constructor() : this(null,null,null) {}


}