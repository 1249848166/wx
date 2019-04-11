package com.su.wx.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.google.gson.Gson;
import com.su.wx.R;
import com.su.wx.adapters.ChatAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.event.ChatActivityEvent;
import com.su.wx.models.WxUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    AVIMClient client;

    List<String> ids;

    EditText input;
    Button send;

    RecyclerView recycler;
    ChatAdapter adapter;
    List<AVIMMessage> messages;

    AVIMConversation conversation;

    ImageView settingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        EventBus.getDefault().register(this);

        try {

            initViews();

            client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());

            ids = getIntent().getStringArrayListExtra("ids");
            StringBuilder sb = new StringBuilder();
            for (String id : ids) {
                sb.append(id).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);

            android.app.ActionBar actionBar=getActionBar();
            if(actionBar!=null){
                actionBar.setTitle(sb.toString());
            }

            createConversation(ids, sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateSetting(View v,long duration){
        Animation animation=new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent=new Intent(ChatActivity.this,ChatSettingActivity.class);
                intent.putExtra("conv",conversation.getConversationId());
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {
        settingView=findViewById(R.id.settingview);
        settingView.setOnClickListener(this);
        input = findViewById(R.id.input);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        recycler = findViewById(R.id.recycler);
        messages=new ArrayList<>();
        adapter=new ChatAdapter(this,messages);
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(new ListDecoration(20));
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
    }

    private void createConversation(List<String> others, String name) {
        if (client != null) {
            HashMap<String,Object> map=new HashMap<>();
            map.put("importance",true);
            client.createConversation(others, name, map, false, true,
                    new AVIMConversationCreatedCallback() {
                        @Override
                        public void done(AVIMConversation conversation, AVIMException e) {
                            if (e == null) {
                                ChatActivity.this.conversation=conversation;
                                Log.e("建立交流", "成功");
                                ChatActivity.this.conversation.queryMessages(new AVIMMessagesQueryCallback() {
                                    @Override
                                    public void done(List<AVIMMessage> messages, AVIMException e) {
                                        if(e==null&&messages.size()>0){
                                            ChatActivity.this.messages.addAll(messages);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            } else {
                                Log.e("建立交流", "失败");
                            }
                        }
                    });
        }
    }

    private void chat(AVIMConversation conversation, String text) {
        final AVIMTextMessage msg = new AVIMTextMessage();
        msg.setText(text);
        conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    Log.e("发送消息", "成功");
                    int origin=messages.size();
                    messages.add(msg);
                    adapter.notifyItemRangeInserted(origin,messages.size());
                } else {
                    Log.e("发送消息", "失败");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send:
                hideInput(this,v);
                String msg=input.getText().toString();
                if(!msg.equals("")){
                    chat(conversation,msg);
                    input.setText("");
                }else{
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingview:
                animateSetting(settingView,500);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatActivityEvent event){
        conversation.read();
        int origin=messages.size();
        messages.add(event.getMsg());
        adapter.notifyItemRangeInserted(origin,messages.size());
    }
}
