package com.su.wx.adapters;

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
import com.su.wx.R;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> {

    private Context context;
    private List<AVIMMessage> messages;

    public ChatAdapter(Context context, List<AVIMMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    private final int type_me=0;
    private final int type_other=1;

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getFrom().equals(WxUser.getCurrentUser().getUsername())){
            return type_me;
        }else{
            return type_other;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==type_me){
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_me,viewGroup,false));
        }else{
            return new Holder(LayoutInflater.from(context).inflate(R.layout.item_chat_other,viewGroup,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        final ImageView avatar=holder.itemView.findViewById(R.id.avatar);
        TextView msg=holder.itemView.findViewById(R.id.msg);
        TextView username=holder.itemView.findViewById(R.id.username);
        String usn=messages.get(i).getFrom();
        if(usn!=null){
            username.setText(usn);
        }
        String ms=messages.get(i).getContent();
        int type;
        String c = null;
        try {
            JSONObject jsonObject=new JSONObject(ms);
            type= (int) jsonObject.get("_lctype");
            c= (String) jsonObject.get("_lctext");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        msg.setText(c);
        AVQuery<WxUser> query=new AVQuery<>("WxUser");
        query.whereEqualTo("username",messages.get(i).getFrom());
        query.findInBackground(new FindCallback<WxUser>() {
            @Override
            public void done(List<WxUser> users, AVException e) {
                if(e==null&&users.size()>0){
                    String av=users.get(0).getAvatar();
                    if(av!=null){
                        ImageLoader.getInstance().loadImage(avatar,av);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
