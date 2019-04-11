package com.su.wx.activity;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.su.wx.R;
import com.su.wx.adapters.UserListAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.models.Friend;
import com.su.wx.models.WxUser;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends BaseActivity implements View.OnClickListener {

    RecyclerView recycler;
    List<WxUser> users;
    UserListAdapter adapter;

    private int limit=20;
    private int page=0;

    private final int search_type_refresh=0;
    private final int search_type_loadmore=1;

    EditText input;
    Button search;

    AlertDialog dialog_user;

    int selectIndex;
    WxUser selectUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        input=findViewById(R.id.username);
        search=findViewById(R.id.search);
        search.setOnClickListener(this);

        recycler=findViewById(R.id.recycler);
        users=new ArrayList<>();
        adapter=new UserListAdapter(this,users);
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(new ListDecoration(5));
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapter.setOnUserSelecteListener(new UserListAdapter.OnUserSelecteListener() {
            @Override
            public void select(int position, WxUser user) {
                selectIndex=position;
                selectUser=user;
                dialog_user=new AlertDialog.Builder(SearchUserActivity.this).create();
                dialog_user.getWindow().setWindowAnimations(R.style.Dialog1);
                WindowManager.LayoutParams params=dialog_user.getWindow().getAttributes();
                params.width=50;
                dialog_user.getWindow().setAttributes(params);
                View content=LayoutInflater.from(SearchUserActivity.this).inflate(R.layout.dialog_user,null);
                View look,add,cancel;
                look=content.findViewById(R.id.look);
                add=content.findViewById(R.id.add);
                cancel=content.findViewById(R.id.cancel);
                look.setOnClickListener(SearchUserActivity.this);
                add.setOnClickListener(SearchUserActivity.this);
                cancel.setOnClickListener(SearchUserActivity.this);
                dialog_user.setView(content);
                dialog_user.setCancelable(false);
                dialog_user.show();
            }
        });
    }

    private void searchUsers(String username, final int search_type){
        AVQuery<WxUser> query=new AVQuery<>("WxUser");
        query.whereContains("username",username);
        query.setLimit(limit);
        query.whereNotEqualTo("username",WxUser.getCurrentUser().getUsername());
        query.setSkip(limit*page);
        query.findInBackground(new FindCallback<WxUser>() {
            @Override
            public void done(List<WxUser> us, AVException e) {
                if(e==null){
                    if(us.size()>0) {
                        if (search_type == search_type_loadmore) {
                            int origin = users.size();
                            users.addAll(us);
                            adapter.notifyItemRangeInserted(origin, users.size() - 1);
                            page++;
                        } else {
                            users.clear();
                            users.addAll(us);
                            adapter.notifyDataSetChanged();
                            page = 0;
                        }
                    }else{
                        Toast.makeText(SearchUserActivity.this, "搜索结果为空", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(SearchUserActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    Log.e("搜索用户","失败:"+e.toString());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search:
                hideInput(this,v);
                String text=input.getText().toString();
                if(!TextUtils.isEmpty(text)){
                    searchUsers(text,search_type_refresh);
                }else{
                    Toast.makeText(this, "请输入用户名进行搜索", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.look:
                dialog_user.dismiss();
                break;
            case R.id.add:
                dialog_user.dismiss();
                if(selectUser!=null){
                    AlertDialog dialog=new AlertDialog.Builder(this).create();
                    dialog.setTitle("提示");
                    dialog.setMessage("是否添加\""+selectUser.getUsername()+"\"为好友");
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "再想想", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AVQuery<Friend> query=new AVQuery<>("Friend");
                            query.whereEqualTo("friendUser",selectUser);
                            query.findInBackground(new FindCallback<Friend>() {
                                @Override
                                public void done(List<Friend> friends, AVException e) {
                                    if(e==null){
                                        if(friends.size()==0){
                                            AVObject friend=new AVObject("Friend");
                                            friend.put("user",WxUser.getCurrentUser());
                                            friend.put("friendUser",selectUser);
                                            friend.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(AVException e) {
                                                    if(e==null){
                                                        Toast.makeText(SearchUserActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Log.e("添加好友","失败:"+e.toString());
                                                        Toast.makeText(SearchUserActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else{
                                            Toast.makeText(SearchUserActivity.this, "好友已经存在", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Log.e("查询是否存在好友","失败:"+e.toString());
                                    }
                                }
                            });
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                }
                break;
            case R.id.cancel:
                dialog_user.dismiss();
                break;
        }
    }

}
