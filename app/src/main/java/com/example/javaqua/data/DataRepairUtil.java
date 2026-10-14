package com.example.javaqua.data;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataRepairUtil {
    private static final String TAG = "DataRepair";

    public interface RepairCallback {
        void onDone();
    }

    private static final Map<String, String> ID_MAP = new HashMap<String, String>() {{
        put("2afe82d4-a357-4076-b46a-5c231871d2f1", "guppy");
        put("3c28f187-bdb5-4c14-8dcc-a21b05f3f05d", "neon_tetra");
        put("19849187-4ba1-430e-a7bc-efce4d1b4f4d", "betta_splendens");
        put("37cef20d-7ca6-4f4c-8078-73f9d31ccb84", "angelfish");
        put("122d43fb-8182-45e1-a529-7b3e32f3a959", "corydoras");
        put("31f8d4e5-4d49-490a-8605-a8fc1ae161b8", "cherry_barb");
    }};

    public static void startRepair(RepairCallback callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            if (callback != null) callback.onDone();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Repairing for UID: " + uid);

        db.collection("aquariums").whereEqualTo("ownerID", uid).get().addOnSuccessListener(docs -> {
            WriteBatch batch = db.batch();
            int docCount = 0;

            for (DocumentSnapshot doc : docs) {
                Object fishesObj = doc.get("fishes");
                boolean changed = false;
                Map<String, Integer> newMap = new HashMap<>();

                if (fishesObj instanceof List) {
                    changed = true;
                    for (Object item : (List<?>) fishesObj) {
                        String id = String.valueOf(item);
                        String mappedId = ID_MAP.getOrDefault(id, id);
                        newMap.put(mappedId, newMap.getOrDefault(mappedId, 0) + 1);
                    }
                } else if (fishesObj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) fishesObj;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        String id = String.valueOf(entry.getKey());
                        int val = entry.getValue() instanceof Number ? ((Number) entry.getValue()).intValue() : 1;
                        if (ID_MAP.containsKey(id)) {
                            String mappedId = ID_MAP.get(id);
                            newMap.put(mappedId, newMap.getOrDefault(mappedId, 0) + val);
                            changed = true;
                        } else {
                            newMap.put(id, val);
                        }
                    }
                }

                if (changed) {
                    batch.update(doc.getReference(), "fishes", newMap);
                    docCount++;
                }
            }

            if (docCount > 0) {
                batch.commit().addOnCompleteListener(task -> {
                    if (callback != null) callback.onDone();
                });
            } else {
                if (callback != null) callback.onDone();
            }
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onDone();
        });
    }
}