package com.su.wx.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.jelly.mango.Mango;
import com.jelly.mango.MultiplexImage;
import com.su.wx.R;
import com.su.wx.models.Circle;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CircleAdapter extends RecyclerView.Adapter<CircleAdapter.Holder> implements View.OnClickListener {

    Context context;
    List<Circle> circles;

    public CircleAdapter(Context context, List<Circle> circles) {
        this.context = context;
        this.circles = circles;
    }

    private final int type_only_text=0;
    private final int type_one_image=1;
    private final int type_two_image=2;
    private final int type_three_image=3;

    @Override
    public int getItemViewType(int position) {
        String content= circles.get(position).getContent();
        int type=0;
        try {
            JSONObject jsonObject=new JSONObject(content);
            if(!jsonObject.has("image1")&&!jsonObject.has("image2")&&!jsonObject.has("image3")){
                type=type_only_text;
            }else if(jsonObject.has("image1")&&!jsonObject.has("image2")&&!jsonObject.has("image3")){
                type=type_one_image;
            }else if(jsonObject.has("image1")&&jsonObject.has("image2")&&!jsonObject.has("image3")){
                type=type_two_image;
            }else if(jsonObject.has("image1")&&jsonObject.has("image2")&&jsonObject.has("image3")){
                type=type_three_image;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return type;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==type_only_text){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_circle_only_text,viewGroup,false));
        }else if(i==type_one_image){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_circle_one_image,viewGroup,false));
        }else if(i==type_two_image){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_circle_two_image,viewGroup,false));
        }else if(i==type_three_image){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_circle_three_image,viewGroup,false));
        }
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_circle_only_text,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        try {
            String content = circles.get(i).getContent();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final ImageView head = holder.itemView.findViewById(R.id.head);
            head.setTag("head");
            head.setOnClickListener(this);
            WxUser from= (WxUser) circles.get(i).get("from");
            from.fetchInBackground(new GetCallback<AVObject>() {
                @Override
                public void done(AVObject u, AVException e) {
                    if(e==null){
                        if(u!=null){
                            String av= (String) u.get("avatar");
                            if(av!=null&&!av.equals("")){
                                ImageLoader.getInstance().loadImage(head,av);
                            }
                        }
                    }else{
                        Log.e("查询用户失败",e.toString());
                    }
                }
            });
            TextView username = holder.itemView.findViewById(R.id.username);
            username.setTag("head");
            username.setOnClickListener(this);
            if (jsonObject.has("username")) {
                try {
                    username.setText(jsonObject.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            TextView text = holder.itemView.findViewById(R.id.text);
            if (jsonObject.has("text")) {
                try {
                    text.setText(jsonObject.getString("text"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            TextView time = holder.itemView.findViewById(R.id.time);
            if (jsonObject.has("time")) {
                try {
                    time.setText(jsonObject.getString("time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (getItemViewType(i) == type_one_image) {
                ImageView image1 = holder.itemView.findViewById(R.id.image1);
                image1.setTag("image:"+i+":0");
                image1.setOnClickListener(this);
                if (jsonObject.has("image1")) {
                    try {
                        String i1 = jsonObject.getString("image1").replace("\\/", "/");
                        ImageLoader.getInstance().loadImage(image1, i1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (getItemViewType(i) == type_two_image) {
                ImageView image1 = holder.itemView.findViewById(R.id.image1);
                image1.setTag("image:"+i+":0");
                image1.setOnClickListener(this);
                ImageView image2 = holder.itemView.findViewById(R.id.image2);
                image2.setTag("image:"+i+":1");
                image2.setOnClickListener(this);
                if (jsonObject.has("image1")) {
                    try {
                        String i1 = jsonObject.getString("image1").replace("\\/", "/");
                        ImageLoader.getInstance().loadImage(image1, i1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (jsonObject.has("image2")) {
                    try {
                        String i2 = jsonObject.getString("image2").replace("\\/", "/");
                        ImageLoader.getInstance().loadImage(image2, i2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (getItemViewType(i) == type_three_image) {
                ImageView image1 = holder.itemView.findViewById(R.id.image1);
                image1.setTag("image:"+i+":0");
                image1.setOnClickListener(this);
                ImageView image2 = holder.itemView.findViewById(R.id.image2);
                image2.setTag("image:"+i+":1");
                image2.setOnClickListener(this);
                ImageView image3 = holder.itemView.findViewById(R.id.image3);
                image3.setTag("image:"+i+":2");
                image3.setOnClickListener(this);
                if (jsonObject.has("image1")) {
                    try {
                        String i1 = jsonObject.getString("image1").replace("\\/", "/");
                        ImageLoader.getInstance().loadImage(image1, i1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (jsonObject.has("image2")) {
                    try {
                        String i2 = jsonObject.getString("image2").replace("\\/", "/");
                        ImageLoader.getInstance().loadImage(image2, i2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (jsonObject.has("image3")) {
                    try {
                        String i3 = jsonObject.getString("image3").replace("\\/", "/");
                        ImageLoader.getInstance().loadImage(image3, i3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return circles.size();
    }

    @Override
    public void onClick(View v) {
        String tag= (String) v.getTag();
        if(tag.startsWith("image:")){
            String[] ts=tag.split(":");
            String pos=ts[1];
            List<String> imageUrls=new ArrayList<>();
            String content= circles.get(Integer.valueOf(pos)).getContent();
            JSONObject jsonObject=null;
            try {
                jsonObject=new JSONObject(content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(jsonObject.has("image1")){
                try {
                    String i1=jsonObject.getString("image1").replace("\\/","/");
                    imageUrls.add(i1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonObject.has("image2")){
                try {
                    String i2=jsonObject.getString("image2").replace("\\/","/");
                    imageUrls.add(i2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonObject.has("image3")){
                try {
                    String i3=jsonObject.getString("image3").replace("\\/","/");
                    imageUrls.add(i3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            List<MultiplexImage> list=new ArrayList<>();
            for(String s:imageUrls){
                if(s.endsWith("gif")) {
                    list.add(new MultiplexImage(null, s, MultiplexImage.ImageType.GIF));
                }else{
                    list.add(new MultiplexImage(null, s, MultiplexImage.ImageType.NORMAL));
                }
            }
            Mango.setPosition(Integer.valueOf(ts[2]));
            Mango.setImages(list);
            try {
                Mango.open(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(tag.startsWith("head")){
            //点击头像，进入用户详情页

        }
    }

    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
