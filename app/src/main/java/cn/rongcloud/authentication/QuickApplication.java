package cn.rongcloud.authentication;

import static com.kit.UIKit.getContext;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;


import com.kit.UIKit;

import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.oklib.wrapper.OkHelper;
import cn.rongcloud.oklib.wrapper.interfaces.IHeader;
import io.rong.imlib.RongCoreClient;
import okhttp3.Headers;

public class QuickApplication extends Application {
    private final static String TAG = "QuickApplication";
    private final static String APP_KEY = "pvxdm17jpw7ar";
    private final static String Business_Token = "这里是测试服务器token，需要申请https://rcrtc-api.rongcloud.net/code";

    @Override
    public void onCreate() {
        super.onCreate();
        initLiveRoom();
        initOKLibs();
    }

    private void initLiveRoom() {
        /**
         * 这里使用语聊房 SDK 初始化，所以不再需要 RCCoreClient 初始化融云相关 SDK。
         * appkey 即您申请的 appkey，需要开通音视频直播服务
         * token一般是您在登录自己的业务服务器之后，业务服务器返回给您的，可存在本地。
         */
        String process = getCurrentProcessName();
        if (!getPackageName().equals(process)) {
            // 非主进程不初始化 避免过度初始化
            return;
        }
        Log.d(TAG, "initVoiceRoom:process : " + process);
        RongCoreClient.init(this,APP_KEY);
    }

    public static String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    public static String getVerName() {
        String verName = "";
        try {
            verName = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    private static String authorization;

    public static void setAuthorization(String authorization) {
        QuickApplication.authorization = authorization;
    }

    void initOKLibs() {
        OkHelper.get().setHeadCacher(new IHeader() {
            @Override
            public Map<String, String> onAddHeader() {
                Map map = new HashMap<String, String>();
                if (!TextUtils.isEmpty(authorization)) {
                    map.put("Authorization", authorization);
                }
                map.put("BusinessToken", Business_Token);
                return map;
            }

            @Override
            public void onCacheHeader(Headers headers) {

            }
        });
    }
}
