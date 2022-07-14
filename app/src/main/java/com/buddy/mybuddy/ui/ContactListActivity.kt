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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.buddy.mybuddy.R
import com.buddy.mybuddy.adapters.ContactListAdapter
import com.buddy.mybuddy.db.ContactDB
import com.buddy.mybuddy.db.ContactEntity
import com.buddy.mybuddy.db.DB_Builder
import com.buddy.mybuddy.models.DtContact
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ContactListActivity : AppCompatActivity() {
    var TAG = "chkhere"
    private val photoDataIndex: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 3 else 0
    var contactList: MutableList<String> = mutableListOf<String>()

    lateinit var db: ContactDB
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        db = DB_Builder.getInstance(this)
        var contact_list = findViewById<ListView>(R.id.contactlist)
        var btnAdd = findViewById<Button>(R.id.btnAdd)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        GlobalScope.launch {
            var contact_list = getDataFromDB()
            println("chk list" + contact_list)
        }

        var arr = showContacts()
        //  var adpt= arr?.let { it1 -> GridAdapter(this, it1) }
        if (arr == null) {
            Log.i(TAG, "its null")
        } else {
            if (arr!!.size > 0) {
                Log.d(TAG, arr.size.toString())
             //   var adpt = ContactListAdapter(this, arr)
              //  contact_list.adapter = adpt
            }
        }

        btnAdd.setOnClickListener {
            GlobalScope.launch {
                var dt =
                    SimpleDateFormat("MMM dd yyyy,hh:mm:ss", Locale.getDefault()).format(Date())
                Log.i("chk contact size",contactList.count().toString())

                contactList.forEach {
                    Log.i("chech contact id",it)
                    var a = ContactEntity(
                        0,
                        it,
                        dt.toString()
                    )
                    Log.i(TAG,"inserting"+a.contactID)
                    db.ContactDao()!!.insert(a)
                    val intent = Intent(this@ContactListActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("Range")
    fun showContacts(): ArrayList<DtContact>? {
        var a: ArrayList<DtContact>? = arrayListOf()
        var ph_name: String
        var ph_num: String
        var ph_id: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 100
            )
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            runBlocking {
                launch {
                    var phone: Cursor = contentResolver.query(
                         ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        null,
                        null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                    )!!
                    //  Log.i("tooot--->", phone.getCount().toString())
                    if (phone.getCount() >= 1) {

                        while (phone.moveToNext()) {
                            ////////////////get photo////////////////////
                            ph_id =
                                phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                            /////////////////////////////////////
                            ph_name =
                                phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))

                            ph_num =
                                phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            //phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                            a?.add(DtContact(ph_id, ph_name, ph_num))
                        }
                    }
                    phone.close()
                }// Android version is lesser than 6.0 or the permission is already granted.
                Log.i(TAG, "tot cont=>" + a!!.size)

            }
        }
        return a
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
                            this@ContactListActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        showContacts()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }


    }
    suspend fun getDataFromDB():List<ContactEntity> {

        val ContactData = GlobalScope.async {
            db.ContactDao().getAll()
        }
        ContactData.await()
        return ContactData.getCompleted()
    }


}

/////////////////////////








