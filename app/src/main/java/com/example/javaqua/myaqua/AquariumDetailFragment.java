package com.example.javaqua.myaqua;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishAdapterForDetailView;
import com.example.javaqua.atlas.FishDetailFragment;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.model.Aquarium;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class AquariumDetailFragment extends Fragment implements FishAdapterForDetailView.OnFishItemClickListener {

    private static final String TAG = "AquariumDetailFragment";
    private static final String ARG_AQUARIUM_ID = "aquarium_id";

    private String aquariumId;
    private FirebaseFirestore db;
    private Aquarium currentAquarium;

    private TextView textViewAquariumName;
    private AnimatedAquariumView animatedAquariumView;
    private TextView textViewAquariumVolume;
    private TextView textViewAquariumTemperature;
    private RecyclerView recyclerViewFishInAquarium;
    private FishAdapterForDetailView fishAdapter;
    private List<FishPreview> fishPreviewList = new ArrayList<>();
    private Atlas atlas;

    public AquariumDetailFragment() {
    }

    public static AquariumDetailFragment newInstance(String aquariumId) {
        AquariumDetailFragment fragment = new AquariumDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AQUARIUM_ID, aquariumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            aquariumId = getArguments().getString(ARG_AQUARIUM_ID);
        }
        db = FirebaseFirestore.getInstance();
        if (getContext() != null) {
            atlas = new Atlas(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aquarium_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewAquariumName = view.findViewById(R.id.textViewAquariumName);
        animatedAquariumView = view.findViewById(R.id.animatedAquariumView);
        textViewAquariumVolume = view.findViewById(R.id.textViewAquariumVolume);
        textViewAquariumTemperature = view.findViewById(R.id.textViewAquariumTemperature);
        recyclerViewFishInAquarium = view.findViewById(R.id.recyclerViewFishInAquarium);

        setupFishRecyclerView();

        if (aquariumId != null) {
            loadAquariumDetails();
        } else {
            Log.e(TAG, "Aquarium ID is null.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Aquarium ID not provided.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupFishRecyclerView() {
        recyclerViewFishInAquarium.setLayoutManager(new LinearLayoutManager(getContext()));
        fishAdapter = new FishAdapterForDetailView(getContext(), fishPreviewList, this);
        recyclerViewFishInAquarium.setAdapter(fishAdapter);
        recyclerViewFishInAquarium.setNestedScrollingEnabled(false);
    }

    private void loadAquariumDetails() {
        if (aquariumId == null) {
            Log.e(TAG, "loadAquariumDetails: Aquarium ID is null.");
            if (animatedAquariumView != null) {
                 animatedAquariumView.setAquariumData(null, new ArrayList<>());
            }
            return;
        }
        Log.d(TAG, "Loading details for aquarium ID: " + aquariumId);

        db.collection("aquariums").document(aquariumId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentAquarium = document.toObject(Aquarium.class);
                            if (currentAquarium != null) {
                                currentAquarium.setId(document.getId());
                                populateUI(currentAquarium);

                                List<String> fishIDs = currentAquarium.getFishIDs();
                                if (fishIDs != null && !fishIDs.isEmpty() && atlas != null) {
                                    // Move database access to background thread
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        List<FishPreview> allPreloadedFish = atlas.getCachedFishPreviewsList();
                                        
                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(() -> {
                                                if (allPreloadedFish != null && !allPreloadedFish.isEmpty()) {
                                                    Map<String, FishPreview> fishPreviewMap = new HashMap<>();
                                                    for (FishPreview preview : allPreloadedFish) {
                                                        if (preview != null && preview.getId() != null) {
                                                            fishPreviewMap.put(preview.getId(), preview);
                                                        }
                                                    }

                                                    fishPreviewList.clear();
                                                    for (String fishId : fishIDs) {
                                                        if (fishId != null) {
                                                            FishPreview preview = fishPreviewMap.get(fishId);
                                                            if (preview != null) {
                                                                fishPreviewList.add(preview);
                                                            }
                                                        }
                                                    }
                                                    
                                                    fishAdapter.updateData(new ArrayList<>(fishPreviewList));
                                                    if (animatedAquariumView != null) {
                                                        animatedAquariumView.setAquariumData(currentAquarium.getBackground(), new ArrayList<>(fishPreviewList));
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    fishPreviewList.clear();
                                    fishAdapter.updateData(new ArrayList<>());
                                    if (animatedAquariumView != null) {
                                        animatedAquariumView.setAquariumData(currentAquarium.getBackground(), new ArrayList<>());
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void populateUI(Aquarium aquarium) {
        if (aquarium == null || getView() == null) return;
        textViewAquariumName.setText(aquarium.getName() != null ? aquarium.getName() : "N/A");
        textViewAquariumVolume.setText(String.format(Locale.getDefault(), "%.1f L", aquarium.getVolumeLiters()));
        textViewAquariumTemperature.setText(String.format(Locale.getDefault(), "%.1f °C", aquarium.getTemperature()));
    }

    @Override
    public void onFishItemClick(FishPreview fishPreview, int position) {
        if (fishPreview == null || fishPreview.getId() == null || getContext() == null || !isAdded()) return;
        String fishId = fishPreview.getId();
        FishDetailFragment fishDetailFragment = FishDetailFragment.newInstance(fishId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView5, fishDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}
