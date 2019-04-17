package com.su.wx.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avos.avoscloud.im.v2.messages.AVIMVideoMessage;
import com.su.wx.R;
import com.su.wx.adapters.ChatAdapter;
import com.su.wx.decoration.ListDecoration;
import com.su.wx.event.ChatActivityEvent;
import com.su.wx.event.ConversationAdapterRefreshEvent;
import com.su.wx.models.WxUser;
import com.su.wx.utils.BitmapUtil;
import com.su.wx.utils.DeviceUtil;
import com.su.wx.utils.Storage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    List<String> ids;
    String name;

    EditText input;
    Button send;

    LinearLayoutManager linearLayoutManager;
    RecyclerView recycler;
    ChatAdapter adapter;
    List<AVIMMessage> messages;

    ImageView settingView;

    private static final int CODE_SELECT_IMAGE = 110;
    private static final int CODE_SELECT_AUDIO = 111;
    private static final int CODE_SELECT_VIDEO = 112;
    private static final int CODE_SELECT_FILE = 113;
    private static final int CODE_SELECT_LOCATION = 114;
    private static final int CODE_CAMERA = 115;

    String cid;

    View image, audio, video, location, file, camera;

    AVIMConversation conversation;

    View loading;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        EventBus.getDefault().register(this);

        try {

            initViews();

            ids = getIntent().getStringArrayListExtra("ids");
            StringBuilder sb = new StringBuilder();
            for (String id : ids) {
                sb.append(id).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            name = sb.toString();

            TextView title=findViewById(R.id.title);
            title.setText(name+"的聊天");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        messages.clear();
        createConversation(ids, name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final String filePath = getPath(this, data.getData());
            File f = new File(filePath);
            if (requestCode == CODE_SELECT_IMAGE) {
                try {
                    if (f.exists()) {
                        Log.e("文件", "存在");
                    } else {
                        Log.e("文件", "不存在");
                    }
                    if (!filePath.endsWith(".gif")
                            && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                        BitmapUtil.encode(Environment.getExternalStorageDirectory()
                                + Storage.cacheImagePath, filePath, new BitmapUtil.Callback() {
                            @Override
                            public void forDone() {
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            chat_image(Environment.getExternalStorageDirectory() + Storage.cacheImagePath,
                                                    filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 500);
                            }
                        });
                    } else {
                        chat_image(filePath, filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(requestCode==CODE_CAMERA){
                try {
                    BitmapUtil.encode(Environment.getExternalStorageDirectory()+Storage.cacheImagePath,
                            Environment.getExternalStorageDirectory()+Storage.cacheImagePath,
                            new BitmapUtil.Callback() {
                        @Override
                        public void forDone() {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        chat_image(Environment.getExternalStorageDirectory()+Storage.cacheImagePath,
                                                Storage.cacheImagePath.substring(
                                                        Storage.cacheImagePath.lastIndexOf("/") + 1, Storage.cacheImagePath.length()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },500);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CODE_SELECT_AUDIO) {

            } else if (requestCode == CODE_SELECT_VIDEO) {
                try {
                    FileInputStream fin=new FileInputStream(f);
                    long size=fin.available()/(1024*1024);
                    if(size>5){
                        AlertDialog dialog=new AlertDialog.Builder(ChatActivity.this).create();
                        dialog.setTitle("提示");
                        dialog.setMessage("请将视频文件限制在5M之内");
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    }else{
                        chat_video(filePath);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CODE_SELECT_FILE) {

            } else if (requestCode == CODE_SELECT_LOCATION) {

            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "操作取消", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未知原因失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateSetting(View v, long duration) {
        Animation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (cid != null) {
                    final Intent intent = new Intent(ChatActivity.this, ChatSettingActivity.class);
                    intent.putExtra("belong", name);
                    intent.putExtra("conv",cid);
                    conversation.fetchInfoInBackground(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if(e==null){
                                String privacy= (String) conversation.getAttribute("privacy");
                                try {
                                    JSONObject jo=new JSONObject(privacy);
                                    boolean pr=false;
                                    if(jo.has(WxUser.getCurrentUser().getUsername())){//有设置过
                                        pr=jo.getBoolean(WxUser.getCurrentUser().getUsername());
                                    }
                                    intent.putExtra("privacy",pr);
                                    startActivity(intent);
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }else{
                                Log.e("获取最新会话失败",e.toString());
                            }
                        }
                    });
                }
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
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        image = findViewById(R.id.image);
        image.setOnClickListener(this);
        audio = findViewById(R.id.audio);
        audio.setOnClickListener(this);
        video = findViewById(R.id.video);
        video.setOnClickListener(this);
        location = findViewById(R.id.location);
        location.setOnClickListener(this);
        file = findViewById(R.id.file);
        file.setOnClickListener(this);
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(this);
        settingView = findViewById(R.id.settingview);
        settingView.setOnClickListener(this);
        input = findViewById(R.id.input);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        recycler = findViewById(R.id.recycler);
        messages = new ArrayList<>();
        adapter = new ChatAdapter(this, messages);
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(new ListDecoration(20));
        linearLayoutManager=new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(linearLayoutManager);
    }

    private void createConversation(List<String> others, String name) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("importance", true);
        map.put("privacy","{}");
        AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
        client.createConversation(others, name, map, false, true,
                new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(final AVIMConversation conversation, AVIMException e) {
                        try {
                            if (e == null) {
                                ChatActivity.this.conversation = conversation;
                                cid = conversation.getConversationId();
                                conversation.queryMessages(new AVIMMessagesQueryCallback() {
                                    @Override
                                    public void done(List<AVIMMessage> messages, AVIMException e) {
                                        if (e == null && messages.size() > 0) {
                                            try {
                                                ChatActivity.this.messages.addAll(messages);
                                                adapter.notifyDataSetChanged();
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            } else {
                                Log.e("建立对话", "失败");
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void chat_text(String text) {
        try {
            loading.setVisibility(View.VISIBLE);
            final AVIMTextMessage msg = new AVIMTextMessage();
            msg.setText(text);
            conversation.sendMessage(msg, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (e == null) {
                        Log.e("发送消息", "成功");
                        int origin = messages.size();
                        messages.add(msg);
                        adapter.notifyItemInserted(origin);
                        recycler.smoothScrollToPosition(messages.size()-1);
                        //给自己发送刷新会话列表事件
                        Log.e("给自己发送刷新会话列表事件", "发送开始");
                        EventBus.getDefault().post(new ConversationAdapterRefreshEvent(conversation.getConversationId()));
                        loading.setVisibility(View.GONE);
                    } else {
                        Log.e("发送消息", "失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出错:" + e.toString(), Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }
    }

    private void chat_image(String filePath, String tag) throws IOException {
        try {
            loading.setVisibility(View.VISIBLE);
            final AVIMImageMessage picture = new AVIMImageMessage(filePath);
            picture.setText(tag);
            conversation.sendMessage(picture, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (e == null) {
                        int origin = messages.size();
                        messages.add(picture);
                        adapter.notifyItemInserted(origin);
                        EventBus.getDefault().post(new ConversationAdapterRefreshEvent(conversation.getConversationId()));
                        loading.setVisibility(View.GONE);
                    } else {
                        Log.e("发送消息", "失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出错:" + e.toString(), Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }
    }

    private void chat_audio(String localFilePath, String tag) throws FileNotFoundException {
        try {
            loading.setVisibility(View.VISIBLE);
            AVFile file = AVFile.withAbsoluteLocalPath(
                    localFilePath.substring(localFilePath.lastIndexOf("/") + 1, localFilePath.length())
                    , localFilePath);
            final AVIMAudioMessage m = new AVIMAudioMessage(file);
            m.setText(tag);
            conversation.sendMessage(m, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (e == null) {
                        int origin = messages.size();
                        messages.add(m);
                        adapter.notifyItemInserted(origin);
                        EventBus.getDefault().post(new ConversationAdapterRefreshEvent(conversation.getConversationId()));
                        loading.setVisibility(View.GONE);
                    } else {
                        Log.e("发送消息", "失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出错:" + e.toString(), Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }
    }

    private void chat_video(String localFilePath) throws FileNotFoundException {
        try {
            loading.setVisibility(View.VISIBLE);
            AVFile file = AVFile.withAbsoluteLocalPath(
                    localFilePath.substring(localFilePath.lastIndexOf("/") + 1, localFilePath.length()),
                    localFilePath);
            final AVIMVideoMessage m = new AVIMVideoMessage(file);
            conversation.sendMessage(m, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (e == null) {
                        int origin = messages.size();
                        messages.add(m);
                        adapter.notifyItemInserted(origin);
                        EventBus.getDefault().post(new ConversationAdapterRefreshEvent(conversation.getConversationId()));
                        loading.setVisibility(View.GONE);
                    } else {
                        Log.e("发送消息", "失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出错:" + e.toString(), Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }
    }

    private void chat_location(String text) {
        try {
            loading.setVisibility(View.VISIBLE);
            final AVIMLocationMessage locationMessage = new AVIMLocationMessage();
            double[] location = DeviceUtil.getLocation(this);
            if (location == null) {
                Toast.makeText(this, "获取位置失败", Toast.LENGTH_SHORT).show();
                return;
            }
            locationMessage.setLocation(new AVGeoPoint(location[0], location[1]));
            locationMessage.setText(text);
            conversation.sendMessage(locationMessage, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (e == null) {
                        int origin = messages.size();
                        messages.add(locationMessage);
                        adapter.notifyItemInserted(origin);
                        EventBus.getDefault().post(new ConversationAdapterRefreshEvent(conversation.getConversationId()));
                        loading.setVisibility(View.GONE);
                    } else {
                        Log.e("发送消息", "失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "出错:" + e.toString(), Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                hideInput(this, v);
                String msg = input.getText().toString();
                if (!msg.equals("")) {
                    AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
                    AVIMConversation conv = client.getConversation(cid);
                    chat_text(msg);
                    input.setText("");
                } else {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingview:
                animateSetting(settingView, 500);
                break;
            case R.id.image:
                Intent intent = getGalleryIntent(new Intent());
                this.startActivityForResult(intent, CODE_SELECT_IMAGE);
                break;
            case R.id.audio:

                break;
            case R.id.video:
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("video/*");
                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent1, CODE_SELECT_VIDEO);
                break;
            case R.id.location:

                break;
            case R.id.file:

                break;
            case R.id.camera:
                startCamera();
                break;
        }
    }

    private void startCamera() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(Environment.getExternalStorageDirectory()+Storage.cacheImagePath);
                Uri uri;
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(getApplicationContext(), "com.su.wx.fileprovider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                startActivityForResult(intent,CODE_CAMERA);
            } else {
                Toast.makeText(this, "没有存储卡", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatActivityEvent event) {
        if (cid != null) {
            AVIMClient client = AVIMClient.getInstance(WxUser.getCurrentUser().getUsername());
            AVIMConversation conv = client.getConversation(cid);
            conv.read();
        }
        int origin = messages.size();
        messages.add(event.getMsg());
        adapter.notifyItemRangeInserted(origin, messages.size());
        recycler.smoothScrollToPosition(messages.size()-1);
    }
}
