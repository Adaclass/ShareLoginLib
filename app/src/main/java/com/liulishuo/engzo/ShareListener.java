package com.liulishuo.engzo;


import android.widget.Toast;

import com.liulishuo.share.SsoShareManager;

/**
 * @author Jack Tony
 * @date 2015/10/23
 */
class ShareListener implements SsoShareManager.ShareStateListener {

    private MainActivity activity;

    ShareListener(MainActivity context) {
        activity = context;
    }

    @Override
    public void onSuccess() {
        String result = "分享成功";
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
        activity.handResult(result);
    }

    @Override
    public void onError(String msg) {
        String result = "分享失败，出错信息：" + msg;
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
        activity.handResult(result);
    }

    @Override
    public void onCancel() {
        String result = "取消分享";
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
        activity.handResult(result);
    }
}
