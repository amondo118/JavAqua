package com.example.javaqua.atlas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaqua.data.repository.AtlasRepository;

import java.util.List;

public class AtlasViewModel extends AndroidViewModel {
    private final AtlasRepository atlasRepository;

    public AtlasViewModel(@NonNull Application application) {
        super(application);
        this.atlasRepository = new AtlasRepository(application);
    }

    public LiveData<List<FishPreview>> getFishPreviews() {
        return atlasRepository.getFishPreviews();
    }

    public LiveData<Boolean> getIsSyncing() {
        return atlasRepository.getIsSyncing();
    }

    public void loadPreviews() {
        atlasRepository.loadCachedPreviews();
    }

    public void searchPreviews(String query) {
        if (query == null || query.trim().isEmpty()) {
            atlasRepository.loadCachedPreviews();
        } else {
            atlasRepository.searchPreviews(query);
        }
    }
}
