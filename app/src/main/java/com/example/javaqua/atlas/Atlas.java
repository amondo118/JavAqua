package com.example.javaqua.atlas;

import android.content.Context;
import android.util.Log;

import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.data.local.dao.FishPreviewDao;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Atlas {

    private static final String TAG = "AtlasUtil";
    private final FirebaseFirestore db;
    private FishPreviewDao fishPreviewDao; // Room DAO

    private static boolean isLoadingPreviews = false;

    public Atlas(Context context) {
        db = FirebaseFirestore.getInstance();
        fishPreviewDao = AppDatabase.getDatabase(context).fishPreviewDao();
    }

    /**
     * Syncs fish previews from Firestore to Room. Called only on startup in MainActivity.
     */
    public void refreshFishPreviewsFromFirestoreAndSaveToRoom() {
        if (isLoadingPreviews) {
            Log.d(TAG, "Refresh already in progress.");
            return;
        }

        isLoadingPreviews = true;
        Log.d(TAG, "Starting to refresh fish previews from Firestore and save to Room...");

        db.collection("atlas_previews")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<RoomFishPreview> roomFishPreviews = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot) {
                            FishPreview preview = document.toObject(FishPreview.class);
                            if (preview != null) {
                                preview.setId(document.getId());
                                roomFishPreviews.add(new RoomFishPreview(
                                        preview.getId(),
                                        preview.getName(),
                                        preview.getLatin(),
                                        preview.getImageUrl(),
                                        preview.getImageSML(),
                                        preview.getCompatibility(),
                                        preview.getMinTankSize(),
                                        preview.getTemperature(),
                                        preview.getPh(),
                                        preview.getHardness(),
                                        preview.getTemperament(),
                                        preview.getDiet(),
                                        preview.getCareLevel(),
                                        preview.getDescription(),
                                        preview.getSprite()
                                ));
                            }
                        }
                        Executors.newSingleThreadExecutor().execute(() -> {
                            fishPreviewDao.insertAll(roomFishPreviews);
                            Log.d(TAG, "Successfully refreshed " + roomFishPreviews.size() + " fish previews and saved to Room.");
                        });

                    } else {
                        Log.e(TAG, "Failed to refresh fish previews from Firestore.", task.getException());
                    }
                    isLoadingPreviews = false;
                });
    }

    public List<FishPreview> getCachedFishPreviewsList() {
        List<RoomFishPreview> roomPreviews = fishPreviewDao.getAllFishPreviewsAlphabetical();
        List<FishPreview> fishPreviews = new ArrayList<>();
        for (RoomFishPreview roomPreview : roomPreviews) {
            fishPreviews.add(new FishPreview(
                    roomPreview.getId(),
                    roomPreview.getName(),
                    roomPreview.getLatin(),
                    roomPreview.getImageUrl(),
                    roomPreview.getImageSML(),
                    roomPreview.getCompatibility(),
                    roomPreview.getMinTankSize(),
                    roomPreview.getTemperature(),
                    roomPreview.getPh(),
                    roomPreview.getHardness(),
                    roomPreview.getTemperament(),
                    roomPreview.getDiet(),
                    roomPreview.getCareLevel(),
                    roomPreview.getDescription(),
                    roomPreview.getSprite()
            ));
        }
        return fishPreviews;
    }

    public FishPreview getCachedFishPreviewById(String fishId) {
        RoomFishPreview roomPreview = fishPreviewDao.getFishPreviewById(fishId);
        if (roomPreview != null) {
            return new FishPreview(
                    roomPreview.getId(),
                    roomPreview.getName(),
                    roomPreview.getLatin(),
                    roomPreview.getImageUrl(),
                    roomPreview.getImageSML(),
                    roomPreview.getCompatibility(),
                    roomPreview.getMinTankSize(),
                    roomPreview.getTemperature(),
                    roomPreview.getPh(),
                    roomPreview.getHardness(),
                    roomPreview.getTemperament(),
                    roomPreview.getDiet(),
                    roomPreview.getCareLevel(),
                    roomPreview.getDescription(),
                    roomPreview.getSprite()
            );
        }
        return null;
    }

    public List<FishPreview> searchCachedFishPreviews(String searchTerm) {
        List<RoomFishPreview> roomPreviews = fishPreviewDao.searchFishPreviews(searchTerm);
        List<FishPreview> fishPreviews = new ArrayList<>();
        for (RoomFishPreview roomPreview : roomPreviews) {
            fishPreviews.add(new FishPreview(
                    roomPreview.getId(),
                    roomPreview.getName(),
                    roomPreview.getLatin(),
                    roomPreview.getImageUrl(),
                    roomPreview.getImageSML(),
                    roomPreview.getCompatibility(),
                    roomPreview.getMinTankSize(),
                    roomPreview.getTemperature(),
                    roomPreview.getPh(),
                    roomPreview.getHardness(),
                    roomPreview.getTemperament(),
                    roomPreview.getDiet(),
                    roomPreview.getCareLevel(),
                    roomPreview.getDescription(),
                    roomPreview.getSprite()
            ));
        }
        return fishPreviews;
    }

    public Task<DocumentSnapshot> getFishDetailsFirestore(String fishId) {
        return db.collection("atlas")
                .document(fishId)
                .get();
    }
}
