package com.su.wx.event;

public class ConversationAdapterRefreshEvent {
    private String cid;

    public ConversationAdapterRefreshEvent(String unreadCount) {
        this.cid = unreadCount;
    }

    public String getCid() {
        return cid;
    }
}
