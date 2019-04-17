package com.su.wx.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.google.gson.Gson;
import com.su.wx.R;
import com.su.wx.event.CircleRefreshEvent;
import com.su.wx.event.WoFragmentSetAvatarEvent;
import com.su.wx.models.WxUser;
import com.su.wx.utils.BitmapUtil;
import com.su.wx.utils.ImageLoader;
import com.su.wx.utils.StatusBarUtil;
import com.su.wx.utils.Storage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WriteCircleActivity extends BaseActivity implements View.OnClickListener {

    EditText text;
    ImageView image1, image2, image3;
    ImageView delete1, delete2, delete3;
    Button upload;

    final int CODE_SELECT_IMAGE1 = 110;
    final int CODE_SELECT_IMAGE2 = 111;
    final int CODE_SELECT_IMAGE3 = 112;

    List<AVFile> imageUrls=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_circle);

        StatusBarUtil.setStatusTextColor(true,this);

        text = findViewById(R.id.text);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        delete1 = findViewById(R.id.delete1);
        delete2 = findViewById(R.id.delete2);
        delete3 = findViewById(R.id.delete3);

        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        delete1.setOnClickListener(this);
        delete2.setOnClickListener(this);
        delete3.setOnClickListener(this);

        upload=findViewById(R.id.upload);
        upload.setOnClickListener(this);

        imageUrls.add(null);
        imageUrls.add(null);
        imageUrls.add(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image1:
                Intent intent1 = getGalleryIntent(new Intent());
                startActivityForResult(intent1, CODE_SELECT_IMAGE1);
                break;
            case R.id.image2:
                Intent intent2 = getGalleryIntent(new Intent());
                startActivityForResult(intent2, CODE_SELECT_IMAGE2);
                break;
            case R.id.image3:
                Intent intent3 = getGalleryIntent(new Intent());
                startActivityForResult(intent3, CODE_SELECT_IMAGE3);
                break;
            case R.id.delete1:
                imageUrls.get(0).deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e==null){
                            Toast.makeText(WriteCircleActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(WriteCircleActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                imageUrls.set(0,null);
                image1.setImageResource(R.drawable.default_holder);
                delete1.setVisibility(View.GONE);
                break;
            case R.id.delete2:
                imageUrls.get(1).deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e==null){
                            Toast.makeText(WriteCircleActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(WriteCircleActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                imageUrls.set(1,null);
                image2.setImageResource(R.drawable.default_holder);
                delete2.setVisibility(View.GONE);
                break;
            case R.id.delete3:
                imageUrls.get(2).deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e==null){
                            Toast.makeText(WriteCircleActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(WriteCircleActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                imageUrls.set(2,null);
                image3.setImageResource(R.drawable.default_holder);
                delete3.setVisibility(View.GONE);
                break;
            case R.id.upload:
                AlertDialog dialog=new AlertDialog.Builder(this).create();
                dialog.setTitle("提示");
                dialog.setMessage("是否提交?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            final JSONObject jsonObject = new JSONObject();
                            String t = text.getText().toString();
                            if (t != null && !t.equals("")) {
                                jsonObject.put("text", t);
                            }
                            if(imageUrls.get(0)!=null){
                                jsonObject.put("image1",imageUrls.get(0).getUrl());
                            }
                            if(imageUrls.get(1)!=null){
                                jsonObject.put("image2",imageUrls.get(1).getUrl());
                            }
                            if(imageUrls.get(2)!=null){
                                jsonObject.put("image3",imageUrls.get(2).getUrl());
                            }
                            if(WxUser.getCurrentUser()!=null){
                                String un="";
                                if(WxUser.getCurrentUser().getUsername()!=null){
                                    un=WxUser.getCurrentUser().getUsername();
                                }
                                if(WxUser.getCurrentUser().getNickname()!=null){
                                    un=un+"("+WxUser.getCurrentUser().getNickname()+")";
                                }
                                jsonObject.put("username",un);
                            }
                            String timeStr=getTimeStr(System.currentTimeMillis());
                            jsonObject.put("time",timeStr);
                            //上传到服务器
                            final AVObject circle=new AVObject("Circle");
                            WxUser.getCurrentUser().fetchInBackground(new GetCallback<AVObject>() {
                                @Override
                                public void done(AVObject u, AVException e) {
                                    if(e==null){
                                        if(u!=null){
                                            circle.put("from",WxUser.getCurrentUser());
                                            String json=jsonObject.toString();
                                            circle.put("content",json);
                                            circle.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(AVException e) {
                                                    if(e==null){
                                                        text.setText("");
                                                        delete1.setVisibility(View.GONE);
                                                        delete2.setVisibility(View.GONE);
                                                        delete3.setVisibility(View.GONE);
                                                        image1.setImageResource(R.drawable.default_holder);
                                                        image2.setImageResource(R.drawable.default_holder);
                                                        image3.setImageResource(R.drawable.default_holder);
                                                        hideInput(WriteCircleActivity.this,text);
                                                        Toast.makeText(WriteCircleActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().post(new CircleRefreshEvent());//发送刷新事件
                                                    }else{
                                                        Toast.makeText(WriteCircleActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else{
                                            Log.e("获取到用户为空","aaa");
                                        }
                                    }else{
                                        Log.e("更新用户信息出错",e.toString());
                                    }
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "再想想", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    private String getTimeStr(long timestamp) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(timestamp);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String filePath = getPath(this, data.getData());
            File f = new File(filePath);
            if (f.exists()) {
                Log.e("文件", "存在");
            } else {
                Log.e("文件", "不存在");
            }
            Toast.makeText(this, "选择成功", Toast.LENGTH_SHORT).show();
            if(!filePath.endsWith("gif")&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                try {
                    BitmapUtil.encode(
                            Environment.getExternalStorageDirectory() + Storage.cacheImagePath,filePath,
                            new BitmapUtil.Callback() {
                                @Override
                                public void forDone() {//图片压缩成功
                                    try {
                                        final AVFile file=AVFile.withAbsoluteLocalPath("upload.jpg",Environment.getExternalStorageDirectory()+Storage.cacheImagePath);
                                        file.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(AVException e) {//图片上传结果
                                                if(e==null){
                                                    if(requestCode==CODE_SELECT_IMAGE1) {
                                                        imageUrls.set(0,file);
                                                        delete1.setVisibility(View.VISIBLE);
                                                        ImageLoader.getInstance().loadImage(image1,file.getUrl());
                                                    }else if(requestCode==CODE_SELECT_IMAGE2){
                                                        imageUrls.set(1,file);
                                                        delete2.setVisibility(View.VISIBLE);
                                                        ImageLoader.getInstance().loadImage(image2,file.getUrl());
                                                    }else if(requestCode==CODE_SELECT_IMAGE3){
                                                        imageUrls.set(2,file);
                                                        delete3.setVisibility(View.VISIBLE);
                                                        ImageLoader.getInstance().loadImage(image3,file.getUrl());
                                                    }
                                                    Toast.makeText(WriteCircleActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(WriteCircleActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    final AVFile file=AVFile.withAbsoluteLocalPath(filePath.substring(
                            filePath.lastIndexOf("/")+1,filePath.length()
                    ),filePath);
                    file.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {//图片上传结果
                            if(e==null){
                                if(requestCode==CODE_SELECT_IMAGE1) {
                                    imageUrls.set(0,file);
                                    delete1.setVisibility(View.VISIBLE);
                                    ImageLoader.getInstance().loadImage(image1,file.getUrl());
                                }else if(requestCode==CODE_SELECT_IMAGE2){
                                    imageUrls.set(1,file);
                                    delete2.setVisibility(View.VISIBLE);
                                    ImageLoader.getInstance().loadImage(image2,file.getUrl());
                                }else if(requestCode==CODE_SELECT_IMAGE3){
                                    imageUrls.set(2,file);
                                    delete3.setVisibility(View.VISIBLE);
                                    ImageLoader.getInstance().loadImage(image3,file.getUrl());
                                }
                                Toast.makeText(WriteCircleActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(WriteCircleActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "操作取消", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未知原因失败", Toast.LENGTH_SHORT).show();
        }
    }
}
