package com.example.javaqua.myaqua;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddFishFragment extends Fragment {

    private static final String TAG = "AddFishFragment";
    public static final String SELECTED_FISH_KEY = "selectedFishKey";
    public static final String SELECTED_FISH_IDS_BUNDLE_KEY = "selectedFishIds";

    private EditText editTextSearchFish;
    private RecyclerView recyclerViewSelectFish;
    private Button buttonSaveFishSelection;
    private SelectableFishAdapter selectableFishAdapter;
    private Atlas atlas;
    private List<FishPreview> originalFishList = new ArrayList<>();

    public AddFishFragment() {
    }

    public static AddFishFragment newInstance() {
        return new AddFishFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            atlas = new Atlas(getContext());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_fish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextSearchFish = view.findViewById(R.id.editTextSearchFish);
        recyclerViewSelectFish = view.findViewById(R.id.recyclerViewSelectFish);
        buttonSaveFishSelection = view.findViewById(R.id.buttonSaveFishSelection);

        setupRecyclerView();
        loadFishPreviews();
        setupSearch();

        buttonSaveFishSelection.setOnClickListener(v -> saveSelectedFish());
    }

    private void setupRecyclerView() {
        selectableFishAdapter = new SelectableFishAdapter(new ArrayList<>());
        recyclerViewSelectFish.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSelectFish.setAdapter(selectableFishAdapter);
    }

    private void loadFishPreviews() {
        Log.d(TAG, "Attempting to load fish previews.");
        if (atlas == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<FishPreview> cachedList = atlas.getCachedFishPreviewsList();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (cachedList != null && !cachedList.isEmpty()) {
                        Log.d(TAG, "Using cached fish previews: " + cachedList.size() + " items.");
                        displayFishList(cachedList);
                    } else {
                        Log.d(TAG, "Cached list empty. Fetching from Firestore.");
                        fetchFishFromFirestore();
                    }
                });
            }
        });
    }

    private void fetchFishFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("atlas_previews")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<FishPreview> fetchedList = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        for (DocumentSnapshot document : querySnapshot) {
                            FishPreview preview = document.toObject(FishPreview.class);
                            if (preview != null) {
                                preview.setId(document.getId());
                                fetchedList.add(preview);
                            }
                        }
                        Log.d(TAG, "Fetched " + fetchedList.size() + " fish from Firestore.");
                        displayFishList(fetchedList);
                    } else {
                        Log.e(TAG, "Error loading fish previews from Firestore.", task.getException());
                        Toast.makeText(getContext(), "Error loading fish list.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayFishList(List<FishPreview> list) {
        originalFishList.clear();
        originalFishList.addAll(list);
        selectableFishAdapter.submitList(new ArrayList<>(originalFishList));
        if (originalFishList.isEmpty()) {
            Toast.makeText(getContext(), "No fish found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        editTextSearchFish.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFishList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFishList(String searchText) {
        if (originalFishList == null) return;

        List<FishPreview> filteredList = new ArrayList<>();
        String query = searchText.toLowerCase(Locale.getDefault()).trim();

        if (query.isEmpty()) {
            filteredList.addAll(originalFishList);
        } else {
            for (FishPreview preview : originalFishList) {
                if (preview.getName() != null && preview.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredList.add(preview);
                }
            }
        }
        selectableFishAdapter.submitList(filteredList);
    }

    private void saveSelectedFish() {
        ArrayList<String> selectedIds = selectableFishAdapter.getSelectedFishIds();
        Bundle result = new Bundle();
        result.putStringArrayList(SELECTED_FISH_IDS_BUNDLE_KEY, selectedIds);
        getParentFragmentManager().setFragmentResult(SELECTED_FISH_KEY, result);
        Log.d(TAG, "Selected fish IDs: " + selectedIds.toString());
        Toast.makeText(getContext(), selectedIds.size() + " fish selected.", Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
