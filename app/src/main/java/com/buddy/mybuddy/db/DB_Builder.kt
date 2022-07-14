package com.buddy.mybuddy.db

import android.content.Context
import androidx.room.Room

object DB_Builder {

        private var INSTANCE: ContactDB? = null
        fun getInstance(context: Context): ContactDB {
            if (INSTANCE == null) {
                synchronized(ContactDB::class) {
                    INSTANCE = buildRoomDB(context)
                }
            }
            return INSTANCE!!
        }
        private fun buildRoomDB(context: Context) =
            Room.databaseBuilder(
                context,
                ContactDB::class.java,
                "coroutines"
            ).build()
    }

