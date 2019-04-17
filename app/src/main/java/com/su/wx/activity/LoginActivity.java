package com.su.wx.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.su.wx.R;
import com.su.wx.event.NetworkDisableEvent;
import com.su.wx.event.NetworkEnableEvent;
import com.su.wx.models.WxUser;
import com.su.wx.utils.DeviceUtil;
import com.su.wx.utils.PasswordUtil;
import com.su.wx.utils.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView version;
    EditText username, password;
    Button login;
    CheckBox check;

    boolean isLoading = true;
    View loading;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EventBus.getDefault().register(this);

        StatusBarUtil.setStatusTextColor(true, this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("注册或登录");
        }

        initViews();
        loading.setVisibility(View.VISIBLE);

        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            version.setText("v" + info.versionName + "." + info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkSavedPasswordAndLogin();//检查本地是否保存了密码
    }

    private void checkSavedPasswordAndLogin() {
        String[] password = PasswordUtil.readPassword(this);
        if (!password[0].equals("") && !password[1].equals("")) {
            check.setChecked(true);
            signUpOrLogin(password[0], password[1]);
        } else {
            loading.setVisibility(View.GONE);
            isLoading = false;
        }
    }

    private void initViews() {
        loading = findViewById(R.id.loading);
        version = findViewById(R.id.version);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        login.setOnClickListener(this);
        check = findViewById(R.id.check);
    }

    @Override
    public void onClick(View v) {
        if (isLoading) {
            Toast.makeText(this, "正在检查登录，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.login:
                if(username.getText().toString().length()>20||password.getText().toString().length()>20){
                    Toast.makeText(this, "用户名或密码的长度超过了20个字符", Toast.LENGTH_SHORT).show();
                    return;
                }
                loading.setVisibility(View.VISIBLE);
                isLoading = true;
                validate();
                break;
        }
    }

    private void validate() {
        final String usn = username.getText().toString();
        final String pwd = password.getText().toString();
        if (!usn.equals("") && !pwd.equals("")) {
            PasswordUtil.savePassword(this, usn, pwd, check.isChecked());
            signUpOrLogin(usn, pwd);
        } else {
            Toast.makeText(this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
            isLoading = false;
        }
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
                                loading.setVisibility(View.GONE);
                                isLoading = false;
                                if (e == null && users.size() > 0) {
                                    WxUser.setCurrentUser(users.get(0));
                                    loginIMClient();
                                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        //账号不存在
                        //判断设备是否存在
                        AVQuery<WxUser> query1 = new AVQuery<>("WxUser");
                        query1.whereEqualTo("imei", DeviceUtil.getMD5(DeviceUtil.getIMEI(LoginActivity.this)));
                        query1.findInBackground(new FindCallback<WxUser>() {
                            @Override
                            public void done(List<WxUser> users, AVException e) {
                                loading.setVisibility(View.GONE);
                                isLoading = false;
                                if (e == null) {
                                    if (users.size() > 0) {//已经存在设备
                                        Toast.makeText(LoginActivity.this, "一个设备只能创建一个账号", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        //注册
                                        final AVObject user = new AVObject("WxUser");
                                        user.put("username", usn);
                                        user.put("password", DeviceUtil.getMD5(pwd));
                                        user.put("imei", DeviceUtil.getMD5(DeviceUtil.getIMEI(LoginActivity.this)));
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                if (e == null) {
                                                    user.fetchIfNeededInBackground(new GetCallback<AVObject>() {
                                                        @Override
                                                        public void done(AVObject object, AVException e) {
                                                            WxUser u = new WxUser();
                                                            u.setUsername(usn);
                                                            u.setObjectId(user.getObjectId());
                                                            u.setImei(DeviceUtil.getMD5(DeviceUtil.getIMEI(LoginActivity.this)));
                                                            WxUser.setCurrentUser(u);
                                                            loginIMClient();
                                                            Toast.makeText(LoginActivity.this, "注册成功并登录", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                            finish();
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                    Log.e("寻找设备失败", e.toString());
                                }
                            }
                        });
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
                    Toast.makeText(LoginActivity.this, "成功连接IM服务器", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "IM服务器连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void networkEnableEvent(NetworkEnableEvent event){
        loading.setVisibility(View.VISIBLE);
        isLoading = true;
        checkSavedPasswordAndLogin();//当网络可用时，检查登录
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void networkDisableEvent(NetworkDisableEvent event){
        Toast.makeText(this, "网络连接已经断开", Toast.LENGTH_SHORT).show();
        loading.setVisibility(View.GONE);
        isLoading = false;
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
    }
}
