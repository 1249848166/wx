package com.su.wx.event;

public class ConversationAdapterEvent {
    private String cid;

    public ConversationAdapterEvent(String unreadCount) {
        this.cid = unreadCount;
    }

    public String getCid() {
        return cid;
    }
}
