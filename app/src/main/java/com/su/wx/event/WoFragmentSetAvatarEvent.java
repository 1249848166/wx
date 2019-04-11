package com.su.wx.event;

public class WoFragmentSetAvatarEvent {
    private String avatar_path;

    public WoFragmentSetAvatarEvent(String avatar_path) {
        this.avatar_path = avatar_path;
    }

    public String getAvatar_path() {
        return avatar_path;
    }
}
