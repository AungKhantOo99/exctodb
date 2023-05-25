package com.ako.myexcel

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context?) {
    var TAG = "DBAdapter"
    private var db: SQLiteDatabase? = null
    private val dbHelper: Database

    init {
        dbHelper = Database(context)
    }

    fun open() {
        if (null == db || !db!!.isOpen) {
            try {
                db = dbHelper.writableDatabase
            } catch (sqLiteException: SQLiteException) {
            }
        }
    }

    fun close() {
        if (db != null) {
            db!!.close()
        }
    }

    fun insert(table: String?, values: ContentValues?): Int {
        return try {
            db = dbHelper.writableDatabase
            db?.insert(table, null, values)
            db?.close()
            Log.e("Data Inserted", "Data Inserted")
            1
        } catch (ex: Exception) {
            Log.e("Error Insert", ex.message.toString())
            0
        }
    }

    fun delete() {
        db!!.execSQL("delete from " + Tablename)
    }

    fun getAllRow(table: String?): Cursor {
        return db!!.query(table, null, null, null, null, null, id)
    }

    val products: ArrayList<HashMap<String, String>>
        get() {
            val prolist: ArrayList<HashMap<String, String>>
            prolist = ArrayList()
            val selectQuery = "SELECT * FROM " + Tablename
            val database = dbHelper.readableDatabase
            val cursor = database.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val map = HashMap<String, String>()
                    map[company] = cursor.getString(1)
                    map[product] = cursor.getString(2)
                    map[price] = cursor.getString(3)
                    prolist.add(map)
                } while (cursor.moveToNext())
            }
            return prolist
        }
    private inner class Database(context: Context?) :
        SQLiteOpenHelper(context, "MyDB1.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            val create_sql = ("CREATE TABLE IF NOT EXISTS " + Tablename + "("
                    + id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + company + " TEXT ," + product + " TEXT ,"
                    + price + " TEXT " + ")")
            db.execSQL(create_sql)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + Tablename)
        }
    }

    companion object {
        const val Tablename = "MyTable1"
        const val id = "_id" // 0 integer
        const val company = "Company" // 1 text(String)
        const val product = "Product" // 2 integer
        const val price = "Price" // 3 date(String)
        private const val VERSION = 2
        private const val DB_NAME = "MyDB1.db"
    }
}