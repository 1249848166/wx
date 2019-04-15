package com.su.wx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMFileMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage;
import com.avos.avoscloud.im.v2.messages.AVIMRecalledMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avos.avoscloud.im.v2.messages.AVIMVideoMessage;
import com.jelly.mango.Mango;
import com.jelly.mango.MultiplexImage;
import com.su.wx.R;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> implements View.OnClickListener {

    private Context context;
    private List<AVIMMessage> messages;

    private String myAvatar,hisAvatar;
    private String myName,hisName;

    public ChatAdapter(Context context, List<AVIMMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    private final int type_me_AVIMTextMessage=1;
    private final int type_me_AVIMImageMessage=2;
    private final int type_me_AVIMAudioMessage=3;
    private final int type_me_AVIMVideoMessage=4;
    private final int type_me_AVIMLocationMessage=5;
    private final int type_me_AVIMFileMessage=6;
    private final int type_me_AVIMRecalledMessage=7;
    private final int type_other_AVIMTextMessage=11;
    private final int type_other_AVIMImageMessage=12;
    private final int type_other_AVIMAudioMessage=13;
    private final int type_other_AVIMVideoMessage=14;
    private final int type_other_AVIMLocationMessage=15;
    private final int type_other_AVIMFileMessage=16;
    private final int type_other_AVIMRecalledMessage=17;

    @Override
    public int getItemViewType(int position) {
        AVIMMessage message=messages.get(position);
        String from=message.getFrom();
        String myUsername=WxUser.getCurrentUser().getUsername();
        if(from.equals(myUsername)){
            if(message instanceof AVIMImageMessage){
                return type_me_AVIMImageMessage;
            }else if(message instanceof AVIMAudioMessage){
                return type_me_AVIMAudioMessage;
            }else if(message instanceof AVIMVideoMessage){
                return type_me_AVIMVideoMessage;
            }else if(message instanceof AVIMLocationMessage){
                return type_me_AVIMLocationMessage;
            }else if(message instanceof AVIMFileMessage){
                return type_me_AVIMFileMessage;
            }else if(message instanceof AVIMRecalledMessage){
                return type_me_AVIMRecalledMessage;
            }else if(message instanceof AVIMTextMessage){
                return type_me_AVIMTextMessage;
            }
        }else{
            if(message instanceof AVIMImageMessage){
                return type_other_AVIMImageMessage;
            }else if(message instanceof AVIMAudioMessage){
                return type_other_AVIMAudioMessage;
            }else if(message instanceof AVIMVideoMessage){
                return type_other_AVIMVideoMessage;
            }else if(message instanceof AVIMLocationMessage){
                return type_other_AVIMLocationMessage;
            }else if(message instanceof AVIMFileMessage){
                return type_other_AVIMFileMessage;
            }else if(message instanceof AVIMRecalledMessage){
                return type_other_AVIMRecalledMessage;
            }else if(message instanceof AVIMTextMessage){
                return type_other_AVIMTextMessage;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==type_me_AVIMTextMessage){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_me_text,viewGroup,false));
        }else if(i==type_other_AVIMTextMessage){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_other_text,viewGroup,false));
        }else if(i==type_me_AVIMImageMessage){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_me_image,viewGroup,false));
        }else if(i==type_other_AVIMImageMessage){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_other_image,viewGroup,false));
        }else if(i==type_me_AVIMAudioMessage){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_me_audio,viewGroup,false));
        }else if(i==type_other_AVIMAudioMessage){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_other_audio,viewGroup,false));
        }else if(i==type_me_AVIMVideoMessage){

        }else if(i==type_other_AVIMVideoMessage){

        }else if(i==type_me_AVIMLocationMessage){

        }else if(i==type_other_AVIMLocationMessage){

        }else if(i==type_me_AVIMFileMessage){

        }else if(i==type_other_AVIMFileMessage){

        }else if(i==type_me_AVIMRecalledMessage){

        }else if(i==type_other_AVIMRecalledMessage){

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        final ImageView avatar=holder.itemView.findViewById(R.id.avatar);
        final TextView username=holder.itemView.findViewById(R.id.username);
        final String usn=messages.get(i).getFrom();
        if(getItemViewType(i)<10) {
            if (myName == null) {
                AVQuery<WxUser> query = new AVQuery<>("WxUser");
                query.whereEqualTo("username", usn);
                query.findInBackground(new FindCallback<WxUser>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void done(List<WxUser> us, AVException e) {
                        if (e == null && us.size() > 0) {
                            if (us.get(0).getNickname() != null) {
                                username.setText(usn + "(" + us.get(0).getNickname() + ")");
                                myName = usn + "(" + us.get(0).getNickname() + ")";
                            }else{
                                username.setText(usn);
                                myName = usn;
                            }
                        } else {
                            username.setText(usn);
                            myName = usn;
                        }
                    }
                });
            } else {
                username.setText(myName);
            }
            if(myAvatar==null){
                AVQuery<WxUser> query=new AVQuery<>("WxUser");
                query.whereEqualTo("username",usn);
                query.findInBackground(new FindCallback<WxUser>() {
                    @Override
                    public void done(List<WxUser> users, AVException e) {
                        if(e==null&&users.size()>0){
                            String av=users.get(0).getAvatar();
                            if(av!=null){
                                ImageLoader.getInstance().loadImage(avatar,av);
                                myAvatar=av;
                            }
                        }
                    }
                });
            }else{
                ImageLoader.getInstance().loadImage(avatar,myAvatar);
            }
        }else{
            if(hisName==null) {
                AVQuery<WxUser> query = new AVQuery<>("WxUser");
                query.whereEqualTo("username", usn);
                query.findInBackground(new FindCallback<WxUser>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void done(List<WxUser> us, AVException e) {
                        if (e == null && us.size() > 0) {
                            if (us.get(0).getNickname() != null) {
                                username.setText(usn + "(" + us.get(0).getNickname() + ")");
                                hisName = usn + "(" + us.get(0).getNickname() + ")";
                            }else{
                                username.setText(usn);
                                hisName = usn;
                            }
                        } else {
                            username.setText(usn);
                            hisName = usn;
                        }
                    }
                });
            }else{
                username.setText(hisName);
            }
            if(hisAvatar==null){
                AVQuery<WxUser> query=new AVQuery<>("WxUser");
                query.whereEqualTo("username",usn);
                query.findInBackground(new FindCallback<WxUser>() {
                    @Override
                    public void done(List<WxUser> users, AVException e) {
                        if(e==null&&users.size()>0){
                            String av=users.get(0).getAvatar();
                            if(av!=null){
                                ImageLoader.getInstance().loadImage(avatar,av);
                                hisAvatar=av;
                            }
                        }
                    }
                });
            }else{
                ImageLoader.getInstance().loadImage(avatar,hisAvatar);
            }
        }
        if(getItemViewType(i)==type_me_AVIMTextMessage||getItemViewType(i)==type_other_AVIMTextMessage){
            String ms=messages.get(i).getContent();
            String c = null;
            try {
                JSONObject jsonObject=new JSONObject(ms);
                c= (String) jsonObject.get("_lctext");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TextView msg=holder.itemView.findViewById(R.id.msg);
            msg.setText(c);
        }else if(getItemViewType(i)==type_me_AVIMImageMessage||getItemViewType(i)==type_other_AVIMImageMessage){
            String content=messages.get(i).getContent();
            String tag = null;
            String url=null;
            //TextView view_tag=holder.itemView.findViewById(R.id.tag);
            ImageView view_image=holder.itemView.findViewById(R.id.image);
            view_image.setImageResource(R.drawable.default_holder);
            try{
                JSONObject jsonObject=new JSONObject(content);
                tag=jsonObject.getString("_lctext");
                JSONObject file=jsonObject.getJSONObject("_lcfile");
                url=file.getString("url");
            }catch (Exception e){
                e.printStackTrace();
            }
            if(tag!=null) {
                //view_tag.setText("");
            }
            if(url!=null){
                ImageLoader.getInstance().loadImage(view_image,url);
                view_image.setTag("chat_image;"+url);
                view_image.setOnClickListener(this);
            }
        }else if(getItemViewType(i)==type_me_AVIMAudioMessage||getItemViewType(i)==type_other_AVIMAudioMessage){

        }else if(getItemViewType(i)==type_me_AVIMVideoMessage||getItemViewType(i)==type_other_AVIMVideoMessage){

        }else if(getItemViewType(i)==type_me_AVIMLocationMessage||getItemViewType(i)==type_other_AVIMLocationMessage){

        }else if(getItemViewType(i)==type_me_AVIMFileMessage||getItemViewType(i)==type_other_AVIMFileMessage){

        }else if(getItemViewType(i)==type_me_AVIMRecalledMessage||getItemViewType(i)==type_other_AVIMRecalledMessage){

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onClick(View v) {
        String[] tag = ((String) v.getTag()).split(";");
        switch (tag[0]){
            case "chat_image":
                previewImg(tag[1]);
                break;
        }
    }

    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        Holder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }

    private void previewImg(String path) {
        try {
            MultiplexImage image=null;
            if(path.endsWith(".gif")){
                image=new MultiplexImage(null,path,MultiplexImage.ImageType.GIF);
            }else{
                image=new MultiplexImage(null,path,MultiplexImage.ImageType.NORMAL);
            }
            List<MultiplexImage> images=new ArrayList<>();
            images.add(image);
            Mango.setImages(images);
            Mango.setPosition(0);
            Mango.open(context);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
