package com.yundesign.videoplayer.bean;

public class Command {

    private int cmd;
    private int value;
    private String content;

    public Command() {
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Command{" +
                "cmd=" + cmd +
                ", value=" + value +
                ", content='" + content + '\'' +
                '}';
    }
}
