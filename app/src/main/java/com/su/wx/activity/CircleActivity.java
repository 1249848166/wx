package com.su.wx.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.google.gson.Gson;
import com.scwang.smartrefresh.header.PhoenixHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.su.wx.R;
import com.su.wx.adapters.CircleAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.event.CircleRefreshEvent;
import com.su.wx.models.Circle;
import com.su.wx.models.Friend;
import com.su.wx.models.WxUser;
import com.su.wx.utils.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CircleActivity extends AppCompatActivity implements View.OnClickListener {

    private int page=0;
    private final int limit=5;
    private List<WxUser> users;
    List<Circle> circles;
    CircleAdapter adapter;

    final int type_refresh=0;
    final int type_loadmore=1;

    View write;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);

        EventBus.getDefault().register(this);

        StatusBarUtil.setStatusTextColor(true,this);

        write=findViewById(R.id.write);
        write.setOnClickListener(this);

        SmartRefreshLayout refresh=findViewById(R.id.refresh);
        refresh.setRefreshHeader(new PhoenixHeader(this));
        refresh.setRefreshFooter(new ClassicsFooter(this));
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(500);
                page=0;
                getCircleContents(type_refresh);
            }
        });
        refresh.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore(500);
                getCircleContents(type_loadmore);
            }
        });

        circles=new ArrayList<>();
        adapter=new CircleAdapter(this,circles);
        RecyclerView recycler=findViewById(R.id.recycler);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recycler.addItemDecoration(new ListDecoration(10));

        users=new ArrayList<>();
        users.add(WxUser.getCurrentUser());
        AVQuery<Friend> query=new AVQuery<>("Friend");
        query.whereEqualTo("user",WxUser.getCurrentUser());
        AVQuery<Friend> query1=new AVQuery<>("Friend");
        query1.whereEqualTo("friendUser",WxUser.getCurrentUser());
        AVQuery<Friend> query2=AVQuery.or(Arrays.asList(query,query1));
        query2.findInBackground(new FindCallback<Friend>() {
            @Override
            public void done(List<Friend> friends, AVException e) {
                try {
                    if (e == null) {
                        if(friends.size()>0) {
                            for (Friend f : friends) {
                                WxUser user= (WxUser) f.get("user");
                                WxUser friendUser= (WxUser) f.get("friendUser");
                                if (user.getObjectId().equals(WxUser.getCurrentUser().getObjectId())) {
                                    users.add(friendUser);
                                } else {
                                    users.add(user);
                                }
                            }
                        }
                        //有了users列表，通过users在circle里面寻找自己的朋友圈
                        page = 0;
                        getCircleContents(type_refresh);
                    } else {
                        Log.e("查询好友用户失败", e.toString());
                    }
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void getCircleContents(final int type){
        AVQuery<Circle> query4=null;
        List<AVQuery<Circle>> querys=new ArrayList<>();
        for(int i=0;i<users.size();i++){
            AVQuery<Circle> query3=new AVQuery<>("Circle");
            query3.whereEqualTo("from",users.get(i));
            querys.add(query3);
        }
        query4=AVQuery.or(querys);
        query4.orderByDescending("createdAt");//时间降序排列
        query4.setLimit(limit);
        query4.setSkip(limit*page);
        query4.findInBackground(new FindCallback<Circle>() {
            @Override
            public void done(List<Circle> cs, AVException e) {
                if(e==null){
                    if(type==type_refresh){
                        if(cs.size()>0){
                            page=1;
                            circles.clear();
                            circles.addAll(cs);
                            adapter.notifyDataSetChanged();
                        }else{
                            circles.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(CircleActivity.this, "你的朋友圈空空如也", Toast.LENGTH_SHORT).show();
                        }
                    }else if(type==type_loadmore){
                        if(cs.size()>0){
                            page++;
                            int origin=circles.size();
                            circles.addAll(cs);
                            adapter.notifyItemRangeInserted(origin,circles.size()-1);
                        }else{
                            Toast.makeText(CircleActivity.this, "没有更多内容", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Log.e("查询朋友圈错误",e.toString());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.write:
                animateWrite(v,500);
                break;
        }
    }

    private void animateWrite(View v,long duration){
        Animator animator=ObjectAnimator.ofFloat(v,"rotationY",0f,-10f,-60f,0f,50f,-30f,10f,0f);
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(CircleActivity.this,WriteCircleActivity.class));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
        Animator animator1=ObjectAnimator.ofFloat(v,"scaleX",1f,0.95f,0.9f,0.95f,1f);
        animator1.setDuration(duration);
        animator1.start();
        Animator animator2=ObjectAnimator.ofFloat(v,"scaleY",1f,0.95f,0.9f,0.95f,1f);
        animator2.setDuration(duration);
        animator2.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshEvent(CircleRefreshEvent event){
        page=0;
        getCircleContents(type_refresh);
    }
}
