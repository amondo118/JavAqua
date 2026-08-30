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

import java.util.ArrayList;

public class AtlasFragment extends Fragment implements FishPreviewAdapter.DetailsLoader {
    private static final int GRID_COLUMNS = 1;

    private FragmentAtlasBinding binding;
    private AtlasViewModel viewModel;
    private FishPreviewAdapter adapter;
    private boolean currentSortAscending = true;

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
        observeViewModel();

        viewModel.loadPreviews();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), GRID_COLUMNS));
        adapter = new FishPreviewAdapter(this);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.seachEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchPreviews(s.toString().trim());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSort() {
        binding.imageView2.setOnClickListener(v -> {
            currentSortAscending = !currentSortAscending;
            Toast.makeText(getContext(), 
                "Sorting by Name: " + (currentSortAscending ? "Ascending" : "Descending"), 
                Toast.LENGTH_SHORT).show();
            
            // Note: Sorting logic could also move to ViewModel if list is large
            if (adapter.getCurrentList() != null) {
                ArrayList<FishPreview> list = new ArrayList<>(adapter.getCurrentList());
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
            adapter.submitList(new ArrayList<>(previews));
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
