package com.su.wx.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.su.wx.R;
import com.su.wx.models.WxUser;
import com.su.wx.utils.DeviceUtil;
import com.su.wx.utils.StatusBarUtil;

import java.util.List;

public class VerifyDeviceIdActivity extends AppCompatActivity implements View.OnClickListener {

    TextView[] nums;
    Button[] btns;
    Button confirm,delete;
    private int index=0;
    private String[] cs=new String[]{"","","","","",""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_device_id);

        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("验证设备ID");
        }

        StatusBarUtil.setStatusTextColor(true,this);

        nums=new TextView[]{
                findViewById(R.id.num1),
                findViewById(R.id.num2),
                findViewById(R.id.num3),
                findViewById(R.id.num4),
                findViewById(R.id.num5),
                findViewById(R.id.num6),
        };

        btns=new Button[]{
                findViewById(R.id.btn0),
                findViewById(R.id.btn1),
                findViewById(R.id.btn2),
                findViewById(R.id.btn3),
                findViewById(R.id.btn4),
                findViewById(R.id.btn5),
                findViewById(R.id.btn6),
                findViewById(R.id.btn7),
                findViewById(R.id.btn8),
                findViewById(R.id.btn9),
        };

        for(Button btn:btns){
            btn.setOnClickListener(this);
        }

        confirm=findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(this);
        delete=findViewById(R.id.btn_delete);
        delete.setOnClickListener(this);

        change();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_confirm){//确定
            if(index>=6){
                String imei_md5=DeviceUtil.getMD5(DeviceUtil.getIMEI(this));
                String pwd_md5=DeviceUtil.getMD5(""+cs[0]+cs[1] +cs[2]+cs[3] +cs[4]+cs[5]);
                final String md5=DeviceUtil.getMD5(imei_md5+pwd_md5);
                AVQuery<WxUser> query=new AVQuery<>("WxUser");
                query.whereEqualTo("objectId",WxUser.getCurrentUser().getObjectId());
                query.findInBackground(new FindCallback<WxUser>() {
                    @Override
                    public void done(List<WxUser> users, AVException e) {
                        if(e==null){
                            String di=users.get(0).getDeviceId();
                            if(md5.equals(di)){//密码验证正确，进入设置页面
                                startActivity(new Intent(VerifyDeviceIdActivity.this,SetDeviceIdActivity.class));
                                finish();
                            }else{
                                Toast.makeText(VerifyDeviceIdActivity.this, "密码错误，验证失败", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(VerifyDeviceIdActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            Log.e("寻找用户","失败:"+e.toString());
                        }
                    }
                });
            }else{
                Toast.makeText(this, "请输入完整六位密码", Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId()==R.id.btn_delete){//删除
            if(index>0){
                index--;
                nums[index].setText("");
                cs[index]="";
                change();
            }
        }else{//数字按键
            if(index<6) {
                nums[index].setText("*");
                cs[index]=((TextView)v).getText().toString();
                index++;
                change();
            }
        }
    }

    private void change(){
        for(int i=0;i<30;i++){
            int pos= (int) (Math.random()*10);
            int tar=(int)(Math.random()*10);
            if(pos!=tar){
                String tem=btns[pos].getText().toString();
                btns[pos].setText(btns[tar].getText().toString());
                btns[tar].setText(tem);
            }
        }
    }
}
