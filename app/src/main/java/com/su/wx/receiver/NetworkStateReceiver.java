package com.su.wx.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.su.wx.event.NetworkDisableEvent;
import com.su.wx.event.NetworkEnableEvent;

import org.greenrobot.eventbus.EventBus;

public class NetworkStateReceiver extends BroadcastReceiver {

    public static final int NETSTATUS_INAVAILABLE = 0;
    public static final int NETSTATUS_WIFI = 1;
    public static final int NETSTATUS_MOBILE = 2;
    public static int netStatus = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo allNetInfo = cm.getActiveNetworkInfo();

        if (allNetInfo == null) {
            if (mobileNetInfo != null && (mobileNetInfo.isConnected() || mobileNetInfo.isConnectedOrConnecting())) {
                netStatus = NETSTATUS_MOBILE;
            } else if (wifiNetInfo != null && (wifiNetInfo.isConnected() || wifiNetInfo.isConnectedOrConnecting())) {
                netStatus = NETSTATUS_WIFI;
            } else {
                netStatus = NETSTATUS_INAVAILABLE;
            }
        } else {
            if (allNetInfo.isConnected() || allNetInfo.isConnectedOrConnecting()) {
                if (mobileNetInfo.isConnected() || mobileNetInfo.isConnectedOrConnecting()) {
                    netStatus = NETSTATUS_MOBILE;
                } else {
                    netStatus = NETSTATUS_WIFI;
                }
            } else {
                netStatus = NETSTATUS_INAVAILABLE;
            }
        }
        if (netStatus == NETSTATUS_INAVAILABLE) {
            EventBus.getDefault().post(new NetworkEnableEvent());
            Log.e("网络连接状态","打开");
        } else if (netStatus == NETSTATUS_MOBILE) {
            EventBus.getDefault().post(new NetworkDisableEvent());
            Log.e("网络连接状态","断开");
        } else {
            EventBus.getDefault().post(new NetworkDisableEvent());
            Log.e("网络连接状态","断开");
        }
    }

}
