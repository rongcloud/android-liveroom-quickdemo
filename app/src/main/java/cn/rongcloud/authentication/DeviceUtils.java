package cn.rongcloud.authentication;


import android.os.Build;

import java.util.UUID;


/**
 * @author lihao
 * @project LiveRoomQuickDemo
 * @date 2022/1/6
 * @time 3:19 下午
 */
public class DeviceUtils {

    public static String getDeviceId(){
        String deviceIdShort =
                "35" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10;
        String serial;
        try {
            serial= new UUID((long) deviceIdShort.hashCode(),Class.forName("SERIAL").hashCode()).toString();

        }catch (Exception exception){
            serial="serial";
        }
        String deviceId = new UUID(deviceIdShort.hashCode(), serial.hashCode()).toString();
        return deviceId;
    }
}
