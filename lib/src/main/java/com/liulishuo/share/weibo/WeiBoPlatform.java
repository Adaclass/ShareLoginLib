package com.liulishuo.share.weibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.liulishuo.share.LoginListener;
import com.liulishuo.share.ShareListener;
import com.liulishuo.share.ShareLoginLib;
import com.liulishuo.share.content.ShareContent;
import com.liulishuo.share.content.ShareContentType;
import com.liulishuo.share.utils.EventHandlerActivity;
import com.liulishuo.share.utils.IPlatform;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

/**
 * @author Kale
 * @date 2018/9/10
 */
public class WeiBoPlatform implements IPlatform {

    public static final String KEY_APP_KEY = "weibo_key_app_key";

    public static final String KEY_REDIRECT_URL = "key_redirect_url";

    public static final String KEY_SCOPE = "key_scope";

    // ---------------------------------------------------------------

    public static final String LOGIN = "weibo_login";

    public static final String TIME_LINE = "weibo_time_line";

    public static final String STORY = "weibo_story";

    private WbShareCallback shareCallback;

    private SsoHandler ssoHandler;

    @Override
    public String[] getSupportedTypes() {
        return new String[]{LOGIN, TIME_LINE};
    }

    @Override
    public boolean isAppInstalled(@NonNull Context context) {
        return WbSdk.isWbInstall(context);
    }

    @Override
    public void checkEnvironment(Context context, String type, int contentType) {
        // 1. 检测是否初始化
        if (TextUtils.isEmpty(ShareLoginLib.getValue(KEY_APP_KEY))) {
            throw new IllegalArgumentException("微博的appId未被初始化，当前为空");
        }

        // 2. 进行进行初始化操作
        try {
            WbSdk.checkInit();
        } catch (RuntimeException e) {
            // 如果没有init，则init一次，之后都不用再做任何初始化操作
            AuthInfo authInfo = new AuthInfo(context, ShareLoginLib.getValue(KEY_APP_KEY),
                    ShareLoginLib.getValue(KEY_REDIRECT_URL), ShareLoginLib.getValue(KEY_SCOPE));
            WbSdk.install(context, authInfo);
        }

        // 3. 检测分享的目标渠道是否合法
        if (!type.equals(LOGIN)) {
            // 是分享操作
            if (!type.equals(TIME_LINE) && !type.equals(STORY)) {
                throw new UnsupportedOperationException("不支持的分享渠道");
            }
        }

        // 4. 微博不支持分享音乐
        if (contentType == ShareContentType.MUSIC) {
            throw new UnsupportedOperationException("目前不能向微博分享音乐");
        }
    }

    @Override
    public void doLogin(@NonNull Activity activity, @NonNull LoginListener listener) {
        ssoHandler = new SsoHandler(activity);
        ssoHandler.authorize(new WbAuthListener() {
            @Override
            public void onSuccess(Oauth2AccessToken token) {
                LoginHelper.parseLoginResp(activity, token, listener);
            }

            @Override
            public void cancel() {
                listener.onCancel();
            }

            @Override
            public void onFailure(WbConnectErrorMessage err) {
                listener.onError(err.getErrorMessage());
            }
        });
    }

    @Override
    public void doShare(@NonNull Activity activity, String shareType, @NonNull ShareContent shareContent, @NonNull ShareListener listener) {
        shareCallback = new WbShareCallback() {
            @Override
            public void onWbShareSuccess() {
                listener.onSuccess();
            }

            @Override
            public void onWbShareCancel() {
                listener.onCancel();
            }

            @Override
            public void onWbShareFail() {
                listener.onError("未知异常");
            }
        };

        WbShareHandler shareHandler = new WbShareHandler(activity);
        shareHandler.registerApp();

        if (shareType.equals(TIME_LINE)) {
            shareHandler.shareMessage(ShareHelper.createShareObject(shareContent), false);
        } else if (shareType.equals(STORY)) {
            shareHandler.shareToStory(ShareHelper.createStoryMessage(shareContent));
        }
    }

    @Override
    public void onResponse(Activity activity, Intent data) {
        if (shareCallback != null) {
            // 分享
            new WbShareHandler(activity).doResultIntent(data, shareCallback);
        } else {
            // 登录
            int requestCode = data.getIntExtra(EventHandlerActivity.KEY_REQUEST_CODE, -1);
            int resultCode = data.getIntExtra(EventHandlerActivity.KEY_RESULT_CODE, -1);
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

}
