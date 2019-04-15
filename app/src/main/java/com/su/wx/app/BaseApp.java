package com.su.wx.app;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.su.wx.core.Core;
import com.su.wx.im.CustomConversationHandler;
import com.su.wx.im.CustomMessageHandler;
import com.su.wx.models.Friend;
import com.su.wx.models.WxUser;

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();

        AVObject.registerSubclass(WxUser.class);
        AVObject.registerSubclass(Friend.class);

        AVOSCloud.initialize(this,Core.getAppId(),Core.getClientKey());
        AVOSCloud.setDebugLogEnabled(true);
        AVIMClient.setMessageQueryCacheEnable(true);//不启用缓存消息
        AVIMClient.setUnreadNotificationEnabled(true);//开启未读消息

        AVIMMessageManager.setConversationEventHandler(new CustomConversationHandler());
        AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
    }

    private static Context context;

    public static Context getAppContext(){
        return context;
    }
}
