package com.example.javaqua.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.atlas.RoomFishPreview;
import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.data.local.dao.FishPreviewDao;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AtlasRepository {
    private final FirebaseFirestore db;
    private final FishPreviewDao fishPreviewDao;
    private final MutableLiveData<List<FishPreview>> fishPreviews = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public AtlasRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.fishPreviewDao = AppDatabase.getDatabase(context).fishPreviewDao();
    }

    public LiveData<List<FishPreview>> getFishPreviews() {
        return fishPreviews;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public void refreshFishPreviews() {
        isSyncing.setValue(true);
        db.collection("atlas_previews")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<RoomFishPreview> roomFishPreviews = new ArrayList<>();
                            task.getResult().forEach(document -> {
                                FishPreview preview = document.toObject(FishPreview.class);
                                if (preview != null) {
                                    preview.setId(document.getId());
                                    roomFishPreviews.add(mapToRoom(preview));
                                }
                            });
                            fishPreviewDao.insertAll(roomFishPreviews);
                            loadCachedPreviews();
                            isSyncing.postValue(false);
                        });
                    } else {
                        isSyncing.setValue(false);
                    }
                });
    }

    public void loadCachedPreviews() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<RoomFishPreview> roomPreviews = fishPreviewDao.getAllFishPreviewsAlphabetical();
            List<FishPreview> previews = new ArrayList<>();
            roomPreviews.forEach(rp -> previews.add(mapFromRoom(rp)));
            fishPreviews.postValue(previews);
        });
    }

    public void searchPreviews(String query) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<RoomFishPreview> roomPreviews = fishPreviewDao.searchFishPreviews(query);
            List<FishPreview> previews = new ArrayList<>();
            roomPreviews.forEach(rp -> previews.add(mapFromRoom(rp)));
            fishPreviews.postValue(previews);
        });
    }

    private RoomFishPreview mapToRoom(FishPreview p) {
        RoomFishPreview rp = new RoomFishPreview();
        rp.setId(p.getId());
        rp.setName(p.getName());
        rp.setLatin(p.getLatin());
        rp.setImageUrl(p.getImageUrl());
        rp.setImageSML(p.getImageSML());
        rp.setDescription(p.getDescription());
        rp.setShortDescription(p.getShortDescription());
        rp.setDifficulty(p.getDifficulty());
        rp.setMinVolume(p.getMinVolume());
        rp.setMinTemp(p.getMinTemp());
        rp.setMaxTemp(p.getMaxTemp());
        rp.setSprite(p.getSprite());
        return rp;
    }

    private FishPreview mapFromRoom(RoomFishPreview rp) {
        FishPreview p = new FishPreview();
        p.setId(rp.getId());
        p.setName(rp.getName());
        p.setLatin(rp.getLatin());
        p.setImageUrl(rp.getImageUrl());
        p.setImageSML(rp.getImageSML());
        p.setDescription(rp.getDescription());
        p.setShortDescription(rp.getShortDescription());
        p.setDifficulty(rp.getDifficulty());
        p.setMinVolume(rp.getMinVolume());
        p.setMinTemp(rp.getMinTemp());
        p.setMaxTemp(rp.getMaxTemp());
        p.setSprite(rp.getSprite());
        return p;
    }
}