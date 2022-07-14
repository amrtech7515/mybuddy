package com.buddy.mybuddy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.buddy.mybuddy.R
import com.buddy.mybuddy.adapters.HomeContactListAdapter
import com.buddy.mybuddy.db.ContactDB
import com.buddy.mybuddy.db.DB_Builder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class HomeActivity : AppCompatActivity() {

    @RequiresApi(api = Build.VERSION_CODES.O)
    var auth = FirebaseAuth.getInstance()
    lateinit var db: ContactDB
    var contactList: MutableList<String> = mutableListOf<String>()
    lateinit var contact_list_view:ListView
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        db = DB_Builder.getInstance(this)
        var btnSend = findViewById<Button>(R.id.btnSend)
        var btnAdd = findViewById<Button>(R.id.btnAdd)
        contact_list_view = findViewById<ListView>(R.id.contactlist)

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        setuptoolbar()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 100
            )
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {

            val hashMapContacts = HashMap<String?, String?>()
        GlobalScope.launch {
            var args = getDataFromDB()
            if (args != null && args.size > 0) {
                Log.i("chk list hr", args.size.toString())

            }
            if (args != null) {
                val colArr: Array<String> = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,    // Contract class constant for the _ID column name
                    ContactsContract.CommonDataKinds.Phone.NUMBER   // Contract class constant for the word column name
                    // Contract class constant for the locale column name
                )
                val argcount = args.size // number of IN arguments
                val inList = StringBuilder(argcount * 2)
                for (i in 0 until argcount) {
                    if (i > 0) {
                        inList.append(",")
                    }
                    inList.append("?")
                }
                Log.i("chk arg", inList.toString())
                var phone: Cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    colArr,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " IN ($inList)",
                    args,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )!!
                Log.i("chk tot rec", phone.getCount().toString())
                if (phone.getCount() > 0) {
                    while (phone.moveToNext()) {
                        hashMapContacts.put(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)),phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))

                                }
                    Log.i("chk tot db rec",hashMapContacts.size.toString())
                    var a=hashMapContacts.values.toList()
                    var adpt = HomeContactListAdapter(this@HomeActivity,a)

                    runOnUiThread {
                        contact_list_view.adapter = adpt
                        hashMapContacts.forEach { (key, value) -> println("$key = $value") }
                    }

                            }

            }
        }}

        btnSend.setOnClickListener {
            // send alert to saved 10 contacts
        }
        btnAdd.setOnClickListener {
            // send alert to saved 10 contacts
           // val intent = Intent(this, ContactListActivity::class.java)
            val intent = Intent(this, AddContactActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@HomeActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {

                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }


    }
    fun setuptoolbar() {
        @SuppressLint("WrongViewCast")
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        // toolbar.setTitle("")
        setActionBar(toolbar)
        var logout = toolbar.findViewById<TextView>(R.id.txtLogout)
        logout.setOnClickListener {
            auth.signOut()
            finish()
        }
    }


    suspend fun getDataFromDB(): Array<String> {

        val ContactData = GlobalScope.async {
            db.ContactDao().getAllContacts()
        }
        ContactData.await()
        return ContactData.getCompleted()
    }

}


