package com.thirtydegreesray.openhub.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * GitHub Discussion model
 */

public class Discussion implements Parcelable {

    private String id;
    private int number;
    private String title;
    private String body;
    @SerializedName("body_html") private String bodyHtml;

    @SerializedName("created_at") private Date createdAt;
    @SerializedName("updated_at") private Date updatedAt;

    private User author;
    @SerializedName("html_url") private String htmlUrl;

    private DiscussionCategory category;

    @SerializedName("comments_count") private int commentsCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public DiscussionCategory getCategory() {
        return category;
    }

    public void setCategory(DiscussionCategory category) {
        this.category = category;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Discussion() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.number);
        dest.writeString(this.title);
        dest.writeString(this.body);
        dest.writeString(this.bodyHtml);
        dest.writeLong(this.createdAt != null ? this.createdAt.getTime() : -1);
        dest.writeLong(this.updatedAt != null ? this.updatedAt.getTime() : -1);
        dest.writeParcelable(this.author, flags);
        dest.writeString(this.htmlUrl);
        dest.writeParcelable(this.category, flags);
        dest.writeInt(this.commentsCount);
    }

    protected Discussion(Parcel in) {
        this.id = in.readString();
        this.number = in.readInt();
        this.title = in.readString();
        this.body = in.readString();
        this.bodyHtml = in.readString();
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.author = in.readParcelable(User.class.getClassLoader());
        this.htmlUrl = in.readString();
        this.category = in.readParcelable(DiscussionCategory.class.getClassLoader());
        this.commentsCount = in.readInt();
    }

    public static final Creator<Discussion> CREATOR = new Creator<Discussion>() {
        @Override
        public Discussion createFromParcel(Parcel source) {
            return new Discussion(source);
        }

        @Override
        public Discussion[] newArray(int size) {
            return new Discussion[size];
        }
    };
}
