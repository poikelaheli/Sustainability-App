package com.example.sustainabilityapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBService (context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION){
    private var tableNames = listOf<String>(USER_TABLE)
    companion object {
        private const val DATABASE_NAME = "Sustainability App"
        private const val DATABASE_VERSION = 1
        const val USER_TABLE = "User"
        const val USER_ID = "userid"
        const val USERNAME_COL = "username"
        const val PASSWORD_COL = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $USER_TABLE (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USERNAME_COL VARCHAR(100) UNIQUE,
                $PASSWORD_COL VARCHAR(200) 
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        dropAll(db)
        onCreate(db)
    }

    fun dropAll(db: SQLiteDatabase?) {
        tableNames.forEach { name ->  db?.execSQL("DROP TABLE IF EXISTS $name")}
    }

    fun addUser (username: String, password: String) {
        val values = ContentValues().apply {
            put(USERNAME_COL, username)
            put(PASSWORD_COL, password)
        }

        writableDatabase.use { db ->
            db.insert(USER_TABLE, null, values)
        }
    }
    fun getUser (username: String): Cursor {
        var query = "SELECT * FROM User WHERE username = \"$username\""
        return readableDatabase.rawQuery(query, null)
    }
}