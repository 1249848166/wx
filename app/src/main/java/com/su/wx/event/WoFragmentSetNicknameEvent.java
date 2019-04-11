package com.su.wx.event;

public class WoFragmentSetNicknameEvent {
    private String nickname;

    public WoFragmentSetNicknameEvent(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
