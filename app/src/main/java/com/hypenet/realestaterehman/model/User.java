package com.hypenet.realestaterehman.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class User implements Parcelable {

    @PrimaryKey
    int id;
    @ColumnInfo
    String name;
    @ColumnInfo
    String email;
    @ColumnInfo
    String password;
    @ColumnInfo
    String address;
    @ColumnInfo
    String phone;
    @ColumnInfo
    String image;
    @ColumnInfo
    String city;
    @ColumnInfo
    String zip;
    @ColumnInfo
    String country;
    @ColumnInfo
    double latitude;
    @ColumnInfo
    double longitude;
    @Ignore
    int code;
    @Ignore
    String distance;
    @ColumnInfo
    double token;
    @ColumnInfo
    int unq_id;
    @ColumnInfo
    String kyc_status;
    @ColumnInfo
    String cnic;
    @ColumnInfo
    String cnic_front;
    @ColumnInfo
    String cnic_back;

    public User() {
    }

    public User(String email) {
        this.email = email;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public User(int id, String name, String email, String password, String image, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected User(Parcel in) {
        id = in.readInt();
        name = in.readString();
        email = in.readString();
        password = in.readString();
        address = in.readString();
        phone = in.readString();
        image = in.readString();
        city = in.readString();
        zip = in.readString();
        country = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        code = in.readInt();
        distance = in.readString();
        token = in.readDouble();
        unq_id = in.readInt();
        kyc_status = in.readString();
        cnic = in.readString();
        cnic_front = in.readString();
        cnic_back = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public String getKyc_status() {
        return kyc_status;
    }

    public void setKyc_status(String kyc_status) {
        this.kyc_status = kyc_status;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getToken() {
        return token;
    }

    public void setToken(double token) {
        this.token = token;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getUnq_id() {
        return unq_id;
    }

    public void setUnq_id(int unq_id) {
        this.unq_id = unq_id;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getCnic_front() {
        return cnic_front;
    }

    public void setCnic_front(String cnic_front) {
        this.cnic_front = cnic_front;
    }

    public String getCnic_back() {
        return cnic_back;
    }

    public void setCnic_back(String cnic_back) {
        this.cnic_back = cnic_back;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(image);
        dest.writeString(city);
        dest.writeString(zip);
        dest.writeString(country);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(code);
        dest.writeString(distance);
        dest.writeDouble(token);
        dest.writeInt(unq_id);
        dest.writeString(kyc_status);
        dest.writeString(cnic);
        dest.writeString(cnic_front);
        dest.writeString(cnic_back);
    }
}
