package com.su.wx.models;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("Circle")
public class Circle extends AVObject {

    private WxUser from;
    private String content;

    public WxUser getFrom() {
        return from;
    }

    public void setFrom(WxUser from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
