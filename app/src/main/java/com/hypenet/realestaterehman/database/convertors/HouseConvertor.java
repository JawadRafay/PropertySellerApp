package com.hypenet.realestaterehman.database.convertors;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypenet.realestaterehman.model.City;
import com.hypenet.realestaterehman.model.GalleryModel;
import com.hypenet.realestaterehman.model.User;

import java.util.List;

public class HouseConvertor {

    @TypeConverter
    public static String toString(List<GalleryModel> ratings){
        return new Gson().toJson(ratings);
    }

    @TypeConverter
    public static List<GalleryModel> fromString(String rating){
        return new Gson().fromJson(rating, new TypeToken<List<GalleryModel>>(){}.getType());
    }

    @TypeConverter
    public static String toCityJson(City city){
        return new Gson().toJson(city);
    }

    @TypeConverter
    public static City fromCityJson(String city){
        return new Gson().fromJson(city, City.class);
    }

    @TypeConverter
    public static String toSellerJson(User user){
        return new Gson().toJson(user);
    }

    @TypeConverter
    public static User fromSellerJson(String user){
        return new Gson().fromJson(user, User.class);
    }
}
