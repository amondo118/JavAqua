package com.example.javaqua.forum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.javaqua.R;
import com.example.javaqua.atlas.FishDetailFragment;
import com.example.javaqua.databinding.FragmentForumBinding;
import com.example.javaqua.model.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ForumFragment extends Fragment implements ForumPostAdapter.OnPostInteractionListener {

    private FragmentForumBinding binding;
    private ForumViewModel viewModel;
    private ForumPostAdapter forumPostAdapter;

    public ForumFragment() {
        // Required empty public constructor
    }

    public static ForumFragment newInstance() {
        return new ForumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForumViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForumBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchView();
        setupSortSpinner();
        observeViewModel();

        viewModel.fetchPosts();

        binding.fabCreatePost.setOnClickListener(v -> {
            CreatePostFragment createPostFragment = CreatePostFragment.newInstance(null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView5, createPostFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupRecyclerView() {
        forumPostAdapter = new ForumPostAdapter(getContext(), new ArrayList<>(), this, FirebaseAuth.getInstance());
        binding.recyclerViewForumPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewForumPosts.setAdapter(forumPostAdapter);
    }

    private void setupSearchView() {
        binding.searchViewForum.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.onSearchQueryChanged(query, viewModel.getSourcePosts().getValue());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.onSearchQueryChanged(newText, viewModel.getSourcePosts().getValue());
                return true;
            }
        });
    }

    private void setupSortSpinner() {
        binding.spinnerSortForum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.onSortPositionChanged(position, viewModel.getSourcePosts().getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void observeViewModel() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            forumPostAdapter.updatePosts(posts);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // UI could show a progress bar here
        });

        viewModel.getSourcePosts().observe(getViewLifecycleOwner(), posts -> {
            viewModel.setPosts(posts);
        });
    }

    @Override
    public void onPostClicked(int position) {
        Post clickedPost = forumPostAdapter.getPosts().get(position);
        if (clickedPost != null && clickedPost.getPostId() != null) {
            PostDetailFragment postDetailFragment = PostDetailFragment.newInstance(clickedPost.getPostId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView5, postDetailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override public void onUpvoteClicked(int position) {}
    @Override public void onDownvoteClicked(int position) {}
    @Override public void onCommentClicked(int position) { onPostClicked(position); }

    @Override
    public void onRepostClicked(int position) {
        Post postToRepost = forumPostAdapter.getPosts().get(position);
        if (postToRepost != null && postToRepost.getPostId() != null) {
            CreatePostFragment createPostFragment = CreatePostFragment.newInstance(postToRepost.getPostId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView5, createPostFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onFishTagClicked(String fishId) {
        if (fishId == null || fishId.isEmpty()) return;
        FishDetailFragment fishDetailFragment = FishDetailFragment.newInstance(fishId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView5, fishDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override public void onCommentUpvoteClicked(int postPosition, int commentPosition) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
