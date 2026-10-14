package com.example.javaqua.atlas;

import android.content.Context;
import android.util.Log;

import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.data.local.dao.FishPreviewDao;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Atlas {

    private static final String TAG = "AtlasUtil";
    private final FirebaseFirestore db;
    private final FishPreviewDao fishPreviewDao;

    private static boolean isLoadingPreviews = false;

    public interface SyncCallback {
        void onSyncComplete(int count);
        void onSyncFailed(Exception e);
    }

    public Atlas(Context context) {
        db = FirebaseFirestore.getInstance();
        fishPreviewDao = AppDatabase.getDatabase(context).fishPreviewDao();
    }

    public void refreshFishPreviewsFromFirestoreAndSaveToRoom() {
        refreshFishPreviewsFromFirestoreAndSaveToRoom(null);
    }

    public void refreshFishPreviewsFromFirestoreAndSaveToRoom(SyncCallback callback) {
        if (isLoadingPreviews) {
            Log.d(TAG, "Sync already in progress.");
            return;
        }

        isLoadingPreviews = true;
        Log.d(TAG, "Starting refresh from Firestore: atlas_previews");

        db.collection("atlas_previews")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int count = task.getResult().size();
                        Log.d(TAG, "Firestore fetch success: " + count + " items.");
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                List<RoomFishPreview> roomFishPreviews = new ArrayList<>();
                                for (DocumentSnapshot document : task.getResult()) {
                                    FishPreview preview = document.toObject(FishPreview.class);
                                    if (preview != null) {
                                        preview.setId(document.getId());
                                        roomFishPreviews.add(mapToRoom(preview));
                                    }
                                }
                                if (!roomFishPreviews.isEmpty()) {
                                    fishPreviewDao.deleteAll(); // Force refresh
                                    fishPreviewDao.insertAll(roomFishPreviews);
                                    Log.d(TAG, "Room insertion complete. Count: " + roomFishPreviews.size());
                                    if (callback != null) callback.onSyncComplete(roomFishPreviews.size());
                                } else {
                                    Log.w(TAG, "No valid fish previews found.");
                                    if (callback != null) callback.onSyncComplete(0);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving to Room", e);
                                if (callback != null) callback.onSyncFailed(e);
                            } finally {
                                isLoadingPreviews = false;
                            }
                        });
                    } else {
                        Log.e(TAG, "Firestore fetch failed.", task.getException());
                        isLoadingPreviews = false;
                        if (callback != null) callback.onSyncFailed(task.getException());
                    }
                });
    }

    public List<FishPreview> getCachedFishPreviewsList() {
        try {
            List<RoomFishPreview> roomPreviews = fishPreviewDao.getAllFishPreviewsAlphabetical();
            List<FishPreview> fishPreviews = new ArrayList<>();
            if (roomPreviews != null) {
                for (RoomFishPreview rp : roomPreviews) fishPreviews.add(mapFromRoom(rp));
            }
            Log.d(TAG, "getCachedFishPreviewsList: returning " + fishPreviews.size() + " items.");
            return fishPreviews;
        } catch (Exception e) {
            Log.e(TAG, "Error reading from Room", e);
            return new ArrayList<>();
        }
    }

    public FishPreview getCachedFishPreviewById(String fishId) {
        try {
            RoomFishPreview rp = fishPreviewDao.getFishPreviewById(fishId);
            return rp != null ? mapFromRoom(rp) : null;
        } catch (Exception e) {
            Log.e(TAG, "Error reading fish " + fishId, e);
            return null;
        }
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
        rp.setMinSchoolSize(p.getMinSchoolSize());
        rp.setBioload(p.getBioload());
        rp.setAggression(p.getAggression());
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
        p.setMinSchoolSize(rp.getMinSchoolSize());
        p.setBioload(rp.getBioload());
        p.setAggression(rp.getAggression());
        p.setSprite(rp.getSprite());
        return p;
    }

    public Task<DocumentSnapshot> getFishDetailsFirestore(String fishId) {
        return db.collection("atlas").document(fishId).get();
    }
}