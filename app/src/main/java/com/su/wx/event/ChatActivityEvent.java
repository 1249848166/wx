package com.su.wx.event;

import com.avos.avoscloud.im.v2.AVIMMessage;

public class ChatActivityEvent {
    private AVIMMessage msg;

    public ChatActivityEvent(AVIMMessage msg) {
        this.msg = msg;
    }

    public AVIMMessage getMsg() {
        return msg;
    }
}
