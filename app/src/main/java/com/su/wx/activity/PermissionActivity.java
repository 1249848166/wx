package com.su.wx.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.su.wx.models.WxUser;
import com.su.wx.utils.DeviceUtil;
import com.su.wx.utils.PasswordUtil;
import com.su.wx.utils.SuPermissionUtil;

import java.util.List;

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
                String[] password = PasswordUtil.readPassword(PermissionActivity.this);
                if (!password[0].equals("") && !password[1].equals("")) {
                    Log.e("aaa","aaa");
                    signUpOrLogin(password[0], password[1]);
                }else {
                    startActivity(new Intent(PermissionActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.onRequestPermissionsResult(0);
    }

    private void signUpOrLogin(final String usn, final String pwd) {
        if(!DeviceUtil.isNetworkConnected(this)) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle("提示");
            dialog.setMessage("网络没有连接，请连接网络");
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
            return;
        }
        if (usn.equals("") || pwd.equals("")) return;
        AVQuery<WxUser> query = new AVQuery<>("WxUser");
        query.whereEqualTo("username", usn);
        query.whereEqualTo("imei", DeviceUtil.getMD5(DeviceUtil.getIMEI(this)));
        query.findInBackground(new FindCallback<WxUser>() {
            @Override
            public void done(List<WxUser> avObjects, AVException avException) {
                if (avException == null) {
                    if (avObjects.size() > 0) {
                        //账号存在.登录
                        AVQuery<WxUser> query1 = new AVQuery<>("WxUser");
                        query1.whereEqualTo("username", usn);
                        query1.whereEqualTo("password", DeviceUtil.getMD5(pwd));
                        query1.findInBackground(new FindCallback<WxUser>() {
                            @Override
                            public void done(List<WxUser> users, AVException e) {
                                if (e == null && users.size() > 0) {
                                    WxUser.setCurrentUser(users.get(0));
                                    loginIMClient();
                                    startActivity(new Intent(PermissionActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(PermissionActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(PermissionActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });
                    } else {
                        //账号不存在
                        startActivity(new Intent(PermissionActivity.this, MainActivity.class));
                        finish();
                    }
                } else {
                    Log.e("登录验证", avException.toString());
                }
            }
        });
    }

    private void loginIMClient() {
        AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
        client.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    Toast.makeText(PermissionActivity.this, "成功连接IM服务器", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PermissionActivity.this, "IM服务器连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
