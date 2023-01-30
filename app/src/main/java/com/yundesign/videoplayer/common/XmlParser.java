package com.yundesign.videoplayer.common;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class XmlParser {

    public void parseFile(String fileName, ParseCallback parseCallback) {
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }

        XmlPullParser pullParser = Xml.newPullParser();
        try {
            pullParser.setInput(new FileInputStream(file), "utf-8");
            String tag;
            int type = 0;
            try {
                type = pullParser.getEventType();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            while (XmlPullParser.END_DOCUMENT != type) {
                if (XmlPullParser.START_TAG == type) {
                    tag = pullParser.getName();
                    if (parseCallback != null)
                        parseCallback.callback(tag, pullParser);
                }

                try {
                    pullParser.nextTag();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    type = pullParser.getEventType();
                } catch (XmlPullParserException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public interface ParseCallback {
        void callback(String tag, XmlPullParser pullParser);
    }

}
