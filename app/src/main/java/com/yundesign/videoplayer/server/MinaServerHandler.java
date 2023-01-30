package com.yundesign.videoplayer.server;

import com.emp.xdcommon.android.log.LogUtils;
import com.google.gson.Gson;
import com.yundesign.videoplayer.bean.Command;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.greenrobot.eventbus.EventBus;

class MinaServerHandler extends IoHandlerAdapter {

    private  String TAG = getClass().getSimpleName();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        LogUtils.e(TAG,"sessionCreated");
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        LogUtils.e(TAG,"收到消息了:"+message);
        Command command = new Gson().fromJson(message.toString(), Command.class);
        LogUtils.e(TAG,"command:"+command);
        EventBus.getDefault().post(command);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        LogUtils.e(TAG,"sessionOpened");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        LogUtils.e(TAG,"sessionClosed");
    }
}
