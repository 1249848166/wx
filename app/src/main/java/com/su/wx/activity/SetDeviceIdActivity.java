package com.su.wx.activity;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.su.wx.R;
import com.su.wx.models.WxUser;
import com.su.wx.utils.DeviceUtil;

public class SetDeviceIdActivity extends AppCompatActivity implements View.OnClickListener {

    TextView[] nums;
    Button[] btns;
    Button confirm,delete;
    private int index=0;
    private String[] cs=new String[]{"","","","","",""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_device_id);

        ActionBar actionBar=getActionBar();
        if(actionBar!=null) {
            actionBar.setTitle("设置唯一设备ID");
        }

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
                String md5=DeviceUtil.getMD5(imei_md5+pwd_md5);
                AVObject user = AVObject.createWithoutData("WxUser", WxUser.getCurrentUser().getObjectId());
                Log.e("id",WxUser.getCurrentUser().getObjectId());
                user.put("deviceId",md5);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e==null){
                            Toast.makeText(SetDeviceIdActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Log.e("deviceId设置失败",e.toString());
                            Toast.makeText(SetDeviceIdActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
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
