package com.su.wx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationsQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.FalsifyFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.su.wx.R;
import com.su.wx.activity.ChatActivity;
import com.su.wx.activity.ConversationVerifyDeviceIdActivity;
import com.su.wx.adapters.ConversationAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.event.ConversationAdapterReceiveEvent;
import com.su.wx.models.WxUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeixinFragment extends Fragment {

    List<AVIMConversation> conversations;
    ConversationAdapter adapter;

    public WeixinFragment() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content=LayoutInflater.from(getContext()).inflate(R.layout.layout_weixin,container,false);
        try {
            SmartRefreshLayout refresh = content.findViewById(R.id.refresh);
            refresh.setRefreshHeader(new ClassicsHeader(getContext()));
            refresh.setRefreshFooter(new FalsifyFooter(getContext()));
            refresh.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    refreshLayout.finishRefresh(500);
                    getConversations();
                }
            });
            RecyclerView recycler = content.findViewById(R.id.recycler);
            conversations = new ArrayList<>();
            adapter = new ConversationAdapter(getContext(), conversations);
            recycler.setAdapter(adapter);
            recycler.addItemDecoration(new ListDecoration(5));
            recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            adapter.setOnConverationSelectListener(new ConversationAdapter.OnConverationSelectListener() {
                @Override
                public void select(int index, final AVIMConversation conversation) {
                    //获取最新会话判断隐私设置
                    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if(e==null){
                                String privacy= (String) conversation.getAttribute("privacy");
                                final List<String> ids = new ArrayList<>(conversation.getMembers());
                                try {
                                    JSONObject jo=new JSONObject(privacy);
                                    if(jo.has(WxUser.getCurrentUser().getUsername())){//设置过
                                        boolean pr=jo.getBoolean(WxUser.getCurrentUser().getUsername());
                                        if(pr){
                                            //前往验证密码界面
                                            Intent intent = new Intent(getContext(), ConversationVerifyDeviceIdActivity.class);
                                            intent.putStringArrayListExtra("ids", (ArrayList<String>) ids);
                                            startActivity(intent);
                                        }else{
                                            Intent intent = new Intent(getContext(), ChatActivity.class);
                                            intent.putStringArrayListExtra("ids", (ArrayList<String>) ids);
                                            startActivity(intent);
                                        }
                                    }else{
                                        Intent intent = new Intent(getContext(), ChatActivity.class);
                                        intent.putStringArrayListExtra("ids", (ArrayList<String>) ids);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }else{
                                Log.e("会话更新失败",e.toString());
                            }
                        }
                    });
                }
            });
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    getConversations();
                }
            }, 1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return content;
    }

    private void getConversations(){
        try {
            AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
            AVIMConversationsQuery query=client.getConversationsQuery();
            query.setLimit(10000);
            query.findInBackground(new AVIMConversationQueryCallback() {
                @Override
                public void done(List<AVIMConversation> conversations, AVIMException e) {
                    if (e == null) {
                        WeixinFragment.this.conversations.clear();
                        WeixinFragment.this.conversations.addAll(conversations);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEnent(ConversationAdapterReceiveEvent event){
        getConversations();
    }

}
