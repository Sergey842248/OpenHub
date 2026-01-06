package com.thirtydegreesray.openhub.ui.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.R2;
import com.thirtydegreesray.openhub.common.GlideApp;
import com.thirtydegreesray.openhub.mvp.model.Discussion;
import com.thirtydegreesray.openhub.ui.activity.ProfileActivity;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseAdapter;
import com.thirtydegreesray.openhub.ui.adapter.base.BaseViewHolder;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;
import com.thirtydegreesray.openhub.util.PrefUtils;
import com.thirtydegreesray.openhub.util.StringUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Adapter for displaying discussions in a list
 */

public class DiscussionsAdapter extends BaseAdapter<DiscussionsAdapter.ViewHolder, Discussion> {

    @Inject
    public DiscussionsAdapter(Context context, BaseFragment fragment) {
        super(context, fragment);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.layout_item_discussion;
    }

    @Override
    protected ViewHolder getViewHolder(View itemView, int viewType) {
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Discussion model = data.get(position);
        GlideApp.with(fragment)
                .load(model.getAuthor().getAvatarUrl())
                .onlyRetrieveFromCache(!PrefUtils.isLoadImageEnable())
                .into(holder.userAvatar);
        holder.userName.setText(model.getAuthor().getLogin());
        holder.discussionTitle.setText(model.getTitle());
        holder.commentNum.setText(String.valueOf(model.getCommentsCount()));
        holder.time.setText(StringUtils.getNewsTimeStr(context, model.getCreatedAt()));
        holder.discussionNumber.setText(("#").concat(String.valueOf(model.getNumber())));
        if (model.getCategory() != null) {
            holder.categoryName.setText(model.getCategory().getName());
            holder.categoryEmoji.setText(model.getCategory().getEmoji());
        }
    }

    class ViewHolder extends BaseViewHolder {

        @BindView(R2.id.user_avatar) ImageView userAvatar;
        @BindView(R2.id.user_name) TextView userName;
        @BindView(R2.id.time) TextView time;
        @BindView(R2.id.discussion_title) TextView discussionTitle;
        @BindView(R2.id.comment_num) TextView commentNum;
        @BindView(R2.id.discussion_number) TextView discussionNumber;
        @BindView(R2.id.category_name) TextView categoryName;
        @BindView(R2.id.category_emoji) TextView categoryEmoji;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @OnClick({R2.id.user_avatar, R2.id.user_name})
        public void onUserClick(){
            if(getAdapterPosition() != RecyclerView.NO_POSITION) {
                Discussion discussion = data.get(getAdapterPosition());
                ProfileActivity.show((Activity) context, userAvatar, discussion.getAuthor().getLogin(),
                        discussion.getAuthor().getAvatarUrl());
            }
        }

    }

}
