package com.hypenet.realestaterehman.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.hypenet.realestaterehman.model.House;

import java.util.List;

@Dao
public interface HouseDao {

    @Query("Select * FROM House")
    LiveData<List<House>> getHouses();

    @Insert
    void insert(House house);

    @Delete
    void delete(House house);

    @Query("Select * from House where id=:id")
    House checkFavourite(int id);
}
