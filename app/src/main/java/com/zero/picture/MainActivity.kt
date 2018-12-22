package com.zero.picture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zero.libraryforphoto.FileProviderForPhoto
import com.zero.libraryforphoto.UriUtil
import com.zero.picture.popup.PicturePopup
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var picturePopup: PicturePopup
    private var pathName = Environment.getExternalStorageDirectory().toString() + "/" + IMAGE_FILE_NAME
    private var temp: File? = null

    companion object {
        private const val IMAGE_FILE_NAME = "icon.jpg"
        private const val PHOTO_PERMISSION = 0x000101
        private const val ALBUM_PERMISSION = 0x000102
        private const val REQUEST_IMAGE_ALBUM = 0
        private const val REQUEST_IMAGE_PHOTO = 1
        private const val REQUEST_SMALL_IMAGE_CUTTING = 2
        private const val MY_PERMISSIONS_REQUEST_READ_MEDIA = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        picturePopup = PicturePopup(this, this)
        btn_click.setOnClickListener { picturePopup.show() }
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_MEDIA
            )
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_photo -> {
                picturePopup.dismiss()
                val file = File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)
                val fileUri = FileProviderForPhoto.getUriForFile(this@MainActivity, file)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PHOTO_PERMISSION)
                    } else {
                        startActivityForResult(intent, REQUEST_IMAGE_PHOTO)
                    }
                }
            }
            R.id.btn_album -> {
                picturePopup.dismiss()
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                // 判断系统中是否有处理该 Intent 的 Activity
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_ALBUM)
                } else {
                    Toast.makeText(this@MainActivity, "未找到图片查看器", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PHOTO_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(intent, REQUEST_IMAGE_PHOTO)
            }
        } else if (requestCode == ALBUM_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(intent, REQUEST_IMAGE_ALBUM)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PHOTO -> {
                    temp = File(pathName)
                    val uri = FileProviderForPhoto.getUriForFile(this@MainActivity, temp)
                    pictureZoom(uri)
                }
                REQUEST_IMAGE_ALBUM -> {
                    if (data != null && data.data != null && data.data!!.toString().contains("com.miui.gallery.open")) {
                        pathName = UriUtil.getRealFilePath(this, data.data)
                        temp = File(pathName)
                        val permissionCheck =
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_READ_MEDIA
                            )
                        } else {
                            pictureZoom(UriUtil.getImageContentUri(this, temp))
                        }
                    } else {
                        if (data != null) {
                            pictureZoom(data.data)
                        }
                    }
                }
                REQUEST_SMALL_IMAGE_CUTTING -> {
                    if (data != null) {
                        PictureDispose.setPicToView(data,iv_picture,temp,pathName)
                    }
                }
            }
        }
    }

    private fun pictureZoom(uri: Uri?){
        PictureDispose.startSmallPhotoZoom(this,uri,REQUEST_SMALL_IMAGE_CUTTING)
    }
}
