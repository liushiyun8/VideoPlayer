package com.yundesign.videoplayer.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.emp.xdcommon.android.log.LogUtils;
import com.emp.xdcommon.common.utils.ToastUtil;
import com.yundesign.videoplayer.R;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.common.ConfigManager;
import com.yundesign.videoplayer.manager.WpsModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

public class WpsActivity extends BaseActivity {

    private int currentPos;
    private List<String> wpsList;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(Command command) {
        if (command.getCmd() == 13) {
            String content = command.getContent();
            if (content == null)
                return;
            switch (content) {
                case "next":             //下一个网站
                    nextWps();
                    break;
                case "prev":             //上一个网站
                    prevWps();
                    break;
                case "first":             //第一个网站
                    firstWps();
                    break;
                case "last":             //最后一个网站
                    lastWps();
                    break;
                default:
                    boolean hasFile=false;
                    for (int i = 0; i < wpsList.size(); i++) {
                        if (wpsList.get(i).contains(content)) {
                            currentPos=i;
                            openWps(wpsList.get(currentPos));
                            hasFile=true;
                            break;
                        }
                    }
                    if(!hasFile)
                        ToastUtil.shortTips("没有找到相应的文件");
                    break;
            }
        }
    }

    boolean openWps(String path) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(WpsModel.OPEN_MODE, WpsModel.OpenMode.READ_MODE); // 打开模式
        bundle.putBoolean(WpsModel.SEND_CLOSE_BROAD, true); // 关闭时是否发送广播
        bundle.putString(WpsModel.THIRD_PACKAGE, getPackageName()); // 第三方应用的包名，用于对改应用合法性的验证
        bundle.putBoolean(WpsModel.CLEAR_TRACE, true);// 清除打开记录
        // bundle.putBoolean(CLEAR_FILE, true); //关闭后删除打开文件
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setClassName(WpsModel.PackageName.NORMAL, WpsModel.ClassName.NORMAL);

        File file = new File(path);
        if (!file.exists()) {
            ToastUtil.shortTips("文件为空或者不存在");
            return false;
        }

        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        intent.putExtras(bundle);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            System.out.println("打开wps异常：" + e.toString());
            ToastUtil.shortTips("打开wps异常：" + e.toString());
            e.printStackTrace();
            LogUtils.e(TAG, "打开wps异常：" + e.toString());
            return false;
        }
        return true;
    }

    private void lastWps() {
        currentPos = wpsList.size() - 1;
        openWps(wpsList.get(currentPos));
    }

    private void firstWps() {
        currentPos = 0;
        openWps(wpsList.get(currentPos));
    }

    private void prevWps() {
        if (currentPos == 0) {
            ToastUtil.shortTips("已经是第一个了");
            return;
        }
        currentPos--;
        openWps(wpsList.get(currentPos));
    }

    private void nextWps() {
        if (currentPos == wpsList.size() - 1) {
            ToastUtil.shortTips("已经是最后一个了");
            return;
        }
        currentPos++;
        Log.e(TAG, wpsList.get(currentPos));
        openWps(wpsList.get(currentPos));
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected void initView() {
        wpsList = ConfigManager.getInstance().getWpsList();
        if (wpsList.size() > 0)
            openWps(wpsList.get(0));
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_wps;
    }
}
