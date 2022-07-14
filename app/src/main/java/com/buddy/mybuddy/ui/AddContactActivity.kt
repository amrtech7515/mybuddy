package com.buddy.mybuddy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.RawContacts
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddContactActivity : AppCompatActivity() {
    var TAG = "chkhere"
    lateinit var db: ContactDB
    var CONTACT_LIST:MutableList<String> = mutableListOf<String>()
    lateinit var contact_list_view:ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 3 else 0

        db = DB_Builder.getInstance(this)
         contact_list_view = findViewById<ListView>(R.id.contactlist)
        var btnAdd = findViewById<Button>(R.id.btnAdd)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        load_contacts_in_list()
        btnAdd.setOnClickListener {
            GlobalScope.launch {
                var dt =
                    SimpleDateFormat("MMM dd yyyy,hh:mm:ss", Locale.getDefault()).format(Date())


                CONTACT_LIST.forEach {
                    Log.i("chech contact id",it!!)
                    var a = ContactEntity(
                        0,
                        it,
                        dt.toString()
                    )
                    Log.i(TAG,"inserting"+a.contactID)
                    db.ContactDao()!!.insert(a)
                    val intent = Intent(this@AddContactActivity, HomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }


    }
    fun load_contacts_in_list()
    {
        val contactList = getContacts()
        println("Contact List : $contactList")
        if (contactList == null) {
            Log.i(TAG, "its null")
        } else {
            if (contactList!!.size > 0) {
                Log.d(TAG, contactList.size.toString())
                val contactValueList = contactList.values.toList()
                println("chklist"+contactValueList)
                var adpt = ContactListAdapter(this,contactValueList)
                contact_list_view.adapter = adpt
            }
        }
    }


    fun getContacts(): HashMap<Int?, DtContact?> {
        val contacts = HashMap<Int?, DtContact?>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), 100
            )
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            runBlocking {
                launch {


                    val projection = arrayOf(RawContacts.CONTACT_ID, RawContacts.DELETED)
                    val rawContacts: Cursor =
                        managedQuery(RawContacts.CONTENT_URI, projection, null, null, null)
                    val contactIdColumnIndex: Int =
                        rawContacts.getColumnIndex(RawContacts.CONTACT_ID)
                    val deletedColumnIndex: Int = rawContacts.getColumnIndex(RawContacts.DELETED)
                    if (rawContacts.moveToFirst()) {
                        while (!rawContacts.isAfterLast()) {
                            val contactId: Int = rawContacts.getInt(contactIdColumnIndex)
                            val deleted = rawContacts.getInt(deletedColumnIndex) === 1
                            if (!deleted) {
                                val contactInfo: HashMap<Int?, ArrayList<String>?> =
                                    object : HashMap<Int?, ArrayList<String>?>() {
                                        /* init {
                            put("contactId", ArrayList<String>())
                            put("name", "")
                            put("email", "")
                            put("address", "")
                            put("photo", "")
                            put("phone", "")
                        }*/
                                    }


                                contacts.put(
                                    contactId,
                                    DtContact(
                                        contactId.toString(),
                                        getName(contactId)!!,
                                        getPhoneNumber(contactId)!!
                                    )
                                )
                                /* contactInfo["contactId"] = "" + contactId
                    contactInfo["name"] = getName(contactId)
                    contactInfo["email"] = getEmail(contactId)
                    contactInfo["photo"] =
                        if (getPhoto(contactId) != null) getPhoto(contactId) else ""
                    contactInfo["address"] = getAddress(contactId)
                    contactInfo["phone"] = getPhoneNumber(contactId)
                    contactInfo["isChecked"] = "false"
                    contacts.put(contactId,contactInfo)*/
                            }
                            rawContacts.moveToNext()
                        }
                    }
                    rawContacts.close()

                }
            }
        }
            return contacts
        }

    @SuppressLint("Range")
    private fun getName(contactId: Int): String? {
        var name = ""
        val projection = arrayOf<String>(ContactsContract.Contacts.DISPLAY_NAME)
        val contact: Cursor = managedQuery(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            ContactsContract.Contacts._ID.toString() + "=?",
            arrayOf(contactId.toString()),
            null
        )
        if (contact.moveToFirst()) {
            name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            contact.close()
        }
        contact.close()
        return name
    }

    private fun getEmail(contactId: Int): String? {
        var emailStr = ""
        val projection = arrayOf(
            Email.DATA,  // use
            // Email.ADDRESS
            // for API-Level
            // 11+
            Email.TYPE
        )
        val email: Cursor = managedQuery(
            Email.CONTENT_URI,
            projection,
            ContactsContract.Data.CONTACT_ID.toString() + "=?",
            arrayOf(contactId.toString()),
            null
        )
        if (email.moveToFirst()) {
            val contactEmailColumnIndex: Int = email.getColumnIndex(Email.DATA)
            while (!email.isAfterLast()) {
                emailStr = emailStr + email.getString(contactEmailColumnIndex).toString() + ";"
                email.moveToNext()
            }
        }
        email.close()
        return emailStr
    }

    @SuppressLint("Range")
    private fun getPhoto(contactId: Int): Bitmap? {
        var photo: Bitmap? = null
        val projection = arrayOf<String>(ContactsContract.Data.PHOTO_ID)
        val contact: Cursor = managedQuery(
            ContactsContract.Data.CONTENT_URI,
            projection,
            ContactsContract.Data._ID.toString() + "=?",
            arrayOf(contactId.toString()),
            null
        )
        if (contact.moveToFirst()) {
            val photoId: String = contact.getString(contact.getColumnIndex(ContactsContract.Data.PHOTO_ID))
            photo = if (photoId != null) {
                getBitmap(photoId)
            } else {
                null
            }
        }
        contact.close()
        return photo
    }

    @SuppressLint("Range")
    private fun getBitmap(photoId: String): Bitmap? {
        val photo: Cursor = managedQuery(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Contacts.Photo.PHOTO),
            ContactsContract.Data._ID.toString() + "=?",
            arrayOf(photoId),
            null
        )
        val photoBitmap: Bitmap?
        photoBitmap = if (photo.moveToFirst()) {
            val photoBlob: ByteArray = photo.getBlob(photo.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO))
            BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.size)
        } else {
            null
        }
        photo.close()
        return photoBitmap
    }

    @SuppressLint("Range")
    private fun getAddress(contactId: Int): String? {
        var postalData = ""
        val addrWhere =
            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"
        val addrWhereParams = arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
        )
        val addrCur: Cursor =
            managedQuery(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null)
        if (addrCur.moveToFirst()) {
            postalData =
                addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
        }
        addrCur.close()
        return postalData
    }

    private fun getPhoneNumber(contactId: Int): String? {
        var phoneNumber = ""
        val projection = arrayOf(Phone.NUMBER, Phone.TYPE)
        val phone: Cursor = managedQuery(
            Phone.CONTENT_URI,
            projection,
            ContactsContract.Data.CONTACT_ID.toString() + "=?",
            arrayOf(contactId.toString()),
            null
        )
        if (phone.moveToFirst()) {
            val contactNumberColumnIndex: Int = phone.getColumnIndex(Phone.DATA)
            while (!phone.isAfterLast()) {
                phoneNumber =
                    phoneNumber + phone.getString(contactNumberColumnIndex).toString() + ";"
                phone.moveToNext()
            }
        }
        phone.close()
        return phoneNumber
    }
}