package com.su.wx.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.su.wx.R;
import com.su.wx.views.VideoPlayerView;

public class TryActivity extends AppCompatActivity {

    VideoPlayerView player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try);

        player=findViewById(R.id.player);
        player.initializePlayer("http://bmob-cdn-21427.b0.upaiyun.com/2018/11/20/2601d49f407c87a080a7a02396e9b167.mp4");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.releasePlayer();
    }
}
