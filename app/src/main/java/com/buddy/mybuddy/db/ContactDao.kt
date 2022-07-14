package com.buddy.mybuddy.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ContactDao {

    @Query("SELECT * FROM ContactEntity")
    fun getAll(): List<ContactEntity>

    @Query("SELECT contactID FROM ContactEntity")
    fun getAllContacts(): Array<String>

    @Insert
    suspend fun insert(contact: ContactEntity):Long

    @Query("delete from ContactEntity where id=:id")
    fun delRec(id: Long)

    @Query("delete from ContactEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM ContactEntity where id=:id")
    fun getRecord(id:Long):ContactEntity
}
