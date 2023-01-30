package com.yundesign.videoplayer.manager;

import org.apache.ftpserver.FtpServer;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class ServerManager {

    private FtpServer ftpServer;
    private NioSocketAcceptor nioSocketAcceptor;
    private NioDatagramAcceptor datagramAcceptor;

    private ServerManager(){}

    public void destroyFtp() {
        ftpServer=null;
    }

    private static class  INS{
        private static ServerManager serverManager=new ServerManager();
    }

    public static ServerManager getInstance(){
        return INS.serverManager;
    }

    public void setFtpManager(FtpServer ftpServer){
        this.ftpServer=ftpServer;
    }

    public void setTcpManager(NioSocketAcceptor nioSocketAcceptor){
        this.nioSocketAcceptor=nioSocketAcceptor;
    }

    public void setUdpManager(NioDatagramAcceptor datagramAcceptor){
        this.datagramAcceptor=datagramAcceptor;
    }

    public boolean isServerOK(){
        if(ftpServer!=null&&!ftpServer.isStopped()||nioSocketAcceptor!=null&&nioSocketAcceptor.isActive()||datagramAcceptor!=null&&datagramAcceptor.isActive()){
            return true;
        }
        return false;
    }

    public void destroyAcceptor(){
        this.nioSocketAcceptor=null;
        this.datagramAcceptor=null;
    }
}
