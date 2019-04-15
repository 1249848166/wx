package com.su.wx.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

public class LrucacheUtil {

    private static LrucacheUtil instance;

    LruCache<String,Bitmap> lruCache;

    private LrucacheUtil(){
        lruCache=new LruCache<String,Bitmap>((int) (Runtime.getRuntime().maxMemory()/2)){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getHeight()*value.getRowBytes();
            }
        };
    }

    public static LrucacheUtil getInstance(){
        if(instance==null){
            synchronized (LrucacheUtil.class){
                if(instance==null){
                    instance=new LrucacheUtil();
                }
            }
        }
        return instance;
    }

    public void put(String key,Bitmap value){
        if(value!=null) {
            lruCache.put(key, value);
        }
    }

    public Bitmap get(String key){
        return lruCache.get(key);
    }
}
