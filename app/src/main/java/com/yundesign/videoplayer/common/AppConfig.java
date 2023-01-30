package com.yundesign.videoplayer.common;

import com.emp.xdcommon.common.utils.PackageUtil;
import com.emp.xdcommon.common.utils.SdCardUtil;

import java.io.File;

public class AppConfig {

    public static final String WORKPATH= SdCardUtil.getNormalSDCardPath()+ File.separator+ "VideoPlayer";
    public static final String LOGPATH= WORKPATH+File.separator+"LOG";
    public static final String CRASHPATH= WORKPATH+File.separator+"CRASH";
    public static final String CONFIG= WORKPATH+File.separator+"config.xml";
    public static final String BACK_IMG= WORKPATH+File.separator+"background";
    public static final String SN_FILE= WORKPATH+File.separator+"sn.txt";
    public static final int DEFAULT_PORT = 8088;
    public static final int DEFAULT_UDP_PORT = 9090;
}
