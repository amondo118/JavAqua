package com.example.javaqua.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.javaqua.atlas.RoomFishPreview;

import java.util.List;

@Dao
public interface FishPreviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RoomFishPreview> fishPreviews);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RoomFishPreview fishPreview);

    @Update
    void update(RoomFishPreview fishPreview);

    @Query("SELECT * FROM fish_previews ORDER BY name ASC")
    List<RoomFishPreview> getAllFishPreviewsAlphabetical();

    @Query("SELECT * FROM fish_previews WHERE id = :fishId")
    RoomFishPreview getFishPreviewById(String fishId);

    @Query("SELECT * FROM fish_previews WHERE name LIKE '%' || :searchTerm || '%' OR latin LIKE '%' || :searchTerm || '%' ORDER BY name ASC")
    List<RoomFishPreview> searchFishPreviews(String searchTerm);

    @Query("DELETE FROM fish_previews")
    void deleteAll();
}
