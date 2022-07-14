package com.buddy.mybuddy.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val contactID:String,
   val dt:String
)