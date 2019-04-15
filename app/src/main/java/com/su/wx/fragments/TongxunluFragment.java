package com.su.wx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.footer.FalsifyFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.su.wx.R;
import com.su.wx.activity.ChatActivity;
import com.su.wx.adapters.FriendListAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.models.Friend;
import com.su.wx.models.WxUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TongxunluFragment extends Fragment implements View.OnClickListener {

    List<Friend> friends;
    FriendListAdapter adapter;

    SmartRefreshLayout refresh;

    WxUser selectFriend;

    AlertDialog dialog_friend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = LayoutInflater.from(getContext()).inflate(R.layout.layout_tongxunlu, container, false);
        try {
            refresh = content.findViewById(R.id.refresh);
            refresh.setRefreshHeader(new ClassicsHeader(getContext()));
            refresh.setRefreshFooter(new FalsifyFooter(getContext()));
            refresh.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    refreshLayout.finishRefresh(500);
                    getFriendList();
                }
            });
            RecyclerView recycler = content.findViewById(R.id.recycler);
            friends = new ArrayList<>();
            adapter = new FriendListAdapter(getContext(), friends);
            recycler.setAdapter(adapter);
            ListDecoration decoration = new ListDecoration(5);
            recycler.addItemDecoration(decoration);
            recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            getFriendList();
            adapter.setOnFriendSelecteListener(new FriendListAdapter.OnFriendSelecteListener() {
                @Override
                public void select(WxUser friendUser) {
                    selectFriend = friendUser;
                    dialog_friend = new AlertDialog.Builder(getContext()).create();
                    dialog_friend.getWindow().setWindowAnimations(R.style.Dialog1);
                    WindowManager.LayoutParams params = dialog_friend.getWindow().getAttributes();
                    params.width = 50;
                    dialog_friend.getWindow().setAttributes(params);
                    View content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_friend, null);
                    View look, delete, chat, cancel;
                    look = content.findViewById(R.id.look);
                    delete = content.findViewById(R.id.delete);
                    chat = content.findViewById(R.id.chat);
                    cancel = content.findViewById(R.id.cancel);
                    look.setOnClickListener(TongxunluFragment.this);
                    delete.setOnClickListener(TongxunluFragment.this);
                    chat.setOnClickListener(TongxunluFragment.this);
                    cancel.setOnClickListener(TongxunluFragment.this);
                    dialog_friend.setView(content);
                    dialog_friend.setCancelable(false);
                    dialog_friend.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return content;
    }

    private void getFriendList() {
        AVQuery<Friend> query = new AVQuery<>("Friend");
        query.whereEqualTo("user", WxUser.getCurrentUser());
        AVQuery<Friend> query1=new AVQuery<>("Friend");
        query1.whereEqualTo("friendUser",WxUser.getCurrentUser());
        query.limit(10000);
        query1.limit(10000);
        AVQuery<Friend> query2=AVQuery.or(Arrays.asList(query,query1));
        query2.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> fs, AVException e) {
                if (e == null) {
                    friends.clear();
                    friends.addAll(fs);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("获取好友列表", "失败:" + e.toString());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.look:
                dialog_friend.dismiss();
                break;
            case R.id.delete:
                dialog_friend.dismiss();
                selectFriend.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("删除好友", "失败:" + e.toString());
                            Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.chat:
                dialog_friend.dismiss();
                final Intent intent = new Intent(getContext(), ChatActivity.class);
                final ArrayList<String> ids = new ArrayList<>();
                String objectId = selectFriend.getObjectId();
                AVQuery<WxUser> query = new AVQuery<>("WxUser");
                query.whereEqualTo("objectId", objectId);
                query.findInBackground(new FindCallback<WxUser>() {
                    @Override
                    public void done(List<WxUser> users, AVException e) {
                        if (e == null && users.size() > 0) {
                            ids.add(users.get(0).getUsername());
                            ids.add(WxUser.getCurrentUser().getUsername());
                            intent.putStringArrayListExtra("ids", ids);
                            startActivity(intent);
                        }
                    }
                });
                break;
            case R.id.cancel:
                dialog_friend.dismiss();
                break;
        }
    }
}
