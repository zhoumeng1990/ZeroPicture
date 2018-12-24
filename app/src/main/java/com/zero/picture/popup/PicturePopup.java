package com.zero.picture.popup;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.Button;
import android.widget.PopupWindow;
import com.zero.picture.R;
/**
 * Created by ZhouMeng on 2018/12/24.
 * 弹窗
 */
public class PicturePopup extends PopupWindow {

    private View view;
    private Context context;
    private View.OnClickListener clickListener;

    public PicturePopup(Context context, View.OnClickListener clickListener) {
        super(context);
        this.context = context;
        this.clickListener = clickListener;
        init();
    }

    /**
     * 设置布局以及点击事件
     */
    private void init() {
        view = LayoutInflater.from(context).inflate(R.layout.pop_picture, null);
        Button btn_camera = view.findViewById(R.id.btn_photo);
        Button btn_select = view.findViewById(R.id.btn_album);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);

        btn_select.setOnClickListener(clickListener);
        btn_camera.setOnClickListener(clickListener);
        btn_cancel.setOnClickListener(v -> dismiss());

        // 导入布局
        this.setContentView(view);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        view.setOnClickListener(v -> dismiss());
    }

    public void show(){
        showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }
}
