package cn.rongcloud.authentication;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.kit.UIKit;
import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.utils.Logger;

import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.authentication.bean.Account;
import cn.rongcloud.oklib.LoadTag;
import cn.rongcloud.oklib.OkApi;
import cn.rongcloud.oklib.WrapperCallBack;
import cn.rongcloud.oklib.wrapper.Wrapper;
import cn.rongcloud.quickdemo.R;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;

/**
 * 登录
 */
public class LoginActivity extends AbsPermissionActivity {

    @Override
    protected String[] onCheckPermission() {
        return VOICE_PERMISSIONS;
    }

    @Override
    protected void onPermissionAccept(boolean accept) {
        if (accept) {
            initView();
        }
    }

    private EditText et_phone;

    void initView() {
        setContentView(R.layout.activity_login);
        et_phone = findViewById(R.id.et_phone);
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = et_phone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    KToast.show("请输入手机号");
                    return;
                }
                login(phone, "111111");
            }
        });
    }

    /**
     * 使用融云测试服务器 获取连接融云IM 服务器的token
     *
     * @param phone 电话token 获取businessToken 的手机号
     * @param code  验证码
     */
    void login(String phone, String code) {
        LoadTag tag = new LoadTag(activity, "login...");
        tag.show();
        Map<String, Object> params = new HashMap<>(4);
        params.put("mobile", phone);
        params.put("verifyCode", code);
        params.put("deviceId", DeviceUtils.getDeviceId());
        OkApi.post(Api.LOGIN, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (null != tag) tag.dismiss();
                Logger.e("result = " + GsonUtil.obj2Json(result));
                if (result.getCode() == 10000) {
                    Account account = result.get(Account.class);
                    if (null != account) {
                        QuickApplication.setAuthorization(account.getAuthorization());
                        AccoutManager.setAcctount(account,true);
                        connect(account);
                    }
                }
            }
        });
    }

    /**
     * 连接 融云IM 服务
     *
     * @param account 账号信息
     */
    private void connect(Account account) {
        //先断开连接
        RongCoreClient.getInstance().disconnect(false);
        //连接
        RongCoreClient.connect(account.getImToken(), new IRongCoreCallback.ConnectCallback() {
                    @Override
                    public void onSuccess(String t) {
                        UIKit.startActivity(LoginActivity.this, RoomListActivity.class);
                    }

                    @Override
                    public void onError(IRongCoreEnum.ConnectionErrorCode e) {
                        String info = "connect fail：\n【" + e.getValue() + "】" + e.name();
                        Log.e("ConnectActivity", info);
                        com.kit.utils.KToast.show(info);
                        setTitle(account.getUserName() + " 连接失败");
                    }

                    @Override
                    public void onDatabaseOpened(IRongCoreEnum.DatabaseOpenStatus code) {

                    }
                }
        );
    }

}
