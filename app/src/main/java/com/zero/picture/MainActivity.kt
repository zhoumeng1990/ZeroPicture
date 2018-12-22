package com.zero.picture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import java.io.FileOutputStream

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        picturePopup = PicturePopup(this, this)
        btn_click.setOnClickListener { picturePopup.show() }
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
                    startSmallPhotoZoom(uri)
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
                            startSmallPhotoZoom(UriUtil.getImageContentUri(this, temp))
                        }
                    } else {
                        if (data != null) {
                            startSmallPhotoZoom(data.data)
                        }
                    }
                }
                REQUEST_SMALL_IMAGE_CUTTING -> {
                    if (data != null) {
                        setPicToView(data)
                    }
                }
            }
        }
    }

    /**
     * 小图模式切割图片
     * 此方式直接返回截图后的 bitmap，由于内存的限制，返回的图片会比较小
     */
    private fun startSmallPhotoZoom(uri: Uri?) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1) // 裁剪框比例
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 300) // 输出图片大小
        intent.putExtra("outputY", 300)
        intent.putExtra("scale", true)
        intent.putExtra("return-data", true)
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING)
    }

    /**
     * 小图模式中，保存图片后，设置到视图中
     */
    private fun setPicToView(data: Intent) {
        val extras = data.extras
        if (extras != null) {
            val photo = extras.getParcelable<Bitmap>("data") // 直接获得内存中保存的 bitmap
            // 创建 smallIcon 文件夹
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val storage = Environment.getExternalStorageDirectory().path
                val dirFile = File("$storage/smallIcon")
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.e("TAG", "文件夹创建失败")
                    } else {
                        Log.e("TAG", "文件夹创建成功")
                    }
                }
                val file = File(dirFile, System.currentTimeMillis().toString() + ".jpg")
                photoCompress(photo!!, file)
            }
            // 在视图中显示图片
            iv_picture.setImageBitmap(photo)
        } else {
            if (temp != null) {
                val bitmap = BitmapFactory.decodeFile(pathName)
                photoCompress(bitmap, temp!!)
                iv_picture.setImageBitmap(bitmap)
            }
        }
    }

    // 保存图片
    private fun photoCompress(photo: Bitmap, file: File) {
        try {
            val outputStream = FileOutputStream(file)
            photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            //上传用户头像
//            presenter.updateUserPic(file)
            temp =null
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}
