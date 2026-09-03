package com.example.dailydoes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MYDB(context: Context) :
    SQLiteOpenHelper(context, "medicines.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE medicines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                dose TEXT NOT NULL,
                frequency INTEGER NOT NULL,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                notes TEXT,
                last_taken_date TEXT
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS medicines")
        onCreate(db)
    }

    fun insertMedicine(
        name: String,
        dose: String,
        frequency: Int,
        date: String,
        time: String,
        notes: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("dose", dose)
        values.put("frequency", frequency)
        values.put("date", date)
        values.put("time", time)
        values.put("notes", notes)
        values.put("last_taken_date", "")
        
        val id = db.insert("medicines", null, values)
        db.close()
        return id
    }

    fun updateMedicine(
        id: Int,
        name: String,
        dose: String,
        frequency: Int,
        date: String,
        time: String,
        notes: String
    ) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("dose", dose)
        values.put("frequency", frequency)
        values.put("date", date)
        values.put("time", time)
        values.put("notes", notes)

        db.update("medicines", values, "id=?", arrayOf(id.toString()))
        db.close()
    }
    
    fun markAsTaken(id: Int, date: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("last_taken_date", date)
        }
        db.update("medicines", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteMedicine(id: Int) {
        val db = writableDatabase
        db.delete("medicines", "id=?", arrayOf(id.toString()))
    }


    fun getAllMedicines(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM medicines ORDER BY id DESC",
            null
        )

        while (cursor.moveToNext()) {
            val map = HashMap<String, String>()
            map["id"] = cursor.getInt(cursor.getColumnIndexOrThrow("id")).toString()
            map["name"] = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            map["dose"] = cursor.getString(cursor.getColumnIndexOrThrow("dose"))
            map["frequency"] =
                cursor.getInt(cursor.getColumnIndexOrThrow("frequency")).toString()
            map["date"] = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            map["time"] = cursor.getString(cursor.getColumnIndexOrThrow("time"))
            map["notes"] = cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: ""
            map["last_taken_date"] = cursor.getString(cursor.getColumnIndexOrThrow("last_taken_date")) ?: ""
            list.add(map)
        }

        cursor.close()
        db.close()
        return list
    }
}
