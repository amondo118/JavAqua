package com.example.javaqua.myaqua;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.javaqua.data.repository.AquariumRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MyAquariumViewModel extends AndroidViewModel {
    private final AquariumRepository aquariumRepository;

    public MyAquariumViewModel(@NonNull Application application) {
        super(application);
        this.aquariumRepository = new AquariumRepository(application);
    }

    public LiveData<List<AquariumAdapter.AquariumGridItem>> getAquariumList() {
        return aquariumRepository.getAquariumList();
    }

    public void loadAquariums() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            aquariumRepository.loadAquariums(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }
}
