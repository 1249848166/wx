package com.su.wx.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.FalsifyFooter;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.su.wx.R;
import com.su.wx.activity.LoginActivity;
import com.su.wx.activity.SetDeviceIdActivity;
import com.su.wx.activity.UserinfoSettingActivity;
import com.su.wx.activity.VerifyDeviceIdActivity;
import com.su.wx.event.WoFragmentSetAvatarEvent;
import com.su.wx.event.WoFragmentSetNicknameEvent;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;
import com.su.wx.utils.PasswordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class WoFragment extends Fragment implements View.OnClickListener {

    ImageView avatar;
    TextView nickname;

    public interface WoFragmentCall {
        void onAvatarClick();
    }

    private WoFragmentCall woFragmentCall;

    public void setWoFragmentCall(WoFragmentCall woFragmentCall) {
        this.woFragmentCall = woFragmentCall;
    }

    public WoFragment() {
        EventBus.getDefault().register(this);
    }

    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content=null;
        try {
            content = LayoutInflater.from(getContext()).inflate(R.layout.layout_wo, container, false);

            SmartRefreshLayout refresh=content.findViewById(R.id.refresh);
            refresh.setRefreshHeader(new MaterialHeader(getContext()));
            refresh.setRefreshFooter(new FalsifyFooter(getContext()));
            refresh.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    refreshLayout.finishRefresh(500);
                }
            });

            avatar = content.findViewById(R.id.avatar);
            TextView username = content.findViewById(R.id.username);
            nickname = content.findViewById(R.id.nickname);
            WxUser user = WxUser.getCurrentUser();
            avatar.setOnClickListener(this);
            if (user != null && user.getAvatar() != null) {
                ImageLoader.getInstance().loadImage(avatar, user.getAvatar());
            }
            if (user != null) {
                username.setText(user.getUsername());
            }
            if (user != null && user.getNickname() != null) {
                nickname.setText("昵称:" + user.getNickname());
            } else {
                nickname.setText("昵称未设置");
            }

            View logout = content.findViewById(R.id.logout);
            logout.setOnClickListener(this);
            View deviceid = content.findViewById(R.id.deviceid);
            deviceid.setOnClickListener(this);
            View userinfo = content.findViewById(R.id.userinfo);
            userinfo.setOnClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                PasswordUtil.removePassword(getContext());
                startActivity(new Intent(getContext(), LoginActivity.class));
                ((Activity) getContext()).finish();
                break;
            case R.id.avatar:
                if (woFragmentCall != null) {
                    woFragmentCall.onAvatarClick();
                }
                break;
            case R.id.deviceid://前往设置设备id
                AVQuery<WxUser> query = new AVQuery<>("WxUser");
                query.whereEqualTo("objectId", WxUser.getCurrentUser().getObjectId());
                query.findInBackground(new FindCallback<WxUser>() {
                    @Override
                    public void done(List<WxUser> users, AVException e) {
                        if (e == null && users.size() > 0) {
                            String id = users.get(0).getDeviceId();
                            if (id != null && !id.equals("")) {
                                //id已经存在，提示是否重新设置
                                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                                dialog.setTitle("提示");
                                dialog.setMessage("设备ID已经存在，是否重新设置?");
                                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "是的", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent(getContext(), VerifyDeviceIdActivity.class));
                                    }
                                });
                                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "不了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                //id不存在，直接进入设置界面
                                startActivity(new Intent(getContext(), SetDeviceIdActivity.class));
                            }
                        } else {
                            Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
                            Log.e("查询用户", "失败:" + e.toString());
                        }
                    }
                });
                break;
            case R.id.userinfo:
                startActivity(new Intent(getContext(), UserinfoSettingActivity.class));
                break;
        }
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSetAvatar(WoFragmentSetAvatarEvent event) {
        try {
            String path = event.getAvatar_path();
            avatar.setImageBitmap(BitmapFactory.decodeFile(path));
            final AVFile file = AVFile.withAbsoluteLocalPath("avatar.png", path);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        AVObject user = AVObject.createWithoutData("WxUser", WxUser.getCurrentUser().getObjectId());
                        user.put("avatar", file.getUrl());
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    WxUser.getCurrentUser().setAvatar(file.getUrl());
                                    Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("设置头像", "失败" + e.toString());
                                    Toast.makeText(getContext(), "设置失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Log.e("图片上传", "失败" + e.toString());
                        Toast.makeText(getContext(), "图片上传失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSetNickname(WoFragmentSetNicknameEvent event){
        nickname.setText(event.getNickname());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
