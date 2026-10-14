package com.example.javaqua.atlas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.javaqua.R;
import com.example.javaqua.databinding.FragmentAtlasBinding;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.example.javaqua.data.repository.AquariumRepository;
import com.example.javaqua.myaqua.AquariumAdapter;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AtlasFragment extends Fragment implements FishPreviewAdapter.DetailsLoader {
    private static final int GRID_COLUMNS = 1;

    private FragmentAtlasBinding binding;
    private AtlasViewModel viewModel;
    private FishPreviewAdapter adapter;
    private boolean currentSortAscending = true;
    private List<AquariumAdapter.AquariumGridItem> userAquariums;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AtlasViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAtlasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearch();
        setupSort();
        setupAquariumSpinner();
        observeViewModel();

        viewModel.loadPreviews();
    }

    private void setupAquariumSpinner() {
        AquariumRepository repo = new AquariumRepository(requireContext());
        repo.getAquariumList().observe(getViewLifecycleOwner(), aquariums -> {
            this.userAquariums = aquariums;
            if (aquariums != null && !aquariums.isEmpty()) {
                List<String> names = aquariums.stream().map(AquariumAdapter.AquariumGridItem::getName).collect(Collectors.toList());
                names.add(0, "All (No Suggestions)");
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerAquariums.setAdapter(spinnerAdapter);
                binding.spinnerAquariums.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            viewModel.setSelectedAquarium(null);
                        } else {
                            viewModel.setSelectedAquarium(userAquariums.get(position - 1));
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            repo.loadAquariums(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), GRID_COLUMNS));
        adapter = new FishPreviewAdapter(this);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.seachEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchPreviews(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSort() {
        binding.buttonSort.setOnClickListener(v -> {
            currentSortAscending = !currentSortAscending;
            if (adapter.getCurrentList() != null) {
                List<FishPreview> list = adapter.getCurrentList();
                list.sort((o1, o2) -> {
                    String n1 = o1.getName() != null ? o1.getName() : "";
                    String n2 = o2.getName() != null ? o2.getName() : "";
                    return currentSortAscending ? n1.compareToIgnoreCase(n2) : n2.compareToIgnoreCase(n1);
                });
                adapter.submitList(list);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getFishPreviews().observe(getViewLifecycleOwner(), previews -> {
            if (previews != null) {
                adapter.submitList(previews);
            }
        });
    }

    @Override
    public void loadDetailsFor(FishPreview item, int position) {
        if (item == null || item.getId() == null) return;
        FishDetailFragment detailFragment = FishDetailFragment.newInstance(item.getId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView5, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}