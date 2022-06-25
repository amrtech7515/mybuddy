package com.buddy.mybuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        var btnSend=findViewById<Button>(R.id.btnSend)
        btnSend.setOnClickListener {


        }
    }
}