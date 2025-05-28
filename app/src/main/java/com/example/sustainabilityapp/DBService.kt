package com.example.sustainabilityapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBService (context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION){
    private var tableNames = listOf<String>(USER_TABLE, DEVICE_TABLE, NETWORK_TABLE)
    companion object {
        private const val DATABASE_NAME = "SustainabilityApp"
        private const val DATABASE_VERSION = 1
        const val USER_TABLE = "User"
        const val USER_ID = "userid"
        const val USERNAME_COL = "username"
        const val PASSWORD_COL = "password"
        const val DEVICE_TABLE = "Device"
        const val DEVICE_ID = "deviceid"
        const val DEVICE_NAME = "name"
        const val DEVICE_STATUS = "status"
        const val DEVICE_NETWORK = "networkid"
        const val NETWORK_TABLE = "Network"
        const val NETWORK_ID = "networkid"
        const val NETWORK_NAME = "name"

        const val TAG = "sustAppDB"
    }

    private var currrentdb: SQLiteDatabase? = null
    private var isCreating = false

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, db.toString())
        currrentdb = db
        isCreating = true
        val createUserTable = """
            CREATE TABLE $USER_TABLE (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USERNAME_COL VARCHAR(100) UNIQUE,
                $PASSWORD_COL VARCHAR(200) 
            )
        """.trimIndent()
        db.execSQL(createUserTable)
        val createNetworkTable = """
            CREATE TABLE $NETWORK_TABLE (
                $NETWORK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $NETWORK_NAME VARCHAR(100) UNIQUE
            )
        """.trimIndent()
        db.execSQL(createNetworkTable)
        val createDeviceTable = """
            CREATE TABLE $DEVICE_TABLE (
                $DEVICE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $DEVICE_NAME VARCHAR(100),
                $DEVICE_STATUS INTEGER,
                $DEVICE_NETWORK INTEGER,
                FOREIGN KEY ($DEVICE_NETWORK) REFERENCES $NETWORK_TABLE($NETWORK_ID)
            )
        """.trimIndent()
        db.execSQL(createDeviceTable)

        isCreating = false
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
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
            db!!.insert(USER_TABLE, null, values)
        }
    }
    fun getUser (username: String): Cursor {
        var query = "SELECT * FROM $USER_TABLE WHERE $USERNAME_COL = \"$username\""
        return readableDatabase!!.rawQuery(query, null)
    }

    fun addNetwork (name: String) {
        val values = ContentValues().apply {
            put(NETWORK_NAME, name)
        }
        writableDatabase.use { db ->
            db!!.insert(NETWORK_TABLE, null, values)
        }
    }

    fun getNetworkByName (name: String): Cursor {
        var query = "SELECT * FROM $NETWORK_TABLE WHERE $NETWORK_NAME = \"$name\""
        return readableDatabase!!.rawQuery(query, null)
    }

    fun getAllNetworks (): Cursor {
        var query = "SELECT * FROM $NETWORK_TABLE"
        return readableDatabase!!.rawQuery(query, null)
    }

    fun addDevice (name: String, status: Int, networkId: Int?) {
        val values = ContentValues().apply {
            put(DEVICE_NAME, name)
            put(DEVICE_STATUS, status)
            put(DEVICE_NETWORK, networkId)
        }
        Log.d(TAG, values.toString())
        writableDatabase.use { db ->
            db!!.insert(DEVICE_TABLE, null, values)
        }
    }

    fun getDevicesByNetwork (network: Int): Cursor {
        var query = "SELECT * FROM $DEVICE_TABLE WHERE $DEVICE_NETWORK = \"$network\""
        return readableDatabase!!.rawQuery(query, null)
    }

    fun addDummyData () {
        Log.d(TAG, "Network")
        addNetwork("TestNetwork")
        Log.d(TAG, "Network done")
        val cursor = getNetworkByName("TestNetwork")
        Log.d(TAG, "cursor")
        val network = mutableMapOf("name" to "", "id" to "")
        Log.d(TAG, "list")
        cursor.use {
            if (cursor.moveToFirst()) {
                network["name"] = cursor.getString(cursor.getColumnIndexOrThrow(NETWORK_NAME))
                network["id"] = cursor.getString(cursor.getColumnIndexOrThrow(NETWORK_ID))
            }
        }
        Log.d(TAG, network.toString())
        addDevice("Test Network Device 1", 2, network["id"]?.toInt())
        addDevice("Test Network Device 2", 3, network["id"]?.toInt())
    }

    fun checkTable (tableName: String) : Boolean {
        var exists = false
        var cursor = readableDatabase!!.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = \"$tableName\"", null)
        if (cursor.count > 0) {
            exists = true;
        }
        return  exists
    }

    fun deleteAll (context: Context) {
        context.deleteDatabase(DATABASE_NAME)
    }


}