package com.example.javaqua.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.javaqua.atlas.SuggestionEngine;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.myaqua.AquariumAdapter;
import com.example.javaqua.myaqua.AquariumDao;
import com.example.javaqua.myaqua.RoomAquarium;
import com.example.javaqua.model.Aquarium;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AquariumRepository {
    private static final String TAG = "AquariumRepo";
    private final AquariumDao aquariumDao;
    private final FirebaseFirestore db;
    private final Context context;
    private final MutableLiveData<List<AquariumAdapter.AquariumGridItem>> aquariumList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);

    public AquariumRepository(Context context) {
        this.context = context.getApplicationContext();
        this.aquariumDao = AppDatabase.getDatabase(context).aquariumDao();
        this.db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<AquariumAdapter.AquariumGridItem>> getAquariumList() {
        return aquariumList;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public void syncAquariums(String userId) {
        isSyncing.postValue(true);
        Log.d(TAG, "Starting sync for user: " + userId);
        db.collection("aquariums")
                .whereEqualTo("ownerID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "Fetched " + task.getResult().size() + " aquariums from Firestore.");
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                List<RoomAquarium> fetchedRoomAquariums = new ArrayList<>();
                                Atlas atlas = new Atlas(context);
                                List<FishPreview> allPreviews = atlas.getCachedFishPreviewsList();
                                Map<String, FishPreview> previewMap = new HashMap<>();
                                for (FishPreview p : allPreviews) previewMap.put(p.getId(), p);

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map<String, Integer> fishCounts = new HashMap<>();
                                    Object fishesObj = document.get("fishes");
                                    
                                    if (fishesObj instanceof Map) {
                                        Map<?, ?> rawMap = (Map<?, ?>) fishesObj;
                                        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                            if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                                                fishCounts.put((String) entry.getKey(), ((Number) entry.getValue()).intValue());
                                            }
                                        }
                                    } else if (fishesObj instanceof List) {
                                        for (Object item : (List<?>) fishesObj) {
                                            if (item instanceof String) {
                                                String fid = (String) item;
                                                fishCounts.put(fid, fishCounts.getOrDefault(fid, 0) + 1);
                                            }
                                        }
                                    }

                                    List<FishPreview> embeddedFish = new ArrayList<>();
                                    for (String fid : fishCounts.keySet()) {
                                        if (previewMap.containsKey(fid)) {
                                            embeddedFish.add(previewMap.get(fid));
                                        } else {
                                            Log.w(TAG, "Fish " + fid + " not in cache during sync.");
                                        }
                                    }

                                    fetchedRoomAquariums.add(new RoomAquarium(
                                            document.getId(), userId, document.getString("name"),
                                            document.contains("volume") ? document.getDouble("volume") : 0.0,
                                            document.contains("temperature") ? document.getDouble("temperature") : 0.0,
                                            fishCounts, document.getString("background"), embeddedFish
                                    ));
                                }

                                aquariumDao.deleteAquariumsForOwner(userId);
                                aquariumDao.insertAll(fetchedRoomAquariums);
                                Log.d(TAG, "Local DB updated with fetched aquariums.");
                                loadAquariums(userId);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in sync transaction", e);
                            } finally {
                                isSyncing.postValue(false);
                            }
                        });
                    } else {
                        Log.e(TAG, "Sync failed", task.getException());
                        isSyncing.postValue(false);
                    }
                });
    }

    public void loadAquariums(String ownerId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<RoomAquarium> roomAquariums = aquariumDao.getAquariumsForOwner(ownerId);
                List<AquariumAdapter.AquariumGridItem> loadedAquariums = new ArrayList<>();
                Atlas atlas = new Atlas(context);

                Log.d(TAG, "Loading " + roomAquariums.size() + " aquariums from local DB.");
                for (RoomAquarium ra : roomAquariums) {
                    List<FishPreview> fishListForSprites = new ArrayList<>();
                    Map<String, Integer> counts = ra.getFishCounts();
                    
                    if (counts != null) {
                        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                            FishPreview p = atlas.getCachedFishPreviewById(entry.getKey());
                            if (p != null) {
                                for (int i = 0; i < entry.getValue(); i++) fishListForSprites.add(p);
                            } else {
                                Log.w(TAG, "Fish " + entry.getKey() + " not found in cache for aquarium " + ra.getName());
                            }
                        }
                    }

                    SuggestionEngine.Rating status = SuggestionEngine.Rating.GREEN;
                    Aquarium model = new Aquarium();
                    model.setVolumeLiters(ra.getVolumeLiters());
                    model.setTemperature(ra.getTemperature());

                    if (counts != null) {
                        for (String fid : counts.keySet()) {
                            FishPreview p = atlas.getCachedFishPreviewById(fid);
                            if (p != null) {
                                SuggestionEngine.Rating r = SuggestionEngine.calculateRating(p, model);
                                if (r == SuggestionEngine.Rating.RED) status = SuggestionEngine.Rating.RED;
                                else if (r == SuggestionEngine.Rating.YELLOW && status == SuggestionEngine.Rating.GREEN) status = SuggestionEngine.Rating.YELLOW;
                            }
                        }
                    }
                    
                    loadedAquariums.add(new AquariumAdapter.AquariumGridItem(ra.getId(), ra.getName(), ra.getBackground(), ra.getVolumeLiters(), ra.getTemperature(), fishListForSprites, status));
                }
                aquariumList.postValue(loadedAquariums);
                Log.d(TAG, "Aquarium list posted to UI.");
            } catch (Exception e) {
                Log.e(TAG, "Error loading aquariums", e);
            }
        });
    }
}