package com.ako.myexcel

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    var lbl: TextView? = null
    var controller: DBHelper? = DBHelper(this)
    var lv: ListView? = null
    private var mLayout: View? = null
    var filePicker: ActivityResultLauncher<Intent>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lbl = findViewById<View>(R.id.txtresulttext) as TextView
        lv = findViewById(R.id.lstView)
        mLayout = findViewById(R.id.main_layout)
        filePicker = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent1 = result.data
                val uri = intent1!!.data
                ReadExcelFile(
                    this@MainActivity, uri
                )
            }
        }
        FillList()
    }

    private fun CheckPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            Snackbar.make(
                mLayout!!, R.string.storage_access_required,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(
                "OK"
            ) { requestStoragePermission() }.show()
            false
        }
    }

    fun FillList() {
        try {
            if (controller == null) {
                val controller = DBHelper(this@MainActivity)
            }
            val myList = controller!!.products
            if (myList.size != 0) {
                lv = findViewById(R.id.lstView)
                val listadapter: ListAdapter = SimpleAdapter(
                    this@MainActivity, myList,
                    R.layout.first, arrayOf<String>(
                        DBHelper.Company, DBHelper.Product,
                        DBHelper.Price
                    ), intArrayOf(
                        R.id.txtproductcompany, R.id.txtproductname,
                        R.id.txtproductprice
                    )
                )
                //lv.adapter=listadapter
                lv?.setAdapter(listadapter)
            }
        } catch (ex: Exception) {
            Toast("FillList error: " + ex.message, ex)
        }
    }

    fun ReadExcelFile(context: Context, uri: Uri?) {
        try {
            val inStream: InputStream?
            var wb: Workbook? = null
            try {
                inStream = context.contentResolver.openInputStream(uri!!)
                wb =
                    if (fileType === extensionXLS) HSSFWorkbook(inStream) else XSSFWorkbook(inStream)
                inStream!!.close()
            } catch (e: IOException) {
                lbl!!.text = "First " + e.message.toString()
                e.printStackTrace()
            }
            val dbAdapter = DBHelper(this)
            val sheet1 = wb!!.getSheetAt(0)
            dbAdapter.open()
            dbAdapter.delete()
            dbAdapter.close()
            dbAdapter.open()
            ExcelHelper.insertExcelToSqlite(dbAdapter, sheet1)
            dbAdapter.close()
            FillList()
        } catch (ex: Exception) {
            Log.d("Error",ex.message.toString())
            Toast("ReadExcelFile Error:" + ex.message.toString(), ex)
        }
    }

    fun ChooseFile() {
        try {
            val fileIntent = Intent(Intent.ACTION_GET_CONTENT)
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE)
            if (fileType === extensionXLS) fileIntent.type =
                "application/vnd.ms-excel" else fileIntent.type =
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            filePicker!!.launch(fileIntent)
        } catch (ex: Exception) {
            Toast("ChooseFile error: " + ex.message.toString(), ex)
        }
    }

    fun Toast(message: String?, ex: Exception?) {
        if (ex != null) Log.e("Error", ex.message.toString())
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_MEMORY_ACCESS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                OpenFilePicker()
            } else {
                Snackbar.make(
                    mLayout!!, R.string.storage_access_denied,
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_MEMORY_ACCESS
            )
        } else {
            Snackbar.make(mLayout!!, R.string.storage_unavailable, Snackbar.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_MEMORY_ACCESS
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_import_xls) {
            fileType = extensionXLS
            OpenFilePicker()
        } else if (id == R.id.action_import_xlxs) {
            fileType = extensionXLXS
            OpenFilePicker()
        }
        return super.onOptionsItemSelected(item)
    }

    fun OpenFilePicker() {
        try {
            if (CheckPermission()) {
                ChooseFile()
            }
        } catch (e: ActivityNotFoundException) {
            lbl!!.text = "No activity can handle picking a file. Showing alternatives."
        }
    }

    companion object {
        init {
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
            )
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
            )
            System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
            )
        }

        private const val PERMISSION_REQUEST_MEMORY_ACCESS = 0
        private var fileType = ""
        private const val extensionXLS = "XLS"
        private const val extensionXLXS = "XLXS"
    }
}