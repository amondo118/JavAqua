package com.example.javaqua.myaqua;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.javaqua.R;
import com.example.javaqua.databinding.FragmentMyAquariumBinding;

import java.util.ArrayList;

public class MyAquariumFragment extends Fragment {

    private static final String TAG = "MyAquariumFragment";
    private FragmentMyAquariumBinding binding;
    private MyAquariumViewModel viewModel;
    private AquariumAdapter aquariumAdapter;

    public MyAquariumFragment() {
        // Required empty public constructor
    }

    public static MyAquariumFragment newInstance() {
        return new MyAquariumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyAquariumViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyAquariumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        observeViewModel();

        binding.buttonAddAquarium.setOnClickListener(v -> {
            AddAquariumFragment addAquariumFragment = new AddAquariumFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView5, addAquariumFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupRecyclerView() {
        aquariumAdapter = new AquariumAdapter(new ArrayList<>(), item -> {
            if (item != null && item.getId() != null) {
                AquariumDetailFragment detailFragment = AquariumDetailFragment.newInstance(item.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView5, detailFragment) 
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Error: Cannot open aquarium details.", Toast.LENGTH_SHORT).show();
            }
        });
        binding.recyclerViewAquariums.setAdapter(aquariumAdapter);
    }

    private void observeViewModel() {
        viewModel.getAquariumList().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.recyclerViewAquariums.setVisibility(View.GONE);
                binding.textViewEmptyAquariums.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewAquariums.setVisibility(View.VISIBLE);
                binding.textViewEmptyAquariums.setVisibility(View.GONE);
                aquariumAdapter.submitList(new ArrayList<>(list));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadAquariums();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
