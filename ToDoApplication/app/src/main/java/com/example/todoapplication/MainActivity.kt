package com.example.todoapplication
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.*

import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.todo.Task
import com.example.todoapplication.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {


    private var tasks = ArrayList<Task>()
    private var countTasks = 0
    private lateinit var binding: ActivityMainBinding
    private val viewModel : ItemViewModel by viewModels()
    private lateinit var userId : String
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)



        val database = FirebaseDatabase.getInstance()
        val countReference = database.getReference("count")
        val userReference = database.getReference("users")


        if(intent.getStringExtra("userId") !=null){
            userId = intent.getStringExtra("userId")!!
        }

        countReference.get().addOnSuccessListener {
            countTasks=it.getValue(Int::class.java)!!
        }
        val userListenerStart = (object  : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.firstOrNull{
                        it.key == userId
                    }?.children?.forEach{
                        createFragment(it.getValue(Task::class.java)!!, false)
                    }
                }else{
                    countTasks = 0
                    countReference.setValue(0)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        userReference.addListenerForSingleValueEvent(userListenerStart)
        viewModel.selectedItem.observe(this) { selectedTask ->
            val userListener = (object : ValueEventListener{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()) {
                        dataSnapshot.children.firstOrNull {
                            it.key == userId
                        }?.children?.filter { it.getValue(Task::class.java)?.taskId == selectedTask.taskId }
                            ?.forEach { _ ->
                                userReference.child(userId).child(selectedTask.taskId.toString())
                                    .removeValue()
                            }
                        createToast("Deletion successful.")
                    }
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onCancelled(error: DatabaseError) {
                    createToast("Failed deletion!!")
                }

            })
            userReference.addListenerForSingleValueEvent(userListener)
        }
        viewModel.checkedItem.observe(this) { checkedTask ->
            val userListener = (object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val updateTaskMap = HashMap<String?, Any?>()
                    updateTaskMap["complete"] = checkedTask.isComplete
                    updateTaskMap["content"] = checkedTask.content
                    updateTaskMap["taskId"] = checkedTask.taskId
                    userReference.child(userId).child(checkedTask.taskId.toString()).updateChildren(updateTaskMap)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onCancelled(error: DatabaseError) {
                    createToast("Failed process!!")
                }
            })
            userReference.addListenerForSingleValueEvent(userListener)
        }
        binding.checkBoxFilter.setOnClickListener {
            when(binding.checkBoxFilter.isChecked){
                true -> {
                    for(fragment in binding.scrollableLinearLayout.children){
                        val fragmentCheckBox = fragment.findViewById<CheckBox>(R.id.checkBox)
                        if (!fragmentCheckBox.isChecked){
                            fragment.isVisible = false

                        }
                    }
                }
                false ->{
                    for(fragment in binding.scrollableLinearLayout.children){
                        fragment.isVisible = true
                    }
                }
            }

        }
        binding.imageButtonSearch.setOnClickListener {
            val search = binding.textViewSearch.text.trim().toString()
            val regex = Regex(search, RegexOption.IGNORE_CASE)
            if (search != "") {
                val matchedTasks = (binding.scrollableLinearLayout as ViewGroup).children.groupBy {
                    val fragmentTextView = it.findViewById<TextView>(R.id.textViewTaskContent)
                    fragmentTextView.text.let { regex.containsMatchIn(fragmentTextView.text) } }
                matchedTasks[true] ?.forEach {
                    val fragmentTextView = it.findViewById<TextView>(R.id.textViewTaskContent)
                    var spannableString = SpannableString(fragmentTextView.text)
                    val matcher = regex.toPattern().matcher(fragmentTextView.text)
                    while (matcher.find()) {
                        val start = matcher.start()
                        val end = matcher.end()
                        spannableString.setSpan(
                            UnderlineSpan(),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    fragmentTextView.text = spannableString
                    it.isVisible = true
                }
                matchedTasks[false] ?.forEach { it.isVisible= false;Log.e("In false ", "-1") }

            }else{
                (binding.scrollableLinearLayout as ViewGroup).children.forEach{ it.isVisible = true; Log.e("In empty ", "3")}
            }
        }
        binding.imageButtonAdd.setOnClickListener{
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.activity_add_popup_screen)
            val closeButton = dialog.findViewById<ImageButton>(R.id.imageButtonClose)
            val saveButton = dialog.findViewById<Button>(R.id.buttonSave)
            saveButton.setOnClickListener {
                val txtToDo = dialog.findViewById<TextView>(R.id.editTextTaskContent).text
                val isDone = false
                val newTask = Task(txtToDo.toString(), isDone, countTasks)
                tasks.add(newTask)
                userReference.child(userId).child(countTasks.toString()).setValue(newTask)
                createFragment(newTask,false)
                countTasks++
                countReference.setValue(countTasks)
                dialog.dismiss()
            }
            closeButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        binding.btnSettings.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.activity_delete_task_popup_screen)
            val signOut = dialog.findViewById<Button>(R.id.buttonCancel)
            val deleteAccount = dialog.findViewById<Button>(R.id.buttonDelete)
            val txtView = dialog.findViewById<TextView>(R.id.textViewDelete)
            txtView.text = "Are You Sure?"
            signOut.let {
                it.text = "Out"
                it.setOnClickListener {
                    dialog.dismiss()
                    signOutAndStartSignInActivity()
                }
                Firebase.auth.signOut()
            }
            deleteAccount.let {
                it.text = "Delete"
                dialog.dismiss()
                it.setOnClickListener {
                    val userListener = (object : ValueEventListener{
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()) {
                                userReference.child(userId).removeValue()
                            }
                        }
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onCancelled(error: DatabaseError) {
                            createToast("Failed deletion!!")
                        }

                    })
                    userReference.addListenerForSingleValueEvent(userListener)
                    var intent = Intent(this,StartPage::class.java)
                    startActivity(intent)
                }
            }
            dialog.show()
        }
    }
    fun createFragment(task: Task, search : Boolean) {
        val fragMan = supportFragmentManager
        val fragTransaction = fragMan.beginTransaction()
        val myFrag = FragmentTask(task, search)
        fragTransaction.add(R.id.scrollableLinearLayout, myFrag, "$countTasks")
        fragTransaction.commit()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createToast(text : String){
        val toast = Toast(this)
        toast.apply {
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toastLayout))
            val textView = layout.findViewById<TextView>(R.id.textViewToast)
            textView.text = text
            textView.setAutoSizeTextTypeUniformWithConfiguration(12,100,2, TypedValue.COMPLEX_UNIT_DIP)
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            val intent = Intent(this, StartPage::class.java)
            startActivity(intent)
            finish()
        }
    }


}