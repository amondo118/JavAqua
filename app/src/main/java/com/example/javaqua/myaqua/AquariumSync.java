package com.example.javaqua.myaqua;

import android.content.Context;
import android.util.Log;

import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.data.AppDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class AquariumSync {
    private static final String TAG = "AquariumSync";

    public static void syncAquariums(Context context, String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AppDatabase appDatabase = AppDatabase.getDatabase(context);
        AquariumDao aquariumDao = appDatabase.aquariumDao();
        Atlas atlas = new Atlas(context.getApplicationContext());

        Log.d(TAG, "Starting sync for user: " + userID);

        db.collection("aquariums")
                .whereEqualTo("ownerID", userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            List<RoomAquarium> fetchedRoomAquariums = new ArrayList<>();
                            List<FishPreview> allFishPreviews = atlas.getCachedFishPreviewsList();
                            Map<String, FishPreview> fishPreviewMap = new HashMap<>();
                            if (allFishPreviews != null) {
                                for (FishPreview preview : allFishPreviews) {
                                    fishPreviewMap.put(preview.getId(), preview);
                                }
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String name = document.getString("name");
                                String backgroundUrl = document.getString("background");
                                double volumeLiters = document.contains("volumeLiters") ? document.getDouble("volumeLiters") : 0.0;
                                double temperature = document.contains("temperature") ? document.getDouble("temperature") : 0.0;

                                List<String> fishIDs = (List<String>) document.get("fishes");
                                List<FishPreview> currentAquariumEmbeddedFish = new ArrayList<>();
                                if (fishIDs != null) {
                                    for (String fishId : fishIDs) {
                                        FishPreview preview = fishPreviewMap.get(fishId);
                                        if (preview != null) {
                                            currentAquariumEmbeddedFish.add(preview);
                                        }
                                    }
                                }

                                fetchedRoomAquariums.add(new RoomAquarium(
                                        id, userID, name, volumeLiters, temperature, fishIDs, backgroundUrl, currentAquariumEmbeddedFish
                                ));
                            }

                            aquariumDao.deleteAquariumsForOwner(userID);
                            aquariumDao.insertAll(fetchedRoomAquariums);
                            Log.d(TAG, "Sync complete. Saved " + fetchedRoomAquariums.size() + " aquariums to Room.");
                        });
                    } else {
                        Log.e(TAG, "Failed to fetch aquariums from Firestore for sync.", task.getException());
                    }
                });
    }
}
