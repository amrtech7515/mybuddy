package com.buddy.mybuddy.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buddy.mybuddy.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    var googleApiClient: GoogleApiClient? = null
    var googleSignInClient: GoogleSignInClient? = null
    var SIGN_IN = 1
    var signInOptions: GoogleSignInOptions? = null
    lateinit var auth: FirebaseAuth
    lateinit var sharedpreferences: SharedPreferences
    private var progressDialog: ProgressDialog? = null
    val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        sharedpreferences = this.getSharedPreferences("mybuddy_pref", Activity.MODE_PRIVATE);
        initiateGoogleSingIn()
        if ((GoogleSignIn.getLastSignedInAccount(this) != null) && sharedpreferences.contains("email")) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // showFragment(frgMain)
            signInGoogle()
        }
    }

    fun initiateGoogleSingIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("634645528682-ie9j3oes3i4h207jl2pst72rq6pi2105.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    private fun signInGoogle() {
        val signInIntent: Intent = googleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, SIGN_IN)
    }



    open fun launchSignInAPi() {
//        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            val intent1: Intent = googleSignInClient!!.getSignInIntent()
            startActivityForResult(intent1, SIGN_IN)
        }
    private fun UpdateUI(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveMessage("email", account.email.toString())
                saveMessage("unm", account.displayName.toString())
                val intent = Intent(this, HomeActivity::class.java)
                 startActivity(intent)
                 finish()
            } else {
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

        open fun initiateGoogleSingOut() {
//        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                SharedPreferenceController.getInstance().setBooleanValue(HomeActivity.this,"login_status",false);
//                Log.v(TAG, "logout Success ");
//            }
//        });
            Auth.GoogleSignInApi.signOut(googleApiClient!!).setResultCallback { status ->
                if (status.isSuccess) {
                    Log.v(TAG, "logout Success ")
                    //                    Toast.makeText(HomeActivity.this, "logout Success", Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "logout failed ")
                    //                    Toast.makeText(HomeActivity.this, "logout failed ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)

            if (signInResult!!.isSuccess) {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleResult(task)
                //checklogin();
                progressBar("Sign In....")
                Log.v(TAG, "login success ")
            } else {
                Log.v(
                    TAG,
                    "Login failed " + signInResult.status + " sigin result " + signInResult.isSuccess
                )
                launchSignInAPi()
            }
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


    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            val account = result.signInAccount
        } else {
            Log.v(TAG, "not access data ")
        }
    }

    private fun results() {
        val opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient!!)
        if (opr.isDone) {
            val result = opr.get()
            handleSignInResult(result)
        } else {
            opr.setResultCallback { googleSignInResult -> handleSignInResult(googleSignInResult) }
        }
    }
    private fun progressBar(input: String) {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage(input)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.show()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
    fun logout()
    {
        auth.signOut()
        finish()
    }
}
