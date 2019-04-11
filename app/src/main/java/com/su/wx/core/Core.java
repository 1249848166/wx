package com.su.wx.core;

public class Core {

    static {
        System.loadLibrary("native-lib");
    }

    public static native String getAppId();

    public static native String getClientKey();
}
