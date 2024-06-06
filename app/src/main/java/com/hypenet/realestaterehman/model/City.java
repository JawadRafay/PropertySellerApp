package com.hypenet.realestaterehman.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class City implements Parcelable {

    int id;
    String name;
    String country_id;

    public City() {
    }

    protected City(Parcel in) {
        id = in.readInt();
        name = in.readString();
        country_id = in.readString();
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry_id() {
        return country_id;
    }

    public void setCountry_id(String country_id) {
        this.country_id = country_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(country_id);
    }
}
