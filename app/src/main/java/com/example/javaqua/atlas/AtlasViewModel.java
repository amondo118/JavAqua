package com.example.javaqua.atlas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.data.repository.AtlasRepository;
import com.example.javaqua.model.Aquarium;
import com.example.javaqua.myaqua.AquariumAdapter;

import java.util.ArrayList;
import java.util.List;

public class AtlasViewModel extends AndroidViewModel {
    private final AtlasRepository atlasRepository;
    private final MutableLiveData<List<FishPreview>> processedFish = new MutableLiveData<>();
    private AquariumAdapter.AquariumGridItem selectedAquarium = null;

    public AtlasViewModel(@NonNull Application application) {
        super(application);
        this.atlasRepository = new AtlasRepository(application);
        
        // Proxy the repository data through our suggestion logic
        atlasRepository.getFishPreviews().observeForever(previews -> {
            applySuggestions(previews);
        });
    }

    public LiveData<List<FishPreview>> getFishPreviews() {
        return processedFish;
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

    public void setSelectedAquarium(AquariumAdapter.AquariumGridItem aquarium) {
        this.selectedAquarium = aquarium;
        if (atlasRepository.getFishPreviews().getValue() != null) {
            applySuggestions(atlasRepository.getFishPreviews().getValue());
        }
    }

    private void applySuggestions(List<FishPreview> source) {
        if (source == null) {
            processedFish.postValue(null);
            return;
        }
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<FishPreview> list = new ArrayList<>();
            for (FishPreview f : source) {
                // Deep copy or at least shallow copy to avoid modifying original objects if needed,
                // but since these are previews, modifying the transient rating is fine.
                // However, to trigger LiveData correctly with DiffUtil, we should have fresh objects.
                FishPreview copy = copyFish(f);
                if (selectedAquarium == null) {
                    copy.setSuggestionRating(SuggestionEngine.Rating.UNKNOWN);
                } else {
                    Aquarium model = new Aquarium();
                    model.setVolumeLiters(selectedAquarium.getVolumeLiters());
                    model.setTemperature(selectedAquarium.getTemperature());
                    copy.setSuggestionRating(SuggestionEngine.calculateRating(copy, model));
                }
                list.add(copy);
            }
            processedFish.postValue(list);
        });
    }

    private FishPreview copyFish(FishPreview p) {
        FishPreview n = new FishPreview();
        n.setId(p.getId());
        n.setName(p.getName());
        n.setLatin(p.getLatin());
        n.setImageUrl(p.getImageUrl());
        n.setImageSML(p.getImageSML());
        n.setDescription(p.getDescription());
        n.setShortDescription(p.getShortDescription());
        n.setDifficulty(p.getDifficulty());
        n.setMinVolume(p.getMinVolume());
        n.setMinTemp(p.getMinTemp());
        n.setMaxTemp(p.getMaxTemp());
        n.setSprite(p.getSprite());
        n.setExpanded(p.isExpanded());
        return n;
    }
}