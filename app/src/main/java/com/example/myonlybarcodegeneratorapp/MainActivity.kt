package com.example.myonlybarcodegeneratorapp

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager
import android.widget.*

import androidx.core.content.ContextCompat




class MainActivity : AppCompatActivity() {

    var text_barcode_number: TextView? = null
    var image_barcode : ImageView? = null
    var saveBtn: Button? = null
    var bitmap: Bitmap? = null
    var gbtn: Button? = null
    var barcodeText: EditText? = null



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

     //Initialization
     text_barcode_number = findViewById(R.id.text_barcode_number)
     image_barcode = findViewById(R.id.image_barcode)
     saveBtn = findViewById(R.id.button)
     gbtn = findViewById(R.id.btnG)
     barcodeText = findViewById(R.id.barcode_text)



        gbtn!!.setOnClickListener(View.OnClickListener {

      val myBarcodeData = barcodeText!!.text.toString()

            //getting permission as we are saving image to gallery
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, myBarcodeData);
            }
        })





//If user click on button save image to gallery
        saveBtn!!.setOnClickListener(View.OnClickListener {


            if (bitmap == null)
            {
                Toast.makeText(this, "no pixel", Toast.LENGTH_LONG).show()
            } else
            {
                saveMediaToStorage(bitmap!!)
            }


        })


    }


    //asking for permission
     @RequiresApi(Build.VERSION_CODES.M)
     open fun askForPermission(permission: String, requestCode: Int, barcodeData: String) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, permission))
            {
                Toast.makeText(
                    this@MainActivity,
                    "Please grant the requested permission to get your task done!",
                    Toast.LENGTH_LONG
                ).show()
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(permission),
                    requestCode
                )
            } else
            {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)

                //fifty characters
                // displayBitmap("1234 65290 9855 65252 1234 65290 9855 65252 1234 567 89012")

                //70 characters
                //displayBitmap("Apart from counting words and characters, our online editor can help..")

                //80 characters
                displayBitmap(barcodeData)
//                Toast.makeText(
//                    this@MainActivity,
//                    "Method excuted",
//                    Toast.LENGTH_LONG   ).show()
            }
        } else
        {
            displayBitmap(barcodeData)

        }
    }


    //Creating barcode and returning as bitmap
    private fun createBarcodeBitmap(
        barcodeValue: String,
        @ColorInt barcodeColor: Int,
        @ColorInt backgroundColor: Int,
        widthPixels: Int,
        heightPixels: Int
    ): Bitmap {
        val bitMatrix = Code128Writer().encode(
            barcodeValue,
            BarcodeFormat.CODE_128,
            widthPixels,
            heightPixels
        )

        val pixels = IntArray(bitMatrix.width * bitMatrix.height)
        for (y in 0 until bitMatrix.height) {
            val offset = y * bitMatrix.width
            for (x in 0 until bitMatrix.width) {
                pixels[offset + x] =
                    if (bitMatrix.get(x, y)) barcodeColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(
            bitMatrix.width,
            bitMatrix.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.setPixels(
            pixels,
            0,
            bitMatrix.width,
            0,
            0,
            bitMatrix.width,
            bitMatrix.height
        )
        return bitmap
    }

    //setting image and text values
    @RequiresApi(Build.VERSION_CODES.M)
    private fun displayBitmap(value: String) {

        val widthPixels = resources.getDimensionPixelSize(R.dimen.width_barcode)
        val heightPixels = resources.getDimensionPixelSize(R.dimen.height_barcode)

   bitmap =  createBarcodeBitmap(
       barcodeValue = value,
       barcodeColor = getColor(R.color.black),
       backgroundColor = getColor(android.R.color.white),
       widthPixels = widthPixels,
       heightPixels = heightPixels
   )
        image_barcode!!.setImageBitmap(bitmap)


//        image_barcode!!.setImageBitmap(
//            createBarcodeBitmap(
//                barcodeValue = value,
//                barcodeColor = getColor(R.color.black),
//                backgroundColor = getColor(android.R.color.white),
//                widthPixels = widthPixels,
//                heightPixels = heightPixels
//            )
//        )


        text_barcode_number!!.text = value
    }


    //saving image to gallery
    fun saveMediaToStorage(bitmap: Bitmap) {

        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            applicationContext.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
                //Toast.makeText(applicationContext, "Saved to Photos Q", Toast.LENGTH_LONG).show();
                showSnackbar("Saved to Photos")
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            galleryRefresh(image)
            // Toast.makeText(applicationContext, "Saved to Photos", Toast.LENGTH_LONG).show();
            showSnackbar("Saved to Photos")
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            //  context?.toast("Saved to Photos")
        }
    }

    //Inform user that image is saved to gallery
    private fun showSnackbar(message: String) {
        val parentLayout = findViewById<View>(android.R.id.content)
        Snackbar.make(parentLayout, "" + message, Snackbar.LENGTH_LONG)
//                .setAction("CLOSE") {  }
//                .setActionTextColor(resources.getColor(R.color.purple_700))
            .show()
    }

    //image by default is loaded a bit later so for that we are trying to referesh the gallery
    private fun galleryRefresh(file: File) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // setar
        // isso
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
    }
}




