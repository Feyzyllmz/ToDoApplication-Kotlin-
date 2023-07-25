package com.example.todoapplication



import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.todo.Task
import com.example.todoapplication.databinding.TaskFragmentBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"




/**
 * A simple [Fragment] subclass.
 * Use the [FragmentTask.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentTask(var task : Task, var search : Boolean) : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var viewModel : ItemViewModel
    private lateinit var binding: TaskFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.task_fragment, container, false)
        binding.textViewTaskContent.textSize = 18.0f
        val contentLength = task.content?.length
        if(contentLength!! >28){
            var splitContent = task.content?.substring(0,28) + '\n' + task.content?.substring(28,task.content!!.length)
            Log.e("dfkfgdföç",splitContent)
            binding.textViewTaskContent.textSize = 18.0f
            if (contentLength>56){
                splitContent = task.content?.substring(0,28) + '\n' + task.content?.substring(28,56) + "..."
            }
            binding.textViewTaskContent.text=splitContent

        }else{
            binding.textViewTaskContent.textSize = 24.0f
            binding.textViewTaskContent.text = task.content
        }

        viewModel = ViewModelProvider(requireActivity())[ItemViewModel::class.java]
        if(task.isComplete==true){
            binding.textViewTaskContent.paintFlags = binding.textViewTaskContent.paintFlags.or(Paint.STRIKE_THRU_TEXT_FLAG)!!
            binding.checkBox.isChecked = true
        }
        binding.textViewTaskContent.setOnLongClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.activity_delete_task_popup_screen)
            val closeButton = dialog.findViewById<Button>(R.id.buttonCancel)
            val deleteButton = dialog.findViewById<Button>(R.id.buttonDelete)
            deleteButton.setOnClickListener {
                viewModel.setSelectItem(task)
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.beginTransaction()
                    .remove(this@FragmentTask)
                    .commit()
                dialog.dismiss()
            }
            closeButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
            true

        }
        binding.checkBox.setOnClickListener {
            if(binding.checkBox.isChecked) {
                binding.textViewTaskContent.paintFlags = binding.textViewTaskContent.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                task.isComplete=true
                viewModel.setCheckedItem(task)

            }else {
                binding.textViewTaskContent.paintFlags = binding.textViewTaskContent.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                task.isComplete = false
                viewModel.setCheckedItem(task)
            }
            binding.textViewTaskContent.invalidate()


        }


        return binding.root
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaskFragment.
         */
        // TODO: Rename and change types and number of parameters
        private const val REQUEST_CODE = 1
        @JvmStatic
        fun newInstance(task: Task, search : Boolean): FragmentTask {
            val fragment = FragmentTask(task, search)
            val args = Bundle().apply {
                putSerializable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }

}