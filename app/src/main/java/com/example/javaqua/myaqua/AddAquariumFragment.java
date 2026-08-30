package com.example.javaqua.myaqua;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.javaqua.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddAquariumFragment extends Fragment {

    private static final String TAG = "AddAquariumFragment";

    private EditText editTextAquariumName, editTextAquariumVolume, editTextAquariumTemp;
    private Button buttonSaveAquarium, buttonSelectFish;
    private TextView textViewSelectedFishInfo;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<String> selectedFishIds = new ArrayList<>();

    public AddAquariumFragment() {
        // Required empty public constructor
    }

    public static AddAquariumFragment newInstance() {
        return new AddAquariumFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_aquarium, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextAquariumName = view.findViewById(R.id.editTextAquariumName);
        editTextAquariumVolume = view.findViewById(R.id.editTextAquariumVolume);
        editTextAquariumTemp = view.findViewById(R.id.editTextAquariumTemp);
        buttonSaveAquarium = view.findViewById(R.id.buttonSaveAquarium);
        buttonSelectFish = view.findViewById(R.id.buttonSelectFish);
        textViewSelectedFishInfo = view.findViewById(R.id.textViewSelectedFishInfo);

        buttonSaveAquarium.setOnClickListener(v -> saveAquarium());

        buttonSelectFish.setOnClickListener(v -> {
            AddFishFragment addFishFragment = AddFishFragment.newInstance();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView5, addFishFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        getParentFragmentManager().setFragmentResultListener("selectedFishKey", this, (requestKey, bundle) -> {
            if (bundle != null) {
                ArrayList<String> fishIds = bundle.getStringArrayList("selectedFishIds");
                if (fishIds != null) {
                    this.selectedFishIds = fishIds;
                    if (fishIds.isEmpty()) {
                        textViewSelectedFishInfo.setText("No fish selected.");
                    } else {
                        textViewSelectedFishInfo.setText(fishIds.size() + " fish selected.");
                    }
                    Log.d(TAG, "Selected fish IDs: " + fishIds.toString());
                }
            }
        });
        textViewSelectedFishInfo.setText("No fish selected.");
    }

    private void saveAquarium() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editTextAquariumName.getText().toString().trim();
        String volumeStr = editTextAquariumVolume.getText().toString().trim();
        String tempStr = editTextAquariumTemp.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextAquariumName.setError("Name is required");
            return;
        }

        double volume = 0;
        if (!TextUtils.isEmpty(volumeStr)) {
            try {
                volume = Double.parseDouble(volumeStr);
            } catch (NumberFormatException e) {
                editTextAquariumVolume.setError("Invalid volume");
                return;
            }
        }

        double temperature = 0;
        if (!TextUtils.isEmpty(tempStr)) {
            try {
                temperature = Double.parseDouble(tempStr);
            } catch (NumberFormatException e) {
                editTextAquariumTemp.setError("Invalid temperature");
                return;
            }
        }

        Map<String, Object> aquariumData = new HashMap<>();
        aquariumData.put("ownerID", currentUser.getUid());
        aquariumData.put("name", name);
        aquariumData.put("background","https://img.freepik.com/free-vector/sea-background-video-conferencing_52683-46343.jpg"); // Optional
        if (volume > 0) aquariumData.put("volume", volume);
        if (temperature > 0) aquariumData.put("temperature", temperature);
        aquariumData.put("fishes", selectedFishIds);

        db.collection("aquariums")
                .add(aquariumData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Aquarium added successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Aquarium added with ID: " + documentReference.getId());
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding aquarium: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error adding aquarium", e);
                });
    }
}
