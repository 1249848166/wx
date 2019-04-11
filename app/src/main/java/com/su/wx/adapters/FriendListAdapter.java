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
import com.su.wx.R;
import com.su.wx.models.Friend;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.Holder> implements View.OnClickListener {

    Context context;
    List<Friend> friends;

    @Override
    public void onClick(View v) {
        int i= (int) v.getTag();
        if(onFriendSelecteListener!=null){
            onFriendSelecteListener.select(i,friends.get(i));
        }
    }

    public interface OnFriendSelecteListener{
        void select(int position, Friend friend);
    }

    private OnFriendSelecteListener onFriendSelecteListener;

    public void setOnFriendSelecteListener(OnFriendSelecteListener onFriendSelecteListener) {
        this.onFriendSelecteListener = onFriendSelecteListener;
    }

    public FriendListAdapter(Context context, List<Friend> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_user,viewGroup,false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int i) {
        final ImageView avatar=holder.itemView.findViewById(R.id.avatar);
        final TextView username=holder.itemView.findViewById(R.id.username);
        WxUser friendUser= (WxUser) friends.get(i).get("friendUser");
        if(friendUser!=null){
            String objectId=friendUser.getObjectId();
            AVQuery<WxUser> query=new AVQuery<>("WxUser");
            query.whereEqualTo("objectId",objectId);
            query.findInBackground(new FindCallback<WxUser>() {
                @Override
                public void done(List<WxUser> users, AVException e) {
                    if(e==null&&users.size()>0){
                        String usn=users.get(0).getUsername();
                        String av=users.get(0).getAvatar();
                        if(usn!=null){
                            username.setText(usn);
                        }
                        if(av!=null){
                            ImageLoader.getInstance().loadImage(avatar,av);
                        }
                    }
                }
            });
        }
        holder.itemView.setTag(i);
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }


    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
