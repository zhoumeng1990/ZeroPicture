package com.zero.picture

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

class PictureDispose {
    companion object {
        /**
         * 小图模式切割图片
         * 此方式直接返回截图后的 bitmap，由于内存的限制，返回的图片会比较小
         */
        fun startSmallPhotoZoom(activity: Activity, uri: Uri?, code: Int) {
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
            activity.startActivityForResult(intent, code)
        }

        /**
         * 小图模式中，保存图片后，设置到视图中
         */
        fun setPicToView(data: Intent, iv_picture: ImageView, temp: File?, pathName: String) {
            val extras = data.extras
            if (extras != null) {
                val photo = extras.getParcelable<Bitmap>("data") // 直接获得内存中保存的 bitmap
                // 创建 smallIcon 文件夹
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val storage = Environment.getExternalStorageDirectory().path
                    val dirFile = File("$storage/smallIcon")
                    val file = File(dirFile, System.currentTimeMillis().toString() + ".jpg")
                    photoCompress(photo!!, file)
                }
                // 在视图中显示图片
                iv_picture.setImageBitmap(photo)
            } else {
                if (temp != null) {
                    val bitmap = BitmapFactory.decodeFile(pathName)
                    photoCompress(bitmap, temp)
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
                //接下来可做上传用户头像处理
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
