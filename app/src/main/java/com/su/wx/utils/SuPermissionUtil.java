package com.su.wx.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SuPermissionUtil {

    private Callback callback;
    private int requestCode = -1;

    public void requestPermission(Context context, String[] permissions, int requestCode, Callback callback) {

        this.callback = callback;
        this.requestCode = requestCode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> toPermissionList = new ArrayList<>();
            if (permissions != null && permissions.length > 0) {
                for (String s : permissions) {
                    if (ContextCompat.checkSelfPermission(context.getApplicationContext(), s) != PackageManager.PERMISSION_GRANTED) {
                        toPermissionList.add(s);
                    }
                }
                if (toPermissionList.size() > 0) {
                    String[] ps = new String[toPermissionList.size()];
                    for (int i = 0; i < ps.length; i++) {
                        ps[i] = toPermissionList.get(i);
                    }
                    ActivityCompat.requestPermissions((Activity) context, ps, requestCode);
                } else {
                    if (callback != null)
                        callback.onResult(requestCode);
                }
            }
        } else {
            if (callback != null)
                callback.onResult(requestCode);
        }

    }

    public interface Callback {
        void onResult(int requestCode);
    }

    public void onRequestPermissionsResult(int requestCode) {
        if (callback != null)
            callback.onResult(requestCode);
    }

}