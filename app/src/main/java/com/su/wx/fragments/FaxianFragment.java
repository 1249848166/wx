package com.su.wx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.FalsifyFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.header.FalsifyHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.su.wx.R;
import com.su.wx.activity.CircleActivity;

public class FaxianFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content=LayoutInflater.from(getContext()).inflate(R.layout.layout_faxian,container,false);
        SmartRefreshLayout refresh=content.findViewById(R.id.refresh);
        refresh.setRefreshHeader(new FalsifyHeader(getContext()));
        refresh.setRefreshFooter(new FalsifyFooter(getContext()));
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(500);
            }
        });

        View pengyouquan=content.findViewById(R.id.pengyouquan);
        pengyouquan.setOnClickListener(this);
        View saoyisao=content.findViewById(R.id.saoyisao);
        saoyisao.setOnClickListener(this);
        View yaoyiyao=content.findViewById(R.id.yaoyiyao);
        yaoyiyao.setOnClickListener(this);
        View kanyikan=content.findViewById(R.id.kanyikan);
        kanyikan.setOnClickListener(this);
        View souyisou=content.findViewById(R.id.souyisou);
        souyisou.setOnClickListener(this);
        View fujinderen=content.findViewById(R.id.fujinderen);
        fujinderen.setOnClickListener(this);
        View piaoliuping=content.findViewById(R.id.piaoliuping);
        piaoliuping.setOnClickListener(this);
        View gouwu=content.findViewById(R.id.gouwu);
        gouwu.setOnClickListener(this);
        View youxi=content.findViewById(R.id.youxi);
        youxi.setOnClickListener(this);
        View xiaochengxu=content.findViewById(R.id.xiaochengxu);
        xiaochengxu.setOnClickListener(this);

        return content;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pengyouquan:
                startActivity(new Intent(getContext(),CircleActivity.class));
                break;
            case R.id.saoyisao:

                break;
            case R.id.yaoyiyao:

                break;
            case R.id.kanyikan:

                break;
            case R.id.souyisou:

                break;
            case R.id.fujinderen:

                break;
            case R.id.piaoliuping:

                break;
            case R.id.gouwu:

                break;
            case R.id.youxi:

                break;
            case R.id.xiaochengxu:

                break;
        }
    }
}
