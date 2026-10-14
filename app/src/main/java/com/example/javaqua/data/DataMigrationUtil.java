package com.example.javaqua.data;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.HashMap;
import java.util.Map;

public class DataMigrationUtil {
    private static final String TAG = "DataMigration";

    public static void startMigration() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Starting additive migration...");
        migrateAtlasPreviews(db);
        migrateAquariums(db);
    }

    private static void migrateAtlasPreviews(FirebaseFirestore db) {
        db.collection("atlas_previews").get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            final int[] count = {0};

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Map<String, Object> updates = new HashMap<>();
                
                Object sizeObj = doc.get("minTankSize");
                if (sizeObj != null && !doc.contains("minVolume")) {
                    updates.put("minVolume", parseDouble(String.valueOf(sizeObj)));
                }

                Object tempObj = doc.get("temperature");
                if (tempObj != null && (!doc.contains("minTemp") || !doc.contains("maxTemp"))) {
                    double[] range = parseTempRange(String.valueOf(tempObj));
                    updates.put("minTemp", range[0]);
                    updates.put("maxTemp", range[1]);
                }

                if (!updates.isEmpty()) {
                    batch.update(doc.getReference(), updates);
                    count[0]++;
                }
            }

            if (count[0] > 0) {
                batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Atlas migration successful."));
            } else {
                Log.d(TAG, "Atlas migration: No updates needed.");
            }
        });
    }

    private static void migrateAquariums(FirebaseFirestore db) {
        db.collection("aquariums").get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            int count = 0;
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Map<String, Object> updates = new HashMap<>();
                if (doc.contains("volumeLiters") && !doc.contains("volume")) {
                    updates.put("volume", doc.get("volumeLiters"));
                }
                if (!updates.isEmpty()) {
                    batch.update(doc.getReference(), updates);
                    count++;
                }
            }
            if (count > 0) {
                batch.commit().addOnSuccessListener(aVoid -> Log.d(TAG, "Aquarium migration successful."));
            }
        });
    }

    private static double parseDouble(String str) {
        try {
            return Double.parseDouble(str.replaceAll("[^0-9.]", ""));
        } catch (Exception e) { return 0.0; }
    }

    private static double[] parseTempRange(String str) {
        double min = 0, max = 0;
        try {
            String clean = str.replaceAll("[^0-9\\-]", "");
            String[] parts = clean.split("-");
            if (parts.length == 2) {
                min = Double.parseDouble(parts[0].trim());
                max = Double.parseDouble(parts[1].trim());
            } else if (parts.length == 1) {
                min = Double.parseDouble(parts[0].trim());
                max = min;
            }
        } catch (Exception e) {}
        return new double[]{min, max};
    }
}