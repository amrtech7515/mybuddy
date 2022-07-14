package com.buddy.mybuddy.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.buddy.mybuddy.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


open class MainActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123

    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var sharedpreferences: SharedPreferences

    lateinit var txtLogin: TextView


    open lateinit var vm: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var viewModelFactory = LoginVMFactory()

        vm = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
        sharedpreferences = this.getSharedPreferences("mybuddy_pref", Activity.MODE_PRIVATE);

        FirebaseApp.initializeApp(applicationContext)

        init_google_signin()
        if ((GoogleSignIn.getLastSignedInAccount(this) != null) && sharedpreferences.contains("email")) {


            vm._loginstatus.value=true
           // showFragment(frgHome)
            /*  startActivity(
                  Intent(
                      this, HomeActivity
                      ::class.java
                  )
              )
              finish()*/
        } else {
           // showFragment(frgMain)
            vm._loginstatus.value=false
            signInGoogle()
        }

        vm.loginstatus.observe(this, { v ->
            if(v) {
                txtLogin.text = "Logout"
              //  showFragment(frgHome)
            }
            else {
                txtLogin.text = "Login"
               // showFragment(frgMain)
            }
        })

        txtLogin.setOnClickListener {
            if(vm._loginstatus.value==true)
            {
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    // val intent = Intent(this, MainActivity::class.java)
                    //  Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
                    //  startActivity(intent)
                    //  finish()
                    // initUI()
                    //   btnLogout.isChecked=false


                    vm._loginstatus.value=false
                }

            }
            else
                signInGoogle()
        }
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // onActivityResult() function : this is where
    // we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // this is where we update the UI after Google signin takes place
    private fun UpdateUI(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveMessage("email", account.email.toString())
                saveMessage("unm", account.displayName.toString())


                vm._loginstatus.value=true

              //  val intent = Intent(this, HomeActivity::class.java)
               // startActivity(intent)
               // finish()
            } else {


                vm._loginstatus.value =false
            }
        }
    }

    fun saveMessage(k: String?, msg: String?) {
        val editor = sharedpreferences!!.edit()
        // below lines will put values for
        // message in shared preferences.
        editor.putString(k, msg)
        // to save our data with key and value.
        editor.apply()
        // on below line we are displaying a toast message after adding data to shared prefs.
        Toast.makeText(this, "Message saved to Shared Preferences", Toast.LENGTH_SHORT).show()
        // after that we are setting our edit text to empty
        // messageEdt.setText("")
    }

    fun init_google_signin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("634645528682-ie9j3oes3i4h207jl2pst72rq6pi2105.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

    }

}