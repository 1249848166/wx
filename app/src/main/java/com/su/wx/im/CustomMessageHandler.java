package com.su.wx.im;

import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.su.wx.event.ChatActivityEvent;
import com.su.wx.event.ConversationAdapterRefreshEvent;

import org.greenrobot.eventbus.EventBus;

public class CustomMessageHandler extends AVIMMessageHandler {
    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client){
        EventBus.getDefault().post(new ConversationAdapterRefreshEvent(message.getConversationId()));//提醒对话列表显示未读消息数
        EventBus.getDefault().post(new ChatActivityEvent(message));
    }

    public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
        Log.e("处理消息回执","未实现");
    }
}
