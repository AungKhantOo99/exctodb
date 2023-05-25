package com.ako.myexcel

import android.content.ContentValues
import android.util.Log
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

object ExcelHelper {
    const val Tablename = "MyTable1"
    const val id = "_id" // 0 integer
    const val Company = "Company" // 1 text(String)
    const val Product = "Product" // 2 text(String)
    const val Price = "Price" // 3 text(String)
    fun insertExcelToSqlite(dbAdapter: DBHelper, sheet: Sheet) {
        val rit = sheet.rowIterator()
        while (rit.hasNext()) {
            val row = rit.next()
            val contentValues = ContentValues()
            row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).cellType =
                CellType.STRING
            row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).cellType =
                CellType.STRING
            row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).cellType =
                CellType.STRING
            contentValues.put(
                Company,
                row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).stringCellValue
            )
            contentValues.put(
                Product,
                row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).stringCellValue
            )
            contentValues.put(
                Price,
                row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).stringCellValue
            )
            try {
                if (dbAdapter.insert("MyTable1", contentValues) < 0) {
                    return
                }
            } catch (ex: Exception) {
                Log.d("Exception in importing", ex.message.toString())
            }
        }
    }
}