package com.yundesign.videoplayer.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.emp.xdcommon.android.log.LogUtils;
import com.yundesign.videoplayer.MainActivity;
import com.yundesign.videoplayer.R;
import com.yundesign.videoplayer.common.AppConfig;
import com.yundesign.videoplayer.common.ConfigManager;
import com.yundesign.videoplayer.manager.ServerManager;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MinaServer extends Service {

    private String TAG = getClass().getSimpleName();
    private NioSocketAcceptor acceptor;
    private String IP;
    private int port = AppConfig.DEFAULT_PORT;
    private int udpPort = AppConfig.DEFAULT_UDP_PORT;
    private NioDatagramAcceptor udpAcceptor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e(TAG, "onCreate");
        IP = ConfigManager.getInstance().getIP();
        if (ConfigManager.getInstance().getTcpPort() != 0)
            port = ConfigManager.getInstance().getTcpPort();
        if (ConfigManager.getInstance().getUdpPort() != 0)
            udpPort = ConfigManager.getInstance().getUdpPort();
        initSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder
                (this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.mipmap.ic_launcher))    // 设置下拉列表中的图标(大图标)
                .setContentTitle("提示") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("远程控制中") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        // 参数一：唯一的通知标识；参数二：通知消息。
        startForeground(110, notification);// 开始前台服务
        return super.onStartCommand(intent, flags, startId);
    }

    private void initSocket() {
        try {
            // 创建一个非堵塞的server端的Socket
            acceptor = new NioSocketAcceptor();
            // 设置过滤器
            acceptor.getFilterChain().addLast("textCodec", new ProtocolCodecFilter(new TextLineCodecFactory()));
            acceptor.getFilterChain().addLast("log", new LoggingFilter());
//            acceptor.getFilterChain().addLast(
//                    "serverCodec",
//                    new ProtocolCodecFilter(
//                            new ObjectSerializationCodecFactory()));
            acceptor.getFilterChain().addLast("ServerFilter",
                    new ExecutorFilter());
            // 设置读取数据的缓冲区大小
            acceptor.getSessionConfig().setReadBufferSize(10 * 1024);
            // 读写通道10秒内无操作进入空暇状态
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
            // 加入逻辑处理器
            acceptor.setHandler(new MinaServerHandler());
            // 绑定端口
            InetSocketAddress inetAddress;
            if (TextUtils.isEmpty(IP))
                inetAddress = new InetSocketAddress(port);
            else inetAddress = new InetSocketAddress(IP, port);
            acceptor.setReuseAddress(true);
            acceptor.bind(inetAddress);
            ServerManager.getInstance().setTcpManager(acceptor);
            LogUtils.e(TAG, "tcp服务端启动成功...     端口号为：" + port);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "tcp服务端启动异常....");
        }

        udpAcceptor = new NioDatagramAcceptor();
        udpAcceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory()));
        udpAcceptor.setHandler(new MinaServerHandler());
        try {
            udpAcceptor.bind(new InetSocketAddress(TextUtils.isEmpty(IP) ? "0.0.0.0" : IP, udpPort));
            ServerManager.getInstance().setUdpManager(udpAcceptor);
            LogUtils.e(TAG, "udp服务端启动成功...     端口号为：" + udpPort);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "udp服务端启动异常....");
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.e(TAG, "onDestroy");
        if (acceptor != null && !acceptor.isDisposed()) {
            acceptor.dispose();
            acceptor = null;
        }
        if (udpAcceptor != null && !udpAcceptor.isDisposed()) {
            udpAcceptor.dispose();
            udpAcceptor = null;
        }
        ServerManager.getInstance().destroyAcceptor();
        LogUtils.e(TAG, "服务端关闭成功......");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }
}
