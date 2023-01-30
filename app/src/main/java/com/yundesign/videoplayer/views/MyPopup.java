package com.yundesign.videoplayer.views;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.yundesign.videoplayer.R;

public class MyPopup {

    private final WindowManager.LayoutParams wmParams;
    private final WindowManager mWindowManager;
    private final LayoutInflater inflater;
    private final LinearLayout mFloatLayout;
    private boolean isShowing;

    public MyPopup(Context context) {
        Context mApplication = context.getApplicationContext();
        wmParams = new WindowManager.LayoutParams();
//获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) mApplication.getSystemService(Context.WINDOW_SERVICE);
//设置window type

// 设置窗体显示类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type =WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
//        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;
//设置悬浮窗口长宽数据
//        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.width = 1;
        wmParams.height = 1;
        inflater = LayoutInflater.from(mApplication);
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.mypop, null);
    }

    public void showPop() {
        if (!isShowing) {
            mWindowManager.addView(mFloatLayout, wmParams);
            isShowing = true;
        }
    }

    public void release() {
        if (isShowing) {
            isShowing = false;
            mWindowManager.removeView(mFloatLayout);
        }
    }
}

