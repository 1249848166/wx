package com.su.wx.adapters;

import android.annotation.SuppressLint;
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
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationMemberCountCallback;
import com.su.wx.R;
import com.su.wx.event.ConversationAdapterEvent;
import com.su.wx.models.WxUser;
import com.su.wx.utils.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.Holder> implements View.OnClickListener {

    private Context context;
    private List<AVIMConversation> conversations;

    @Override
    public void onClick(View v) {
        int index = (int) v.getTag();
        conversations.get(index).read();//标记为已读
        this.notifyItemChanged(index);
        if (onConverationSelectListener != null) {
            onConverationSelectListener.select(index, conversations.get(index));
        }
    }

    public interface OnConverationSelectListener {
        void select(int index, AVIMConversation conversation);
    }

    private OnConverationSelectListener onConverationSelectListener;

    public void setOnConverationSelectListener(OnConverationSelectListener onConverationSelectListener) {
        this.onConverationSelectListener = onConverationSelectListener;
    }

    public ConversationAdapter(Context context, List<AVIMConversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        EventBus.getDefault().unregister(this);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_conversation, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int i) {
        final ImageView avatar = holder.itemView.findViewById(R.id.avatar);
        final TextView title = holder.itemView.findViewById(R.id.title);
        final TextView content = holder.itemView.findViewById(R.id.content);
        final TextView time = holder.itemView.findViewById(R.id.time);
        final TextView dot = holder.itemView.findViewById(R.id.dot);
        final ImageView alert = holder.itemView.findViewById(R.id.alert);
        conversations.get(i).getMemberCount(new AVIMConversationMemberCountCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(Integer memberCount, AVIMException e) {
                if (e == null) {
                    if (memberCount == 2) {//单聊
                        conversations.get(i).fetchInfoInBackground(new AVIMConversationCallback() {
                            @Override
                            public void done(AVIMException e) {
                                if(e!=null){
                                    Log.e("更新对话信息失败",e.toString());
                                }
                            }
                        });
                        AVIMMessage lastMeg = conversations.get(i).getLastMessage();
                        String json = lastMeg.getContent();
                        int t;
                        String c;
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            t = (int) jsonObject.get("_lctype");
                            if (t == AVIMMessageType.TEXT_MESSAGE_TYPE) {
                                c = (String) jsonObject.get("_lctext");
                                content.setText(c);//设置会话最后一条内容
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        time.setText(conversations.get(i).getUpdatedAt().toString());
                        int uc = conversations.get(i).getUnreadMessagesCount();
                        if (uc == 0) {
                            dot.setVisibility(View.GONE);
                        } else if (0 < uc && uc <= 99) {
                            dot.setVisibility(View.VISIBLE);
                            dot.setText(uc + "");
                        } else {
                            dot.setVisibility(View.VISIBLE);
                            dot.setText("99+");
                        }
                        String username = null;
                        List<String> list = conversations.get(i).getMembers();
                        for (String id : list) {
                            if (!id.equals(WxUser.getCurrentUser().getUsername())) {
                                username = id;
                                break;
                            }
                        }
                        AVQuery<WxUser> query = new AVQuery<>("WxUser");
                        query.whereEqualTo("username", username);
                        final String finalUsername = username;
                        query.findInBackground(new FindCallback<WxUser>() {
                            @SuppressLint("CheckResult")
                            @Override
                            public void done(List<WxUser> users, AVException e1) {
                                if (e1 == null && users.size() > 0) {
                                    String t = users.get(0).getNickname();
                                    String tt = finalUsername + ((t == null || t.equals("")) ? "" : ("(" + t + ")"));
                                    title.setText(tt);//设置会话标题
                                    String av = users.get(0).getAvatar();
                                    if (av != null) {
                                        ImageLoader.getInstance().loadImage(avatar, av);
                                    }
                                } else {
                                    Log.e("查询用户失败", e1.toString());
                                }
                            }
                        });
                        boolean importance = (boolean) conversations.get(i).getAttribute("importance");
                        if (importance) {
                            alert.setVisibility(View.GONE);
                        } else {
                            alert.setVisibility(View.VISIBLE);
                        }
                    } else {//群聊

                    }
                }
            }
        });
        holder.itemView.setTag(i);
        holder.itemView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        View itemView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConversationAdapterEvent event) {
        String cid = event.getCid();
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getConversationId().equals(cid)) {
                Log.e("当前对话未读消息数:",conversations.get(i).getUnreadMessagesCount()+"条");
                this.notifyItemChanged(i);
                break;
            }
        }
    }
}
