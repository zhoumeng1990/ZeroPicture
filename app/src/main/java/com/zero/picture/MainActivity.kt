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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zero.libraryforphoto.FileProviderForPhoto
import com.zero.libraryforphoto.UriUtil
import com.zero.picture.popup.PicturePopupForKotlin
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

/**
 * Created by ZhouMeng on 2018/12/24.
 * 启动页
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var picturePopup: PicturePopupForKotlin
    private var pathName = Environment.getExternalStorageDirectory().toString() + "/" + IMAGE_FILE_NAME
    private var temp: File? = null
    private lateinit var ivPicture: ImageView

    companion object {
        private const val IMAGE_FILE_NAME = "icon.jpg"
        private const val REQUEST_IMAGE_ALBUM = 0x0000
        private const val REQUEST_IMAGE_PHOTO = 0x0001
        private const val REQUEST_SMALL_IMAGE_CUTTING = 0x0002
        private const val REQUEST_READ_MEDIA_AND_CAMERA = 0x0003
    }

    //使用anko布局
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            button {
                text = "clicked"
                onClick {
                    picturePopup.show()
                }
            }.lparams(wrapContent)

            imageView { id = R.id.ivPicture }.lparams(dip(180), dip(180))
        }

//        setContentView(R.layout.activity_main)
        picturePopup = PicturePopupForKotlin(this, this)
//        btn_click.setOnClickListener { picturePopup.show() }
        //运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    REQUEST_READ_MEDIA_AND_CAMERA
                )
            }
        }
        ivPicture = find(R.id.ivPicture)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_photo -> {
                picturePopup.dismiss()
                val file = File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)
                val fileUri = FileProviderForPhoto.getUriForFile(this@MainActivity, file)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                startActivityForResult(intent, REQUEST_IMAGE_PHOTO)
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
                    if (data?.data != null && data.data!!.toString().contains("com.miui.gallery.open")) {
                        pathName = UriUtil.getRealFilePath(this, data.data)
                        temp = File(pathName)
                        pictureZoom(UriUtil.getImageContentUri(this, temp))
                    } else {
                        if (data != null) {
                            pictureZoom(data.data)
                        }
                    }
                }
                REQUEST_SMALL_IMAGE_CUTTING -> {
                    if (data != null) {
                        PictureDispose.setPicToView(data, ivPicture, temp, pathName)
                    }
                }
            }
        }
    }

    private fun pictureZoom(uri: Uri?) {
        PictureDispose.startSmallPhotoZoom(this, uri, REQUEST_SMALL_IMAGE_CUTTING)
    }
}
