package com.su.wx.im;

import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMFileMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMRecalledMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avos.avoscloud.im.v2.messages.AVIMVideoMessage;
import com.su.wx.event.ChatActivityEvent;
import com.su.wx.event.ConversationAdapterEvent;

import org.greenrobot.eventbus.EventBus;

public class CustomMessageHandler extends AVIMMessageHandler {
    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client){
        EventBus.getDefault().post(new ConversationAdapterEvent(message.getConversationId()));//提醒对话列表显示未读消息数
        if(message instanceof AVIMTextMessage){
            EventBus.getDefault().post(new ChatActivityEvent(message));
        }else if(message instanceof AVIMImageMessage){

        }else if(message instanceof AVIMAudioMessage){

        }else if(message instanceof AVIMVideoMessage){

        }else if(message instanceof AVIMLocationMessage){

        }else if(message instanceof AVIMFileMessage){

        }else if(message instanceof AVIMRecalledMessage){

        }
    }

    public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
        Log.e("处理消息回执","未实现");
    }
}
