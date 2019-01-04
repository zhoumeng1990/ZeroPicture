package com.zero.picture.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import com.zero.picture.R
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * Created by ZhouMeng on 2018/12/24.
 * aoko弹窗
 */
class PicturePopupForKotlin(private val context: Context, private val clickListener: View.OnClickListener) :
    PopupWindow(context) {

    private lateinit var view: View

    init {
        init()
    }

    /**
     * 设置布局以及点击事件
     */
    private fun init() {
        view = with(context){
            verticalLayout {
                lparams(width = matchParent,height = wrapContent){
                    setMargins(dip(10),0,dip(10),0)
                }
                backgroundColor = Color.parseColor("#66000000")
                button{
                    text = "拍照"
                    id = R.id.btnPhoto
                    background = context.getDrawable(R.drawable.shape_btn_top)
                }.lparams(width = matchParent,height = wrapContent)

                view { backgroundColor = Color.parseColor("#66000000") }.lparams(width= matchParent,height = dip(1))

                button{
                    text = "相册"
                    id = R.id.btnAlbum
                    background = context.getDrawable(R.drawable.shape_btn_top)
                }.lparams(width = matchParent,height = wrapContent)

                button{
                    text = "取消"
                    background = context.getDrawable(R.drawable.shape_btn_top)
                    onClick { dismiss() }
                }.lparams(width = matchParent,height = wrapContent)
            }
        }

        view.find<Button>(R.id.btnPhoto).setOnClickListener(clickListener)
        view.find<Button>(R.id.btnAlbum).setOnClickListener(clickListener)

//        view = LayoutInflater.from(context).inflate(R.layout.pop_picture, null)
//        val btn_camera = view!!.findViewById<Button>(R.id.btn_photo)
//        val btn_select = view!!.findViewById<Button>(R.id.btn_album)
//        val btn_cancel = view!!.findViewById<Button>(R.id.btn_cancel)
//
//        btn_select.setOnClickListener(clickListener)
//        btn_camera.setOnClickListener(clickListener)
//        btn_cancel.setOnClickListener { v -> dismiss() }
//
        // 导入布局
        this.contentView = view
//        this.width = WindowManager.LayoutParams.MATCH_PARENT
//        this.height = WindowManager.LayoutParams.WRAP_CONTENT
        // 设置可触
        this.isFocusable = true
        val dw = ColorDrawable(0x0000000)
        this.setBackgroundDrawable(dw)
        // 单击弹出窗以外处 关闭弹出窗
        view.setOnClickListener { dismiss() }
    }

    fun show() {
        showAtLocation(view, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
    }
}
