package com.su.wx.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.su.wx.R;
import com.su.wx.event.WoFragmentSetNicknameEvent;
import com.su.wx.models.WxUser;
import com.su.wx.utils.DeviceUtil;
import com.su.wx.utils.PasswordUtil;
import com.su.wx.utils.Storage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class UserinfoSettingActivity extends AppCompatActivity implements View.OnClickListener {

    Button change_pwd,edit_nickname;
    TextView pwd,new_pwd,confirm_new_pwd,new_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo_setting);

        change_pwd=findViewById(R.id.change_pwd);
        edit_nickname=findViewById(R.id.edit_nickname);
        pwd=findViewById(R.id.pwd);
        new_pwd=findViewById(R.id.new_pwd);
        confirm_new_pwd=findViewById(R.id.confirm_new_pwd);
        new_nickname=findViewById(R.id.new_nickname);
        change_pwd.setOnClickListener(this);
        edit_nickname.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_pwd:
                String password=pwd.getText().toString();
                final String new_password=new_pwd.getText().toString();
                final String confirm_new_password=confirm_new_pwd.getText().toString();
                if(password.equals("")||new_password.equals("")||confirm_new_password.equals("")){
                    Toast.makeText(this,"输入不完整",Toast.LENGTH_SHORT).show();
                    return;
                }
                final String password_md5=DeviceUtil.getMD5(password);
                AVQuery<WxUser> query=new AVQuery<>("WxUser");
                query.whereEqualTo("objectId",WxUser.getCurrentUser().getObjectId());
                query.findInBackground(new FindCallback<WxUser>() {
                    @Override
                    public void done(List<WxUser> users, AVException e) {
                        if(e==null&&users.size()>0){
                            String p=users.get(0).getPassword();
                            if(p.equals(password_md5)){
                                if(new_password.equals(confirm_new_password)){
                                    final String new_password_md5=DeviceUtil.getMD5(new_password);
                                    AVObject user=AVObject.createWithoutData("WxUser",WxUser.getCurrentUser().getObjectId());
                                    user.put("password",new_password_md5);
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            if(e==null){
                                                SharedPreferences sp=getSharedPreferences("user",MODE_PRIVATE);
                                                if(sp.getBoolean("isRemember",false)){//如果用户选择了保存密码，修改
                                                    PasswordUtil.savePassword(UserinfoSettingActivity.this,
                                                            WxUser.getCurrentUser().getUsername(),
                                                            new_password_md5,true);
                                                }
                                                Toast.makeText(UserinfoSettingActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }else{
                                                Log.e("密码修改失败",e.toString());
                                                Toast.makeText(UserinfoSettingActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(UserinfoSettingActivity.this, "两次输入的新密码不一样", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(UserinfoSettingActivity.this, "原密码不匹配", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(UserinfoSettingActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            Log.e("查询用户失败",e.toString());
                        }
                    }
                });
                break;
            case R.id.edit_nickname:
                final String nn=new_nickname.getText().toString();
                if(nn.equals("")){
                    Toast.makeText(this, "请输入新昵称", Toast.LENGTH_SHORT).show();
                    return;
                }
                AVObject user=AVObject.createWithoutData("WxUser",WxUser.getCurrentUser().getObjectId());
                user.put("nickname",nn);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e==null){
                            WxUser.getCurrentUser().setNickname(nn);
                            EventBus.getDefault().post(new WoFragmentSetNicknameEvent(nn));
                            Toast.makeText(UserinfoSettingActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Log.e("修改昵称","失败"+e.toString());
                            Toast.makeText(UserinfoSettingActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }
}
