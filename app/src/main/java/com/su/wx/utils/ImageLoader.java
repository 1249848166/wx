package com.su.wx.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {

    private static ImageLoader instance;

    private ImageLoader(){}

    public static ImageLoader getInstance(){
        if(instance==null){
            synchronized (ImageLoader.class){
                if(instance==null){
                    instance=new ImageLoader();
                }
            }
        }
        return instance;
    }

    public void loadImage(final ImageView imageView, final String url){
        if(LrucacheUtil.getInstance().get(url)!=null){
            imageView.setImageBitmap(LrucacheUtil.getInstance().get(url));
        }else {
            ThreadUtil.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection conn = null;
                    try {
                        URL u = new URL(url);
                        conn = (HttpURLConnection) u.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setReadTimeout(5000);
                        conn.setConnectTimeout(5000);
                        conn.connect();
                        if (conn.getResponseCode() == 200) {
                            final Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
                            LrucacheUtil.getInstance().put(url, bitmap);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
