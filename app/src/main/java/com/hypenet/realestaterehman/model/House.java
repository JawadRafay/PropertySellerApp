package com.hypenet.realestaterehman.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.hypenet.realestaterehman.database.convertors.HouseConvertor;

import java.util.List;

@Entity
@TypeConverters({HouseConvertor.class})
public class House implements Parcelable {

    @PrimaryKey
    int id;
    @ColumnInfo
    String title;
    @ColumnInfo
    String description;
    @ColumnInfo
    String email;
    @ColumnInfo
    String phone;
    @ColumnInfo
    String owner_name;
    @ColumnInfo
    String latitude;
    @ColumnInfo
    String longitude;
    @ColumnInfo
    String area_name;
    @ColumnInfo
    String type;
    @ColumnInfo
    String price;
    @ColumnInfo
    String sell_type;
    @ColumnInfo
    String seller_id;
    @ColumnInfo
    City city;
    @ColumnInfo
    List<GalleryModel> property_images;
    @ColumnInfo
    User seller;

    public House() {
    }

    protected House(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        email = in.readString();
        phone = in.readString();
        owner_name = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        area_name = in.readString();
        type = in.readString();
        price = in.readString();
        sell_type = in.readString();
        seller_id = in.readString();
        city = in.readParcelable(City.class.getClassLoader());
        property_images = in.createTypedArrayList(GalleryModel.CREATOR);
        seller = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<House> CREATOR = new Creator<House>() {
        @Override
        public House createFromParcel(Parcel in) {
            return new House(in);
        }

        @Override
        public House[] newArray(int size) {
            return new House[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSell_type() {
        return sell_type;
    }

    public void setSell_type(String sell_type) {
        this.sell_type = sell_type;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public List<GalleryModel> getProperty_images() {
        return property_images;
    }

    public void setProperty_images(List<GalleryModel> property_images) {
        this.property_images = property_images;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(owner_name);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(area_name);
        dest.writeString(type);
        dest.writeString(price);
        dest.writeString(sell_type);
        dest.writeString(seller_id);
        dest.writeParcelable(city, flags);
        dest.writeTypedList(property_images);
        dest.writeParcelable(seller, flags);
    }
}
