package com.yundesign.videoplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.emp.xdcommon.android.log.LogUtils;
import com.emp.xdcommon.common.io.FileUtils;
import com.yundesign.keyserver.IKey;
import com.yundesign.videoplayer.bean.Command;
import com.yundesign.videoplayer.common.AppConfig;
import com.yundesign.videoplayer.common.ConfigManager;
import com.yundesign.videoplayer.ftp.MyFtpServer;
import com.yundesign.videoplayer.server.MinaServer;
import com.yundesign.videoplayer.ui.BaseActivity;
import com.yundesign.videoplayer.ui.MyVideoActivity;
import com.yundesign.videoplayer.ui.PhotoActivity;
import com.yundesign.videoplayer.ui.WebActivity;
import com.yundesign.videoplayer.ui.WpsActivity;
import com.yundesign.videoplayer.utils.DeviceUtil;
import com.yundesign.videoplayer.utils.IpUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends BaseActivity {

    private PermissionsChecker mPermissionsChecker;
    private static final int REQUEST_CODE = 200; // 请求码
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private Intent minaService;
    private Intent ftpService;
    private ConfigManager configManager;
    private TextView tvIp;
    private NetReciever receiver;
    private AlertDialog dialog;
    private TextView tvSn;
    private String deviceId;
    private boolean isRegister;
    private ServiceConnection keyConn;
    private IKey iKey;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void MainEvent(Command command) {
        if (command.getCmd() == 8) {
            if (command.getValue() == 0) {
                startVideoMode();
            } else if (command.getValue() == 1) {
                startPhotoMode();
            } else if (command.getValue() == 2) {
                startWebMode();
            } else {
                startWpsMode();
            }
        } else if (command.getCmd() == 14) {
            execShell(command.getContent());
        }
    }

    private void startWebMode() {
        startActivity(new Intent(this, WebActivity.class));
    }

    private void startWpsMode() {
        startActivity(new Intent(this, WpsActivity.class));
    }

    private void startPhotoMode() {
        startActivity(new Intent(this, PhotoActivity.class));
    }

    private void startVideoMode() {
        startActivity(new Intent(this, MyVideoActivity.class));
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Override
    protected void initView() {
        dialog = new AlertDialog.Builder(this).setTitle("系统提示:").setMessage("此设备未注册,请及时联系开发者注册！")
                .setCancelable(false)
                .create();
        tvIp = findViewById(R.id.ip);
        tvSn = findViewById(R.id.sn);
        setIp();
        registerNetReceiver();
        deviceId = DeviceUtil.getDeviceId(this);
        tvSn.setText(deviceId);

        keyConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iKey = IKey.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iKey=null;
            }
        };
        Intent keyIntent = new Intent("com.emp.keyService");
        keyIntent.setPackage("com.yundesign.keyserver");
        bindService(keyIntent, keyConn,Context.BIND_AUTO_CREATE);
        mPermissionsChecker = new PermissionsChecker(this);
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        } else {
            start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void registerNetReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetReciever();
        registerReceiver(receiver, intentFilter);
    }

    private void setIp() {
        String ip = IpUtil.getlocalIp();
        tvIp.setText(ip);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    private void start() {
        File snFile = new File(AppConfig.SN_FILE);
        if (snFile.exists()) {
            try {
                if (deviceId != null) {
                    String encodeSn = DeviceUtil.getEncodeString(deviceId);
                    LogUtils.e(TAG, "encodeSn:" + encodeSn);
                    Log.e(TAG, "encodeSn:" + encodeSn + ",snfile:" + FileUtils.readFileToString(snFile) + ",ok:" + encodeSn.equals(FileUtils.readFileToString(snFile)));
                    if (!TextUtils.isEmpty(encodeSn) && encodeSn.trim().equals(FileUtils.readFileToString(snFile).trim())) {
                        isRegister = true;
                    }
                    Log.e(TAG, "isRegister:" + isRegister);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configManager = ConfigManager.getInstance();
        ftpService = new Intent(this, MyFtpServer.class);
        startService(ftpService);
        if (!isRegister) {
            dialog.show();
            return;
        }
        minaService = new Intent(this, MinaServer.class);
        startService(minaService);
        if ("VID".equals(configManager.getInitMode())) {
            startVideoMode();
        } else if ("IMG".equals(configManager.getInitMode()))
            startPhotoMode();
        else if ("WEB".equals(configManager.getInitMode())) {
            startWebMode();
        } else startWpsMode();
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE) {
            if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
                finish();
            } else if (resultCode == PermissionsActivity.PERMISSIONS_GRANTED) {
                start();
            }
        }
    }

    private void execShell(String content) {
        if (iKey != null) {
            try {
                iKey.sendKey(content);
            } catch (RemoteException e) {
                e.printStackTrace();
                LogUtils.e(TAG,"KeyServer sendKey Error");
            }
        }
//        String[] keys = content.split(",");
//        int[] keycodes = new int[keys.length];
//        for (int i = 0; i < keys.length; i++) {
//            try {
//                keycodes[i] = Integer.parseInt(keys[i]);
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.e(TAG, "commands:" + Arrays.toString(keycodes));
//        sendKeyCode(keycodes);
    }

    @Override
    protected void onDestroy() {
        if (minaService != null)
            stopService(minaService);
        if (ftpService != null)
            stopService(ftpService);
        if (receiver != null)
            unregisterReceiver(receiver);
        if(keyConn!=null){
            unbindService(keyConn);
        }
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        super.onDestroy();
    }

    public class NetReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
                setIp();
        }
    }

    /**
     * 传入需要的键值即可
     *
     * @param keyCodes 模拟按键的keyCode
     */
    private void sendKeyCode(int[] keyCodes) {
        new Thread() {
            public void run() {
                try {
                    //调用内部方法模拟按键按下的过程
                    Instrumentation inst = new Instrumentation();
                    if (keyCodes.length >= 2) {
                        long downTime = SystemClock.uptimeMillis();
                        inst.sendKeySync(new KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCodes[1], 0, keyCodes[0]));
                        Thread.sleep(100);
                        inst.sendKeySync(new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCodes[1], 0, keyCodes[0]));
                    } else if (keyCodes.length == 1) {
                        inst.sendKeyDownUpSync(keyCodes[0]);
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                }
            }
        }.start();
    }

}
