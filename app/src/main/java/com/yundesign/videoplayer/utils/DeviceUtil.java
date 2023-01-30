package com.yundesign.videoplayer.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.emp.xdcommon.common.manager.PreferencesManager;
import com.emp.xdcommon.common.utils.AndroidUtil;
import com.emp.xdcommon.common.utils.HexUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class DeviceUtil {
    /*
     * deviceID的组成为：渠道标志+识别符来源标志+hash后的终端识别符
     *
     * 渠道标志为：
     * 1，andriod（a）
     *
     * 识别符来源标志：
     * 1， wifi mac地址（wifi）；
     * 2， IMEI（imei）；
     * 3， 序列号（sn）；
     * 4， id：随机码。若前面的都取不到时，则随机生成一个随机码，需要缓存。
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
//        deviceId.append("a");
        try {
            //wifi mac地址
//            WifiManager wifi = (WifiManager) MyApp.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            WifiInfo info = wifi.getConnectionInfo();
//            String wifiMac = info.getMacAddress();
//            if (!TextUtils.isEmpty(wifiMac)) {
//                deviceId.append("wifi");
//                deviceId.append(wifiMac);
//                return deviceId.toString();
//            }
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return "03"+getUUID(context);
            }
            //IMEI（imei）
            String imei = tm.getDeviceId();
            if (!TextUtils.isEmpty(imei) && !"unknown".equals(imei)) {
                deviceId.append("00");
                deviceId.append(imei);
                return deviceId.toString();
            }
            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if (!TextUtils.isEmpty(sn) && !"unknown".equals(sn)) {
                deviceId.append("01");
                deviceId.append(sn);
                return deviceId.toString();
            }

            String androidId = AndroidUtil.getAndroidId(context);
            if (!TextUtils.isEmpty(androidId) && !"unknown".equals(androidId)) {
                deviceId.append("03");
                deviceId.append(androidId);
                return deviceId.toString();
            }
            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID(context);
            if (!TextUtils.isEmpty(uuid)) {
                deviceId.append("02");
                deviceId.append(uuid);
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("02").append(getUUID(context));
        }
        return deviceId.toString();
    }

    /**
     * 得到全局唯一UUID
     */
    public static String getUUID(Context context) {
        PreferencesManager pre = PreferencesManager.getIns(context);
        if (TextUtils.isEmpty(pre.getDid())) {
            String uuid = UUID.randomUUID().toString();
            pre.setDid(uuid);
        }
        return pre.getDid();
    }

    /**
     * 得到全局唯一UUID
     */
    @SuppressLint("NewApi")
    public static String getEncodeString(String sn) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("SHA-1");
            md5.update(sn.getBytes());
            byte[] m = md5.digest();//加密
            return HexUtil.encodeHexStr(m);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
