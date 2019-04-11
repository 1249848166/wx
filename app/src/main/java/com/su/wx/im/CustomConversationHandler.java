package com.su.wx.im;

import android.util.Log;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;

import java.util.List;

public class CustomConversationHandler extends AVIMConversationEventHandler {

    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
        Log.e("成员离开聊天群",members.get(0)+"被"+kickedBy+"踢出聊天群");
    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
        Log.e("成员被邀请",members.get(0)+"被"+invitedBy+"邀请加入群聊");
    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
        Log.e("你离开聊天群","aaa");
    }

    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
        Log.e("你加入了聊天群","aaa");
    }
}
