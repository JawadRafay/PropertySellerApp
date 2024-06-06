package com.hypenet.realestaterehman.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class Chat implements Parcelable{

    @ColumnInfo
    String role;
    @ColumnInfo
    String content;
    @ColumnInfo
    long time;

    public Chat(String role, String content) {
        this.role = role;
        this.content = content;
        this.time = System.currentTimeMillis();
    }

    protected Chat(Parcel in) {
        role = in.readString();
        content = in.readString();
        time = in.readLong();
    }

    public static final Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(role);
        dest.writeString(content);
        dest.writeLong(time);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
