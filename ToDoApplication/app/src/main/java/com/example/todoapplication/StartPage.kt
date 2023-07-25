package com.example.todoapplication

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.todoapplication.databinding.ActivityStartPageBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class StartPage : AppCompatActivity() {
    private lateinit var binding : ActivityStartPageBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val userReference = database.getReference("users")
    private var userId="null"
    private var userCount = 0
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start_page)
        auth = Firebase.auth
        mAuth = FirebaseAuth.getInstance()

        if(mAuth.currentUser?.uid?.toString()?.isNotEmpty()==true){
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("userId", mAuth.currentUser?.uid)
            startActivity(intent)
        }



        val currentUser = auth.currentUser

        /*if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // finish the current activity to prevent the user from coming back to the SignInActivity using the back button
        }*/
        binding.btnGoogle.setOnClickListener {
            signIn()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogIn.isClickable = true
        binding.btnLogIn.setOnClickListener {
            var email = binding.editTextEmail.text.toString()
            var password = binding.editTextPassword.text.toString()
            if(email.isNotEmpty() || password.isNotEmpty()){
                signIn(email ,password)
            }else{
                Toast.makeText(this, "Please enter necessary fields!!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSignIn.setOnClickListener {
            Log.e(TAG, "clicked")
            binding.btnLogIn.isVisible=false
            binding.btnLogIn.isClickable= false
            binding.btnSignIn.isClickable = false
            binding.btnSignIn.isVisible = false
            binding.textView.isVisible = false
            val buttonSignIn = Button(this)
            val editTextPassword = EditText(this)
            buttonSignIn.text = "SIGN"
            buttonSignIn.setTextColor(resources.getColor(R.color.white))
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.horizontalBias = 0.495f
            layoutParams.verticalBias = 0.559f
            buttonSignIn.layoutParams = layoutParams
            buttonSignIn.width = binding.btnLogIn.width
            buttonSignIn.width = binding.btnLogIn.width
            buttonSignIn.backgroundTintList = resources.getColorStateList(R.color.brown)
            buttonSignIn.height = binding.btnLogIn.height
            buttonSignIn.id = R.string.added_sign_in
            binding.startConstraintLayout.addView(buttonSignIn)
            val layoutParams2 = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams2.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams2.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams2.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams2.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            editTextPassword.let {
                it.height = binding.editTextPassword.height
                it.width = binding.editTextPassword.width
                it.textSize = 20F
                it.minHeight = binding.editTextPassword.minHeight
                it.inputType = binding.editTextPassword.inputType
                it.background = resources.getDrawable(R.drawable.custom_border)
                it.setPadding(10,0,0,0)
                it.hint="Enter Password Again..."
                it.setHintTextColor(resources.getColorStateList(R.color.brown))
                it.gravity= binding.editTextPassword.gravity
            }
            layoutParams2.horizontalBias = 0.508f
            layoutParams2.verticalBias = 0.485f
            editTextPassword.layoutParams = layoutParams2
            binding.startConstraintLayout.addView(editTextPassword)
            val layoutParams3 =  binding.btnGoogle.layoutParams as ConstraintLayout.LayoutParams
            layoutParams3.horizontalBias = 0.508f
            layoutParams3.verticalBias = 0.690f
            binding.btnGoogle.width = binding.btnLogIn.width
            binding.btnGoogle.layoutParams = layoutParams3
            val layoutParams4 = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams4.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams4.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams4.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams4.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            val textViewOr = TextView(this)
            layoutParams4.verticalBias = 0.639F
            textViewOr.let {
                it.height = 200
                it.width = 100
                it.setBackgroundResource(R.color.light_pink)
                it.setTextColor(resources.getColor(R.color.brown))
                it.setPadding(40,0,0,0)
                it.text = "or"
                it.textSize = 24F
                it.gravity = R.string.textView_gravity_bottom
                it.layoutParams = layoutParams4
            }
            binding.startConstraintLayout.addView(textViewOr)
            val imageButton = ImageButton(this)
            val layoutParams5 = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams5.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams5.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams5.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams5.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            imageButton.let {
                it.setBackgroundColor(resources.getColor(android.R.color.transparent))
                it.setImageResource(R.drawable.back_icon_foreground)
            }
            layoutParams5.horizontalBias = 0.00001F
            layoutParams5.verticalBias = 0.0001F
            imageButton.layoutParams = layoutParams5
            binding.startConstraintLayout.addView(imageButton)

            imageButton.setOnClickListener {
                binding.startConstraintLayout.removeView(editTextPassword)
                binding.startConstraintLayout.removeView(textViewOr)
                binding.startConstraintLayout.removeView(buttonSignIn)
                binding.startConstraintLayout.removeView(imageButton)
                binding.btnLogIn.isClickable = true
                binding.btnLogIn.isVisible = true
                binding.textView.isVisible = true
                binding.btnSignIn.isClickable = true
                binding.btnSignIn.isVisible = true
                val layoutParams6 =  binding.btnGoogle.layoutParams as ConstraintLayout.LayoutParams
                layoutParams6.horizontalBias = 0.500f
                layoutParams6.verticalBias = 0.95f
                binding.btnGoogle.width = binding.btnLogIn.width
                binding.btnGoogle.layoutParams = layoutParams6
            }
            buttonSignIn.setOnClickListener {
                if(editTextPassword.text.isNotEmpty() && binding.editTextPassword.text.isNotEmpty() && binding.editTextEmail.text.isNotEmpty()){
                    if(editTextPassword.text.isNotEmpty() == binding.editTextPassword.text.isNotEmpty()){
                        createUser(binding.editTextEmail.text.toString(),binding.editTextPassword.text.toString())
                    }else{
                        Toast.makeText(this,"Please fill the necessary fields",Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }

    }

    private fun createUser(email : String, password : String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                    user?.let {
                        val uid = it.uid
                        userReference.child(uid).setValue("tasks")
                        userCount++
                    }
                    Toast.makeText(this, "Sign is successful.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG,email)
                    Log.e(TAG, password)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    // user?.uid?.let { userReference.child(it).setValue("tasks") }
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("userId", user?.uid)
                    Log.e("Google", user?.uid.toString())
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun signIn(email : String, password : String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                    user?.let {
                        val uid = it.uid
                        userId=uid
                        var intent = Intent(this,MainActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                    }
                    Toast.makeText(this, "Sign In successful.",Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                }
            }
    }

}