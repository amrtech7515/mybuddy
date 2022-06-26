package com.buddy.mybuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        var btnSend = findViewById<Button>(R.id.btnSend)
        var btnAdd = findViewById<Button>(R.id.btnAdd)
        btnSend.setOnClickListener {
            // send alert to saved 10 contacts


        }
        btnAdd.setOnClickListener {
            // send alert to saved 10 contacts
            show_contacts()

        }
    }
    fun show_contacts()
    {
        //add contacts in this fun
    }
   }
//en

