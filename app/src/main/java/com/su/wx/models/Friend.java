package com.su.wx.models;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("Friend")
public class Friend extends AVObject {

    private WxUser user;
    private WxUser friendUser;

    public WxUser getUser() {
        return user;
    }

    public void setUser(WxUser user) {
        this.user = user;
    }

    public WxUser getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(WxUser friendUser) {
        this.friendUser = friendUser;
    }
}
