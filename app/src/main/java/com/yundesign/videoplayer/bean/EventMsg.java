package com.yundesign.videoplayer.bean;

public class EventMsg {

    public static final int FINISH = 100;
    private int what;

    public EventMsg(int what) {
        this.what = what;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }
}
