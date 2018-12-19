package com.zero.picture

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zero.picture.popup.PicturePopup
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() , View.OnClickListener{
    private val picturePopup = PicturePopup(this,this)
    override fun onClick(v: View) {
        when {
            v.id == R.id.btn_photo ->{picturePopup.dismiss()}
            v.id == R.id.btn_album ->{picturePopup.dismiss()}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_click.setOnClickListener { picturePopup.show() }
    }
}
