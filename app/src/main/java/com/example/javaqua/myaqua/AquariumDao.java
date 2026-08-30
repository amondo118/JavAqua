package com.example.javaqua.myaqua;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AquariumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RoomAquarium> aquariums);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomAquarium aquarium);

    @Update
    void update(RoomAquarium aquarium);

    @Query("SELECT * FROM aquariums WHERE ownerID = :ownerId")
    List<RoomAquarium> getAquariumsForOwner(String ownerId);

    @Query("SELECT * FROM aquariums WHERE id = :aquariumId")
    RoomAquarium getAquariumById(String aquariumId);

    @Query("DELETE FROM aquariums WHERE ownerID = :ownerId")
    void deleteAquariumsForOwner(String ownerId);

    @Query("DELETE FROM aquariums")
    void deleteAll();
}
