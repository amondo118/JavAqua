package com.example.javaqua.myaqua;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.data.AppDatabase;
import com.example.javaqua.data.repository.AquariumRepository;
import com.example.javaqua.model.Aquarium;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddAquariumFragment extends Fragment {

    private static final String TAG = "AddAquaFrag";
    private static final String ARG_AQUARIUM_ID = "aquarium_id";

    private EditText editTextAquariumName;
    private Slider sliderVolume, sliderTemp;
    private TextView textViewVolumeDisplay, textViewTempDisplay;
    private RecyclerView recyclerViewBackgrounds, recyclerViewSelectedFish;
    private Button buttonSaveAquarium, buttonAddFish;

    // Selection Overlay
    private View fishSelectionSection;
    private EditText editTextSearchFish;
    private RecyclerView recyclerViewAllFish;
    private SelectableFishAdapter selectableAdapter;

    private SelectedFishAdapter selectedFishAdapter;
    private Atlas atlas;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String aquariumId = null;
    private String selectedBackgroundUrl = "";
    private final Map<String, Integer> currentFishCounts = new HashMap<>();
    private final List<FishPreview> allAvailableFish = new ArrayList<>();

    private final List<String> backgroundPresets = Arrays.asList(
            "https://img.freepik.com/free-vector/sea-background-video-conferencing_52683-46343.jpg",
            "https://img.freepik.com/free-photo/underwater-world-ocean-floor_23-2150931557.jpg",
            "https://img.freepik.com/free-photo/view-aquarium-coral-reef_23-2150150965.jpg",
            "https://img.freepik.com/free-photo/beautiful-underwater-landscape-with-sun-rays_23-2148281358.jpg"
    );

    public AddAquariumFragment() {}

    public static AddAquariumFragment newInstance(@Nullable String aquariumId) {
        AddAquariumFragment fragment = new AddAquariumFragment();
        if (aquariumId != null) {
            Bundle args = new Bundle();
            args.putString(ARG_AQUARIUM_ID, aquariumId);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        atlas = new Atlas(requireContext());
        if (getArguments() != null) aquariumId = getArguments().getString(ARG_AQUARIUM_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_aquarium, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextAquariumName = view.findViewById(R.id.editTextAquariumName);
        sliderVolume = view.findViewById(R.id.sliderVolume);
        sliderTemp = view.findViewById(R.id.sliderTemp);
        textViewVolumeDisplay = view.findViewById(R.id.textViewVolumeDisplay);
        textViewTempDisplay = view.findViewById(R.id.textViewTempDisplay);
        recyclerViewBackgrounds = view.findViewById(R.id.recyclerViewBackgrounds);
        recyclerViewSelectedFish = view.findViewById(R.id.recyclerViewSelectedFish);
        buttonAddFish = view.findViewById(R.id.buttonAddFish);
        buttonSaveAquarium = view.findViewById(R.id.buttonSaveAquarium);

        fishSelectionSection = view.findViewById(R.id.fishSelectionSection);
        editTextSearchFish = view.findViewById(R.id.editTextSearchFish);
        recyclerViewAllFish = view.findViewById(R.id.recyclerViewAllFish);
        Button buttonFinishSelection = view.findViewById(R.id.buttonFinishSelection);

        setupSliders();
        setupBackgrounds();
        setupSelectedFishList();
        setupSelectionOverlay(buttonFinishSelection);

        if (aquariumId != null) {
            loadExistingAquarium();
            ((TextView)view.findViewById(R.id.textViewTitle)).setText("Edit Aquarium");
        } else {
            selectedBackgroundUrl = backgroundPresets.get(0);
        }

        buttonAddFish.setOnClickListener(v -> showSelectionOverlay());
        buttonSaveAquarium.setOnClickListener(v -> saveAquarium());
    }

    private void setupSliders() {
        sliderVolume.addOnChangeListener((s, v, f) -> textViewVolumeDisplay.setText(String.format(Locale.getDefault(), "%.0f L", v)));
        sliderTemp.addOnChangeListener((s, v, f) -> textViewTempDisplay.setText(String.format(Locale.getDefault(), "%.1f °C", v)));
    }

    private void setupBackgrounds() {
        BackgroundAdapter adapter = new BackgroundAdapter(backgroundPresets, url -> selectedBackgroundUrl = url);
        recyclerViewBackgrounds.setAdapter(adapter);
    }

    private void setupSelectedFishList() {
        selectedFishAdapter = new SelectedFishAdapter((id, count) -> {
            if (count <= 0) currentFishCounts.remove(id);
            else currentFishCounts.put(id, count);
            refreshSelectedFishDisplay();
        });
        recyclerViewSelectedFish.setAdapter(selectedFishAdapter);
    }

    private void setupSelectionOverlay(Button finishBtn) {
        selectableAdapter = new SelectableFishAdapter();
        recyclerViewAllFish.setAdapter(selectableAdapter);

        finishBtn.setOnClickListener(v -> {
            List<String> ids = selectableAdapter.getSelectedFishIds();
            for (String id : ids) if (!currentFishCounts.containsKey(id)) currentFishCounts.put(id, 1);
            currentFishCounts.keySet().removeIf(id -> !ids.contains(id));
            refreshSelectedFishDisplay();
            fishSelectionSection.setVisibility(View.GONE);
        });

        editTextSearchFish.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterSearch(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAllAvailableFish();
    }

    private void loadAllAvailableFish() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<FishPreview> cached = atlas.getCachedFishPreviewsList();
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                allAvailableFish.clear();
                allAvailableFish.addAll(cached);
                selectableAdapter.submitList(new ArrayList<>(allAvailableFish));
                
                if (allAvailableFish.isEmpty()) {
                    db.collection("atlas_previews").get().addOnSuccessListener(docs -> {
                        for (QueryDocumentSnapshot doc : docs) {
                            FishPreview fp = doc.toObject(FishPreview.class);
                            fp.setId(doc.getId());
                            allAvailableFish.add(fp);
                        }
                        selectableAdapter.submitList(new ArrayList<>(allAvailableFish));
                    });
                }
            });
        });
    }

    private void filterSearch(String q) {
        List<FishPreview> filtered = new ArrayList<>();
        String query = q.toLowerCase().trim();
        for (FishPreview f : allAvailableFish) {
            String name = f.getName() != null ? f.getName().toLowerCase() : "";
            String latin = f.getLatin() != null ? f.getLatin().toLowerCase() : "";
            if (name.contains(query) || latin.contains(query)) filtered.add(f);
        }
        selectableAdapter.submitList(filtered);
    }

    private void showSelectionOverlay() {
        if (allAvailableFish.isEmpty()) loadAllAvailableFish();
        selectableAdapter.setSelectedFishIds(new ArrayList<>(currentFishCounts.keySet()));
        fishSelectionSection.setVisibility(View.VISIBLE);
    }

    private void refreshSelectedFishDisplay() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<FishPreview> displayList = new ArrayList<>();
            Map<String, Integer> countsCopy = new HashMap<>(currentFishCounts);
            List<String> missing = new ArrayList<>();

            for (String id : countsCopy.keySet()) {
                FishPreview p = atlas.getCachedFishPreviewById(id);
                if (p != null) displayList.add(p); else missing.add(id);
            }

            // Always update UI with what we have immediately
            if (isAdded()) requireActivity().runOnUiThread(() -> selectedFishAdapter.setData(displayList, countsCopy));

            if (!missing.isEmpty()) {
                final AtomicInteger latch = new AtomicInteger(missing.size());
                for (String id : missing) {
                    db.collection("atlas_previews").document(id).get().addOnSuccessListener(doc -> {
                        FishPreview fp = doc.toObject(FishPreview.class);
                        if (fp != null) {
                            fp.setId(doc.getId());
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                if (!isAdded()) return;
                                com.example.javaqua.atlas.RoomFishPreview rp = new com.example.javaqua.atlas.RoomFishPreview();
                                rp.setId(fp.getId()); rp.setName(fp.getName()); rp.setLatin(fp.getLatin()); 
                                rp.setImageUrl(fp.getImageUrl()); rp.setSprite(fp.getSprite());
                                AppDatabase.getDatabase(requireContext()).fishPreviewDao().insert(rp);
                                if (latch.decrementAndGet() == 0) refreshSelectedFishDisplay();
                            });
                        } else latch.decrementAndGet();
                    }).addOnFailureListener(e -> latch.decrementAndGet());
                }
            }
        });
    }

    private void loadExistingAquarium() {
        db.collection("aquariums").document(aquariumId).get().addOnSuccessListener(doc -> {
            if (!isAdded()) return;
            Aquarium aqua = doc.toObject(Aquarium.class);
            if (aqua != null) {
                editTextAquariumName.setText(aqua.getName());
                sliderVolume.setValue((float) aqua.getVolumeLiters());
                sliderTemp.setValue((float) aqua.getTemperature());
                selectedBackgroundUrl = aqua.getBackground();
                currentFishCounts.clear();
                currentFishCounts.putAll(aqua.getFishCounts());
                refreshSelectedFishDisplay();
            }
        });
    }

    private void saveAquarium() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String name = editTextAquariumName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { editTextAquariumName.setError("Required"); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("ownerID", user.getUid()); data.put("name", name);
        data.put("volume", (double) sliderVolume.getValue()); data.put("temperature", (double) sliderTemp.getValue());
        data.put("background", selectedBackgroundUrl.isEmpty() ? backgroundPresets.get(0) : selectedBackgroundUrl);
        data.put("fishes", currentFishCounts);

        if (aquariumId == null) db.collection("aquariums").add(data).addOnSuccessListener(r -> onSaveSuccess(user.getUid()));
        else db.collection("aquariums").document(aquariumId).set(data).addOnSuccessListener(v -> onSaveSuccess(user.getUid()));
    }

    private void onSaveSuccess(String uid) {
        new AquariumRepository(requireContext()).syncAquariums(uid);
        if (isAdded()) getParentFragmentManager().popBackStack();
    }

    private class BackgroundAdapter extends RecyclerView.Adapter<BackgroundAdapter.VH> {
        private final List<String> urls; private final OnSelect listener; private int selectedPos = 0;
        interface OnSelect { void onSelect(String url); }
        BackgroundAdapter(List<String> urls, OnSelect listener) {
            this.urls = urls; this.listener = listener;
            if (!urls.isEmpty()) listener.onSelect(urls.get(0));
        }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_background_preset, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Glide.with(h.itemView).load(urls.get(p)).into(h.img);
            h.card.setStrokeColor(selectedPos == p ? getResources().getColor(R.color.light_primary, null) : 0);
            h.itemView.setOnClickListener(v -> {
                int old = selectedPos; selectedPos = h.getAdapterPosition();
                notifyItemChanged(old); notifyItemChanged(selectedPos);
                listener.onSelect(urls.get(p));
            });
        }
        @Override public int getItemCount() { return urls.size(); }
        class VH extends RecyclerView.ViewHolder {
            ImageView img; com.google.android.material.card.MaterialCardView card;
            VH(View v) { super(v); img = v.findViewById(R.id.imageViewPreset); card = (com.google.android.material.card.MaterialCardView)v; }
        }
    }
}