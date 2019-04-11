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

import com.su.wx.R;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.Holder> implements View.OnClickListener {

    private Context context;
    private List<WxUser> users;

    @Override
    public void onClick(View v) {
        int i= (int) v.getTag();
        if(onUserSelecteListener!=null){
            onUserSelecteListener.select(i,users.get(i));
        }
    }

    public interface OnUserSelecteListener{
        void select(int position,WxUser user);
    }

    private OnUserSelecteListener onUserSelecteListener;

    public void setOnUserSelecteListener(OnUserSelecteListener onUserSelecteListener) {
        this.onUserSelecteListener = onUserSelecteListener;
    }

    public UserListAdapter(Context context, List<WxUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_user,viewGroup,false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int i) {
        ImageView avatar=holder.itemView.findViewById(R.id.avatar);
        TextView username=holder.itemView.findViewById(R.id.username);
        if(users.get(i).getAvatar()!=null) {
            ImageLoader.getInstance().loadImage(avatar,users.get(i).getAvatar());
        }
        username.setText(users.get(i).getUsername());
        holder.itemView.setTag(i);
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class Holder extends RecyclerView.ViewHolder{

        View itemView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
        }
    }
}
