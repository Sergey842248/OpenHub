package com.thirtydegreesray.openhub.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * GitHub Discussion Category model
 */

public class DiscussionCategory implements Parcelable {

    private String id;
    private String name;
    private String description;
    private String emoji;
    @SerializedName("is_answerable") private boolean isAnswerable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public boolean isAnswerable() {
        return isAnswerable;
    }

    public void setAnswerable(boolean answerable) {
        isAnswerable = answerable;
    }

    public DiscussionCategory() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.emoji);
        dest.writeByte(this.isAnswerable ? (byte) 1 : (byte) 0);
    }

    protected DiscussionCategory(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.emoji = in.readString();
        this.isAnswerable = in.readByte() != 0;
    }

    public static final Creator<DiscussionCategory> CREATOR = new Creator<DiscussionCategory>() {
        @Override
        public DiscussionCategory createFromParcel(Parcel source) {
            return new DiscussionCategory(source);
        }

        @Override
        public DiscussionCategory[] newArray(int size) {
            return new DiscussionCategory[size];
        }
    };
}
