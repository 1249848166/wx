package com.su.wx.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> list=new ArrayList<>();
        list.add(Manifest.permission.CAMERA);
        list.add(Manifest.permission.INTERNET);
        list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        list.add(Manifest.permission.ACCESS_NETWORK_STATE);
        list.add(Manifest.permission.READ_PHONE_STATE);
        list.add(Manifest.permission.ACCESS_WIFI_STATE);
        boolean hasNoPermited=false;
        for(String s:list){
            if(ContextCompat.checkSelfPermission(getApplicationContext(), s) != PackageManager.PERMISSION_GRANTED){
                hasNoPermited=true;
                break;
            }
        }
        if(hasNoPermited){
            startActivity(new Intent(this,PermissionActivity.class));//跳转到权限申请页面
            finish();
        }else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }, 1000);
        }
    }
}
