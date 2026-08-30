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
import java.util.concurrent.Executors;

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
                        List<RoomFishPreview> roomFishPreviews = new ArrayList<>();
                        task.getResult().forEach(document -> {
                            FishPreview preview = document.toObject(FishPreview.class);
                            if (preview != null) {
                                preview.setId(document.getId());
                                roomFishPreviews.add(mapToRoom(preview));
                            }
                        });
                        Executors.newSingleThreadExecutor().execute(() -> {
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
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RoomFishPreview> roomPreviews = fishPreviewDao.getAllFishPreviewsAlphabetical();
            List<FishPreview> previews = new ArrayList<>();
            roomPreviews.forEach(rp -> previews.add(mapFromRoom(rp)));
            fishPreviews.postValue(previews);
        });
    }

    public void searchPreviews(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RoomFishPreview> roomPreviews = fishPreviewDao.searchFishPreviews(query);
            List<FishPreview> previews = new ArrayList<>();
            roomPreviews.forEach(rp -> previews.add(mapFromRoom(rp)));
            fishPreviews.postValue(previews);
        });
    }

    private RoomFishPreview mapToRoom(FishPreview p) {
        return new RoomFishPreview(p.getId(), p.getName(), p.getLatin(), p.getImageUrl(), p.getImageSML(),
                p.getCompatibility(), p.getMinTankSize(), p.getTemperature(), p.getPh(), p.getHardness(),
                p.getTemperament(), p.getDiet(), p.getCareLevel(), p.getDescription(), p.getSprite());
    }

    private FishPreview mapFromRoom(RoomFishPreview rp) {
        return new FishPreview(rp.getId(), rp.getName(), rp.getLatin(), rp.getImageUrl(), rp.getImageSML(),
                rp.getCompatibility(), rp.getMinTankSize(), rp.getTemperature(), rp.getPh(), rp.getHardness(),
                rp.getTemperament(), rp.getDiet(), rp.getCareLevel(), rp.getDescription(), rp.getSprite());
    }
}
