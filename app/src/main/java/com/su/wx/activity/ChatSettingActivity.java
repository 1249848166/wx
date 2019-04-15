package com.su.wx.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationsQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.su.wx.R;
import com.su.wx.adapters.FriendListAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.models.Friend;
import com.su.wx.models.WxUser;
import com.su.wx.utils.StatusBarUtil;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatSettingActivity extends AppCompatActivity implements View.OnClickListener {

    PopupWindow popupWindow;
    View content;
    RecyclerView recycler;
    FriendListAdapter adapter;
    List<Friend> friends;

    String cid;

    SwitchButton switchButton_privacy;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_setting);

        StatusBarUtil.setStatusTextColor(true, this);

        TextView belong = findViewById(R.id.belong);
        String bl = getIntent().getStringExtra("belong");
        belong.setText(bl + "的聊天设置");

        cid = getIntent().getStringExtra("conv");

        switchButton_privacy = findViewById(R.id.privacy);
        switchButton_privacy.setChecked(getIntent().getBooleanExtra("privacy", false));
        switchButton_privacy.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, final boolean isChecked) {
                if (WxUser.getCurrentUser() != null) {
                    AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
                    AVIMConversationsQuery query = client.getConversationsQuery();
                    query.whereEqualTo("objectId", cid);
                    query.findInBackground(new AVIMConversationQueryCallback() {
                        @Override
                        public void done(List<AVIMConversation> conversations, AVIMException e) {
                            if (e == null) {
                                if(conversations.size()>0){
                                    AVIMConversation conversation=conversations.get(0);
                                    conversation.setAttribute("privacy",isChecked);
                                    conversation.updateInfoInBackground(new AVIMConversationCallback() {
                                        @Override
                                        public void done(AVIMException e) {
                                            if(e==null){
                                                Toast.makeText(ChatSettingActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                                            }else{
                                                switchButton_privacy.setChecked(!isChecked);
                                                Toast.makeText(ChatSettingActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else{
                                    switchButton_privacy.setChecked(!isChecked);
                                    Toast.makeText(ChatSettingActivity.this, "会话不存在", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("隐私会话设置失败",e.toString());
                                Toast.makeText(ChatSettingActivity.this, "隐私设置失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    switchButton_privacy.setChecked(!isChecked);
                }
            }
        });

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        initContent();
        popupWindow = new PopupWindow(content, width, (int) (height * 0.8));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popupwindow));
        popupWindow.setAnimationStyle(R.style.PopupWindowAnim);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                try {
                    backgroundAlpha(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getFriends();
    }

    void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void initContent() {
        content = LayoutInflater.from(this).inflate(R.layout.layout_popup, null);
        recycler = content.findViewById(R.id.recycler);
        friends = new ArrayList<>();
        adapter = new FriendListAdapter(this, friends);
        adapter.setOnFriendSelecteListener(new FriendListAdapter.OnFriendSelecteListener() {
            @Override
            public void select(final WxUser friendUser) {
                AlertDialog dialog = new AlertDialog.Builder(ChatSettingActivity.this).create();
                dialog.setTitle("提示");
                dialog.setMessage("是否将他添加到聊天当中？");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "没错", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //将选中的好友添加到聊天
                        AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
                        final AVIMConversation conv = client.getConversation(getIntent().getStringExtra("conv"));
                        AVQuery<WxUser> query = new AVQuery<>("WxUser");
                        query.whereEqualTo("objectId", friendUser.getObjectId());
                        query.findInBackground(new FindCallback<WxUser>() {
                            @Override
                            public void done(final List<WxUser> us, AVException e) {
                                if (e == null) {
                                    if (us.size() > 0) {
                                        conv.addMembers(Arrays.asList((String) us.get(0).getUsername()), new AVIMConversationCallback() {
                                            @Override
                                            public void done(AVIMException e) {
                                                if (e == null) {
                                                    conv.setName(conv.getName() + "," + us.get(0).getUsername());
                                                    conv.updateInfoInBackground(new AVIMConversationCallback() {
                                                        @Override
                                                        public void done(AVIMException e) {

                                                        }
                                                    });
                                                    conv.fetchInfoInBackground(new AVIMConversationCallback() {
                                                        @Override
                                                        public void done(AVIMException e) {

                                                        }
                                                    });
                                                    popupWindow.dismiss();
                                                    Toast.makeText(ChatSettingActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.e("添加好友进聊天失败", e.toString());
                                                    Toast.makeText(ChatSettingActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(ChatSettingActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                                    Log.e("查询好友用户失败", e.toString());
                                }
                            }
                        });
                    }
                });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "不要", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(new ListDecoration(10));
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void getFriends() {
        AVQuery<Friend> query = new AVQuery<>("Friend");
        query.whereEqualTo("user", WxUser.getCurrentUser());
        query.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> fs, AVException e) {
                if (e == null) {
                    if (fs.size() > 0) {
                        friends.clear();
                        friends.addAll(fs);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ChatSettingActivity.this, "您没有好友", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatSettingActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                    Log.e("查找好友失败", e.toString());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
