package com.example.javaqua.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.javaqua.atlas.RoomFishPreview;
import com.example.javaqua.data.local.dao.FishPreviewDao;
import com.example.javaqua.myaqua.AquariumDao;
import com.example.javaqua.myaqua.RoomAquarium;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {RoomAquarium.class, RoomFishPreview.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AquariumDao aquariumDao();
    public abstract FishPreviewDao fishPreviewDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}