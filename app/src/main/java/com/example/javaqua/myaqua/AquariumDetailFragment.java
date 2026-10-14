package com.example.javaqua.myaqua;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.SuggestionEngine;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishAdapterForDetailView;
import com.example.javaqua.atlas.FishDetailFragment;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.model.Aquarium;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AquariumDetailFragment extends Fragment implements FishAdapterForDetailView.OnFishItemClickListener {

    private static final String TAG = "AquariumDetail";
    private static final String ARG_AQUARIUM_ID = "aquarium_id";

    private String aquariumId;
    private FirebaseFirestore db;
    private Aquarium currentAquarium;

    private TextView textViewAquariumName;
    private AnimatedAquariumView animatedAquariumView;
    private TextView textViewAquariumVolume, textViewAquariumTemperature;
    private RecyclerView recyclerViewFishInAquarium;
    private FishAdapterForDetailView fishAdapter;
    private final List<FishPreview> fishPreviewList = new ArrayList<>();
    private Atlas atlas;

    // Compatibility UI
    private MaterialButton buttonCompatibility;
    private MaterialCardView cardCompatibilityMessages;
    private LinearLayout layoutMessages;

    public AquariumDetailFragment() {}

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
        if (getArguments() != null) aquariumId = getArguments().getString(ARG_AQUARIUM_ID);
        db = FirebaseFirestore.getInstance();
        atlas = new Atlas(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        ImageButton buttonEdit = view.findViewById(R.id.buttonEditAquarium);

        buttonCompatibility = view.findViewById(R.id.buttonCompatibility);
        cardCompatibilityMessages = view.findViewById(R.id.cardCompatibilityMessages);
        layoutMessages = view.findViewById(R.id.layoutMessages);

        buttonCompatibility.setOnClickListener(v -> {
            int target = cardCompatibilityMessages.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            cardCompatibilityMessages.setVisibility(target);
        });

        setupFishRecyclerView();

        buttonEdit.setOnClickListener(v -> {
            if (aquariumId != null) {
                AddAquariumFragment editFragment = AddAquariumFragment.newInstance(aquariumId);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView5, editFragment).addToBackStack(null).commit();
            }
        });

        if (aquariumId != null) loadAquariumDetails();
    }

    private void setupFishRecyclerView() {
        recyclerViewFishInAquarium.setLayoutManager(new LinearLayoutManager(getContext()));
        fishAdapter = new FishAdapterForDetailView(getContext(), fishPreviewList, this);
        recyclerViewFishInAquarium.setAdapter(fishAdapter);
    }

    private void loadAquariumDetails() {
        db.collection("aquariums").document(aquariumId).get().addOnSuccessListener(document -> {
            if (!isAdded() || getContext() == null) return;
            try {
                currentAquarium = document.toObject(Aquarium.class);
                if (currentAquarium != null) {
                    currentAquarium.setId(document.getId());
                    populateUI(currentAquarium);
                    processFishData(currentAquarium.getFishCounts());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading aquarium", e);
            }
        });
    }

    private void processFishData(Map<String, Integer> counts) {
        if (counts == null) return;
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<FishPreview> uniqueFish = new ArrayList<>();
            List<FishPreview> spritesList = new ArrayList<>();
            List<String> missingIds = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                FishPreview p = atlas.getCachedFishPreviewById(entry.getKey());
                if (p != null) {
                    SuggestionEngine.Rating r = SuggestionEngine.calculateRating(p, currentAquarium);
                    p.setSuggestionRating(r);
                    uniqueFish.add(p);
                    for (int i = 0; i < entry.getValue(); i++) spritesList.add(p);
                } else {
                    missingIds.add(entry.getKey());
                }
            }

            if (!missingIds.isEmpty()) {
                Log.d(TAG, "Syncing " + missingIds.size() + " fish for report...");
                final AtomicInteger latch = new AtomicInteger(missingIds.size());
                for (String id : missingIds) {
                    db.collection("atlas_previews").document(id).get().addOnSuccessListener(doc -> {
                        FishPreview fp = doc.toObject(FishPreview.class);
                        if (fp != null) {
                            fp.setId(doc.getId());
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                com.example.javaqua.atlas.RoomFishPreview rp = new com.example.javaqua.atlas.RoomFishPreview();
                                rp.setId(fp.getId()); rp.setName(fp.getName()); rp.setLatin(fp.getLatin()); 
                                rp.setImageUrl(fp.getImageUrl()); rp.setSprite(fp.getSprite());
                                rp.setMinVolume(fp.getMinVolume()); rp.setMinTemp(fp.getMinTemp()); rp.setMaxTemp(fp.getMaxTemp());
                                rp.setShortDescription(fp.getShortDescription()); rp.setDescription(fp.getDescription());
                                AppDatabase.getDatabase(requireContext()).fishPreviewDao().insert(rp);
                                if (latch.decrementAndGet() == 0) processFishData(counts);
                            });
                        } else latch.decrementAndGet();
                    }).addOnFailureListener(e -> latch.decrementAndGet());
                }
                return;
            }

            SuggestionEngine.CompatibilityResult report = SuggestionEngine.getCompatibilityReport(currentAquarium, uniqueFish, counts);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    updateCompatibilityUI(report);
                    fishAdapter.setCounts(counts);
                    fishAdapter.updateData(uniqueFish);
                    if (animatedAquariumView != null) {
                        animatedAquariumView.setAnimationEnabled(true);
                        animatedAquariumView.setAquariumData(currentAquarium.getBackground(), spritesList);
                    }
                });
            }
        });
    }

    private void updateCompatibilityUI(SuggestionEngine.CompatibilityResult report) {
        int color;
        switch (report.overallRating) {
            case RED: color = Color.RED; break;
            case YELLOW: color = Color.YELLOW; break;
            case GREEN: color = Color.parseColor("#4CAF50"); break;
            default: color = Color.GRAY; break;
        }
        
        buttonCompatibility.setStrokeColor(ColorStateList.valueOf(color));
        buttonCompatibility.setTextColor(color);
        buttonCompatibility.setIconTint(ColorStateList.valueOf(color));

        layoutMessages.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String msg : report.messages) {
            TextView tv = (TextView) inflater.inflate(R.layout.item_compatibility_message, layoutMessages, false);
            tv.setText("• " + msg);
            layoutMessages.addView(tv);
        }
    }

    private void populateUI(Aquarium aquarium) {
        textViewAquariumName.setText(aquarium.getName());
        textViewAquariumVolume.setText(String.format(Locale.getDefault(), "%.1f L", aquarium.getVolumeLiters()));
        textViewAquariumTemperature.setText(String.format(Locale.getDefault(), "%.1f °C", aquarium.getTemperature()));
    }

    @Override
    public void onFishItemClick(FishPreview fishPreview, int position) {
        FishDetailFragment fragment = FishDetailFragment.newInstance(fishPreview.getId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView5, fragment).addToBackStack(null).commit();
    }
}