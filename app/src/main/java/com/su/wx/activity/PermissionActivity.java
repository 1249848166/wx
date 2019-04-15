package com.su.wx.activity;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.su.wx.utils.SuPermissionUtil;

public class PermissionActivity extends AppCompatActivity {

    SuPermissionUtil permissionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionUtil=new SuPermissionUtil();
        permissionUtil.requestPermission(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE
        }, 0, new SuPermissionUtil.Callback() {
            @Override
            public void onResult(int requestCode) {
                Toast.makeText(PermissionActivity.this, "权限申请成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PermissionActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.onRequestPermissionsResult(0);
    }
}
