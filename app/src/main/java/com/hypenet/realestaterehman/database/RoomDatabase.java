package com.hypenet.realestaterehman.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.TypeConverters;

import com.hypenet.realestaterehman.database.convertors.HouseConvertor;
import com.hypenet.realestaterehman.model.House;


@Database(entities = {House.class}, version = 1)
@TypeConverters({HouseConvertor.class})
public abstract class RoomDatabase extends androidx.room.RoomDatabase {

    public static final String DATABASE_NAME = "home_room";
    private static RoomDatabase instance = null;

    public static synchronized RoomDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),RoomDatabase.class,DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract HouseDao houseDao();
}
