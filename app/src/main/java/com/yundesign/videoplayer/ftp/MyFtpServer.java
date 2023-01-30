package com.yundesign.videoplayer.ftp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.emp.xdcommon.android.log.LogUtils;
import com.yundesign.videoplayer.common.AppConfig;
import com.yundesign.videoplayer.common.ConfigManager;
import com.yundesign.videoplayer.manager.ServerManager;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFtpServer extends Service {

    private final String TAG = "ZffFtpServer";

    private FtpServer server;
    private String user = "admin";
    private String password = "123654";
    private int port = 2121;

    private FtpServerCallback mFtpServerCallback;


    public class MyBinder extends Binder {
        public MyFtpServer getService() {
            return MyFtpServer.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        try {
            init();
            Toast.makeText(this, "启动ftp服务成功", Toast.LENGTH_SHORT).show();
        } catch (FtpException e) {
            e.printStackTrace();
            Toast.makeText(this, "启动ftp服务失败", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "启动ftp服务失败:" + e.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        Toast.makeText(this, "关闭ftp服务", Toast.LENGTH_SHORT).show();
        LogUtils.e(TAG, "onDestroy,关闭ftp服务");
    }

    /**
     * 初始化
     *
     * @throws FtpException
     */
    public void init() throws FtpException {
        release();
        startFtp();
    }

    private void startFtp() throws FtpException {
        creatDirsFiles();
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port); //设置端口号 非ROOT不可使用1024以下的端口
        serverFactory.addListener("default", factory.createListener());

        //创建ftpHome目录
        File ftpHomeDir = new File(AppConfig.WORKPATH);
        if (!ftpHomeDir.exists()) {
            ftpHomeDir.mkdirs();
        }
        //设置访问用户名和密码还有共享路径
        BaseUser baseUser = new BaseUser();
        baseUser.setName(user);
        baseUser.setPassword(password);
        baseUser.setHomeDirectory(AppConfig.WORKPATH);

        List<Authority> authorities = new ArrayList<Authority>();
        Authority author = new WritePermission();
        authorities.add(author);
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);

        Map<String, Ftplet> ftplets = new HashMap<String, Ftplet>();
        ftplets.put("default", new MyFtplet());
        serverFactory.setFtplets(ftplets);

        server = serverFactory.createServer();
        server.start();
        ServerManager.getInstance().setFtpManager(server);
    }

    /**
     * 创建服务器配置文件
     */
    public void creatDirsFiles() {
        try {
            File dir = new File(AppConfig.WORKPATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 释放资源
     */
    public void release() {
        stopFtp();
    }

    private void stopFtp() {
        if (server != null) {
            server.stop();
            ServerManager.getInstance().destroyFtp();
            server = null;
        }
    }


    private class MyFtplet extends DefaultFtplet {

        @Override
        public FtpletResult onDownloadEnd(FtpSession session, FtpRequest request)
                throws FtpException, IOException {
            Log.i(TAG, "onDownloadEnd-->");
            return super.onDownloadEnd(session, request);
        }

        @Override
        public FtpletResult onDownloadStart(FtpSession session,
                                            FtpRequest request) throws FtpException, IOException {
            Log.i(TAG, "onDownloadStart-->");
            return super.onDownloadStart(session, request);
        }

        @Override
        public FtpletResult onSite(FtpSession session, FtpRequest request)
                throws FtpException, IOException {
            Log.i(TAG, "onSite-->");
            return super.onSite(session, request);
        }

        @Override
        public FtpletResult onUploadEnd(FtpSession session, FtpRequest request)
                throws FtpException, IOException {
            Log.i(TAG, "onUploadEnd-->session=" + session.getDataType().name() + ", request=" + request.getArgument());
            Log.i(TAG, "onUploadEnd-->getRenameFrom=" + session.getRenameFrom());
            if (mFtpServerCallback != null) {
                mFtpServerCallback.onUploadEnd();
            }
            ConfigManager.getInstance().loadConfig();
            return super.onUploadEnd(session, request);
        }

        @Override
        public FtpletResult onUploadStart(FtpSession session, FtpRequest request)
                throws FtpException, IOException {
            Log.i(TAG, "onUploadStart-->session=" + session.getDataType().name() + ", request=" + request.getArgument());
            if (mFtpServerCallback != null) {
                mFtpServerCallback.onUploadStart();
            }
            return super.onUploadStart(session, request);
        }

    }

    public void setFtpCallback(FtpServerCallback callback) {
        mFtpServerCallback = callback;
    }

    public interface FtpServerCallback {
        void onUploadStart();

        void onUploadEnd();
    }

}
