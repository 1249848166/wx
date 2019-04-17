package com.su.wx.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.su.wx.R;

public class VideoPlayerView extends RelativeLayout{

    private final String TAG=getClass().getSimpleName();
    private PlayerView playerView;
    private ExoPlayer player;
    private boolean playWhenReady;
    private int currentWindow;
    private long playbackPosition;
    private ComponentListener componentListener;

    public VideoPlayerView(Context context) {
        this(context,null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        @SuppressLint("InflateParams")
        RelativeLayout layout= (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.item_video,null);
        this.addView(layout);
        playerView=layout.findViewById(R.id.player);
        player = ExoPlayerFactory.newSimpleInstance(
                context,
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        componentListener=new ComponentListener();
        player.addListener(componentListener);
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
    }

    public void initializePlayer(String path) {
        if (player==null){
            player = ExoPlayerFactory.newSimpleInstance(
                    getContext(),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());
            componentListener=new ComponentListener();
            player.addListener(componentListener);
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
        //创建一个mp4媒体文件
        Uri uri = Uri.parse(path);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    public void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(componentListener);
            player.release();
            player = null;
        }
    }

    public static class ComponentListener extends Player.DefaultEventListener {
        private final String TAG=getClass().getSimpleName();
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            if (playWhenReady && playbackState == Player.STATE_READY) {
                Log.e(TAG, "onPlayerStateChanged: actually playing media");
            }
            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case Player.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case Player.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case Player.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.e(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);
        }
    }

}
