package com.zero.picture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zero.libraryforphoto.FileProviderForPhoto
import com.zero.picture.popup.PicturePopup
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var picturePopup : PicturePopup
    private val IMAGE_FILE_NAME = "icon.jpg"
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_SMALL_IMAGE_CUTTING = 2
    override fun onClick(v: View) {
        when {
            v.id == R.id.btn_photo ->{
                picturePopup.dismiss()
                val file = File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)
                val fileUri = FileProviderForPhoto.getUriForFile(this@MainActivity, file)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
                } else {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
            v.id == R.id.btn_album ->{picturePopup.dismiss()}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        picturePopup = PicturePopup(this,this)
        btn_click.setOnClickListener { picturePopup.show() }
    }
}
