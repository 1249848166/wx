package com.su.wx.activity;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.su.wx.R;
import com.su.wx.event.WoFragmentSetAvatarEvent;
import com.su.wx.fragments.FaxianFragment;
import com.su.wx.fragments.TongxunluFragment;
import com.su.wx.fragments.WeixinFragment;
import com.su.wx.fragments.WoFragment;
import com.su.wx.models.WxUser;
import com.su.wx.utils.Storage;
import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    ViewPager viewPager;
    TabLayout tabLayout;
    List<Fragment> fragments;
    PageAdapter adapter;
    List<TabLayout.Tab> tabs;
    String[] tabNames=new String[]{"唯信","通讯录","发现","我"};
    int[] lightIcons=new int[]{R.drawable.weixin_light,R.drawable.tongxunlu_light,R.drawable.faxian_light,R.drawable.wo_light};
    int[] normalIcons=new int[]{R.drawable.weixin_normal,R.drawable.tongxunlu_normal,R.drawable.faxian_normal,R.drawable.wo_normal};

    TopRightMenu topRightMenu;

    Toolbar toolbar;
    View addView,searchView;

    TextView title;

    final int CODE_SELECT_FILE=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolBar();
        initTopRightMenu();
        initFragments();
        initViewPager();
        initTabLayout();
    }

    private void initToolBar() {
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addView=toolbar.findViewById(R.id.add);
        searchView=toolbar.findViewById(R.id.search);
        addView.setOnClickListener(this);
        searchView.setOnClickListener(this);
        title=toolbar.findViewById(R.id.title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(WxUser.getCurrentUser()==null){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(WxUser.getCurrentUser()!=null) {
            AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
            if (client != null) {
                client.close(new AVIMClientCallback() {
                    @Override
                    public void done(AVIMClient client, AVIMException e) {
                        if (e == null) {
                            Log.e("关闭im连接", "成功");
                        } else {
                            Log.e("关闭im连接", "失败");
                        }
                    }
                });
            }
        }
    }

    private void initTopRightMenu() {
        topRightMenu=new TopRightMenu(this);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.ic_person_black_24dp, "添加好友"));
        menuItems.add(new MenuItem(R.drawable.ic_group_black_24dp, "发起多人聊天"));
        topRightMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .showIcon(true).dimBackground(true)
                .needAnimationStyle(true).setAnimationStyle(R.style.TRM_ANIM_STYLE).addMenuList(menuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        if(position==0){//添加好友
                            startActivity(new Intent(MainActivity.this,
                                    SearchUserActivity.class));
                        }else if(position==1){//发起多人聊天

                        }
                    }
                });
    }

    private void initFragments() {
        WeixinFragment fragment_weixin=new WeixinFragment();
        TongxunluFragment fragment_tongxunlu=new TongxunluFragment();
        FaxianFragment fragment_faxian=new FaxianFragment();
        WoFragment fragment_wo=new WoFragment();
        fragment_wo.setWoFragmentCall(new WoFragment.WoFragmentCall() {
            @Override
            public void onAvatarClick() {
                Intent intent = getGalleryIntent(new Intent());
                startActivityForResult(intent,CODE_SELECT_FILE);
            }
        });
        fragments=new ArrayList<>();
        fragments.add(fragment_weixin);
        fragments.add(fragment_tongxunlu);
        fragments.add(fragment_faxian);
        fragments.add(fragment_wo);
    }

    private void initViewPager() {
        viewPager=findViewById(R.id.viewPager);
        adapter=new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1000);
    }

    private void initTabLayout() {
        tabLayout=findViewById(R.id.tabLayout);
        tabs=new ArrayList<>();
        TabLayout.Tab tab_weixin=tabLayout.newTab().setIcon(lightIcons[0]).setText(tabNames[0]);
        TabLayout.Tab tab_tongxunlu=tabLayout.newTab().setIcon(normalIcons[1]).setText(tabNames[1]);
        TabLayout.Tab tab_faxian=tabLayout.newTab().setIcon(normalIcons[2]).setText(tabNames[2]);
        TabLayout.Tab tab_wo=tabLayout.newTab().setIcon(normalIcons[3]).setText(tabNames[3]);
        tabs.add(tab_weixin);
        tabs.add(tab_tongxunlu);
        tabs.add(tab_faxian);
        tabs.add(tab_wo);
        for(TabLayout.Tab tab:tabs){
            tabLayout.addTab(tab);
        }
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(lightIcons[0]).setText(tabNames[0]);
        tabLayout.getTabAt(1).setIcon(normalIcons[1]).setText(tabNames[1]);
        tabLayout.getTabAt(2).setIcon(normalIcons[2]).setText(tabNames[2]);
        tabLayout.getTabAt(3).setIcon(normalIcons[3]).setText(tabNames[3]);
        tabLayout.setSelectedTabIndicator(null);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                for(int i=0;i<tabLayout.getTabCount();i++){
                    if(i==tabLayout.getSelectedTabPosition()){
                        tabLayout.getTabAt(i).setIcon(lightIcons[i]);
                    }else{
                        tabLayout.getTabAt(i).setIcon(normalIcons[i]);
                    }
                }
                switch (tabLayout.getSelectedTabPosition()){
                    case 0:
                        title.setText("唯信");
                        break;
                    case 1:
                        title.setText("通讯录");
                        break;
                    case 2:
                        title.setText("发现");
                        break;
                    case 3:
                        title.setText("我");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add:
                if(topRightMenu!=null){
                    topRightMenu.showAsDropDown(addView,-225,0);
                }
                break;
            case R.id.search:

                break;
        }
    }

    class PageAdapter extends FragmentPagerAdapter{

        PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CODE_SELECT_FILE){
            if(resultCode==RESULT_OK){
                String filePath=getPath(this,data.getData());
                File f=new File(filePath);
                if(f.exists()){
                    Log.e("文件","存在");
                }else{
                    Log.e("文件","不存在");
                }
                Toast.makeText(this, "选择成功", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new WoFragmentSetAvatarEvent(filePath));
            }else if(resultCode==RESULT_CANCELED){
                Toast.makeText(this, "操作取消", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "未知原因失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
