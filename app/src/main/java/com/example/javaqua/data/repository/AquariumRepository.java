package com.example.javaqua.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.myaqua.AquariumAdapter;
import com.example.javaqua.myaqua.AquariumDao;
import com.example.javaqua.myaqua.RoomAquarium;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class AquariumRepository {
    private static final String TAG = "AquariumRepository";
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
        isSyncing.setValue(true);
        db.collection("aquariums")
                .whereEqualTo("ownerID", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            List<RoomAquarium> fetchedRoomAquariums = new ArrayList<>();
                            Atlas atlas = new Atlas(context);
                            List<FishPreview> allPreviews = atlas.getCachedFishPreviewsList();
                            Map<String, FishPreview> previewMap = new HashMap<>();
                            if (allPreviews != null) {
                                for (FishPreview p : allPreviews) previewMap.put(p.getId(), p);
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<String> fishIDs = (List<String>) document.get("fishes");
                                List<FishPreview> embeddedFish = new ArrayList<>();
                                if (fishIDs != null) {
                                    for (String fid : fishIDs) {
                                        if (previewMap.containsKey(fid)) embeddedFish.add(previewMap.get(fid));
                                    }
                                }

                                fetchedRoomAquariums.add(new RoomAquarium(
                                        document.getId(), userId, document.getString("name"),
                                        document.contains("volumeLiters") ? document.getDouble("volumeLiters") : 0.0,
                                        document.contains("temperature") ? document.getDouble("temperature") : 0.0,
                                        fishIDs, document.getString("background"), embeddedFish
                                ));
                            }

                            aquariumDao.deleteAquariumsForOwner(userId);
                            aquariumDao.insertAll(fetchedRoomAquariums);
                            loadAquariums(userId);
                            isSyncing.postValue(false);
                        });
                    } else {
                        isSyncing.setValue(false);
                    }
                });
    }

    public void loadAquariums(String ownerId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RoomAquarium> roomAquariums = aquariumDao.getAquariumsForOwner(ownerId);
            List<AquariumAdapter.AquariumGridItem> loadedAquariums = new ArrayList<>();
            Atlas atlas = new Atlas(context);

            for (RoomAquarium ra : roomAquariums) {
                List<FishPreview> fish = new ArrayList<>();
                if (ra.getEmbeddedFishPreviews() != null && !ra.getEmbeddedFishPreviews().isEmpty()) {
                    fish.addAll(ra.getEmbeddedFishPreviews());
                } else if (ra.getFishIDs() != null) {
                    for (String id : ra.getFishIDs()) {
                        FishPreview p = atlas.getCachedFishPreviewById(id);
                        if (p != null) fish.add(p);
                    }
                }
                loadedAquariums.add(new AquariumAdapter.AquariumGridItem(ra.getId(), ra.getName(), ra.getBackground(), fish));
            }
            aquariumList.postValue(loadedAquariums);
        });
    }
}
