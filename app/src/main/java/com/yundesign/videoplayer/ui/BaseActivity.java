package com.yundesign.videoplayer.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yundesign.videoplayer.MainActivity;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.bean.EventMsg;
import com.yundesign.videoplayer.ftp.MyFtpServer;
import com.yundesign.videoplayer.manager.ServerManager;
import com.yundesign.videoplayer.server.MinaServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG = getClass().getSimpleName();


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MainEvent(Command command) {
        if (command.getCmd() == 8) {
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayout());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        if (!ServerManager.getInstance().isServerOK()) {
//            EventBus.getDefault().post(new EventMsg(EventMsg.FINISH));
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
            restartServer();
        }
    }

    protected void restartServer(){
        startService(new Intent(this, MyFtpServer.class));
        startService(new Intent(this, MinaServer.class));
    }

    protected boolean useEventBus() {
        return false;
    }

    protected abstract void initView();

    protected abstract int getContentLayout();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (useEventBus())
            EventBus.getDefault().unregister(this);
    }
}
