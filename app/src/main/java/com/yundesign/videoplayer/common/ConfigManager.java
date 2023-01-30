package com.yundesign.videoplayer.common;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private XmlParser xmlParser;
    private String IP;
    private int udpPort;
    private int tcpPort;
    private int startServer;
    private String initMode;
    private boolean autoPlay;
    private int loopMode;
    private int imgInterval;
    private boolean imgAutoPlay;
    private List<String> videoList = new ArrayList<>();
    private List<String> imgList = new ArrayList<>();
    private List<String> webList = new ArrayList<>();
    private List<String> wpsList = new ArrayList<>();

    private static class INS {
        private static ConfigManager configManager = new ConfigManager();
    }

    public static ConfigManager getInstance() {
        return INS.configManager;
    }

    private ConfigManager() {
        xmlParser = new XmlParser();
        loadConfig();
    }

    public void loadConfig() {
        xmlParser.parseFile(AppConfig.CONFIG, new XmlParser.ParseCallback() {
            private int modTag;

            @Override
            public void callback(String tag, XmlPullParser pullParser) {
                try {
                    if ("IP".equals(tag)) {
                        IP = pullParser.nextText();
                    } else if ("UDPPORT".equals(tag)) {
                        udpPort = Integer.parseInt(pullParser.nextText());
                    } else if ("TCPPORT".equals(tag)) {
                        tcpPort = Integer.parseInt(pullParser.nextText());
                    } else if ("startServer".equals(tag)) {
                        startServer = Integer.parseInt(pullParser.nextText());
                    } else if ("initMode".equals(tag)) {
                        initMode = pullParser.nextText();
                    } else if ("autoPlay".equals(tag)) {
                        autoPlay = Boolean.parseBoolean(pullParser.nextText());
                    } else if ("loopMode".equals(tag)) {
                        loopMode = Integer.parseInt(pullParser.nextText());
                    } else if ("imgInterval".equals(tag)) {
                        imgInterval = Integer.parseInt(pullParser.nextText());
                    } else if ("imgAutoPlay".equals(tag)) {
                        imgAutoPlay = Boolean.parseBoolean(pullParser.nextText());
                    } else if ("video".equals(tag)) {
                        modTag = 0;
                        videoList.clear();
                    } else if ("image".equals(tag)) {
                        modTag = 1;
                        imgList.clear();
                    }else if("web".equals(tag)){
                        modTag=2;
                        webList.clear();
                    }else if("wps".equals(tag)){
                        modTag=3;
                        wpsList.clear();
                    }else if ("url".equals(tag)) {
                        switch (modTag){
                            case 0:
                                videoList.add(AppConfig.WORKPATH+ File.separator+pullParser.nextText());
                                break;
                            case 1:
                                imgList.add(AppConfig.WORKPATH+ File.separator+pullParser.nextText());
                                break;
                            case 2:
                                webList.add(pullParser.nextText());
                                break;
                            case 3:
                                wpsList.add(AppConfig.WORKPATH+ File.separator+pullParser.nextText());
                                break;
                        }
                    }
                } catch (IOException|NumberFormatException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getStartServer() {
        return startServer;
    }

    public void setStartServer(int startServer) {
        this.startServer = startServer;
    }

    public String getInitMode() {
        return initMode;
    }

    public void setInitMode(String initMode) {
        this.initMode = initMode;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public int getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(int loopMode) {
        this.loopMode = loopMode;
    }

    public int getImgInterval() {
        return imgInterval;
    }

    public void setImgInterval(int imgInterval) {
        this.imgInterval = imgInterval;
    }

    public boolean isImgAutoPlay() {
        return imgAutoPlay;
    }

    public void setImgAutoPlay(boolean imgAutoPlay) {
        this.imgAutoPlay = imgAutoPlay;
    }

    public List<String> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<String> videoList) {
        this.videoList = videoList;
    }

    public List<String> getImgList() {
        return imgList;
    }

    public void setImgList(List<String> imgList) {
        this.imgList = imgList;
    }

    public List<String> getWebList() {
        return webList;
    }

    public void setWebList(List<String> webList) {
        this.webList = webList;
    }

    public List<String> getWpsList() {
        return wpsList;
    }

    public void setWpsList(List<String> wpsList) {
        this.wpsList = wpsList;
    }
}
