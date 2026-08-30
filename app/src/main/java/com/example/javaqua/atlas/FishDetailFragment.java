package com.example.javaqua.atlas;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.javaqua.R;
import com.example.javaqua.forum.CreatePostFragment;
import com.example.javaqua.forum.ForumPostAdapter;
import com.example.javaqua.forum.PostDetailFragment;
import com.example.javaqua.model.Fish;
import com.example.javaqua.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FishDetailFragment extends Fragment implements ForumPostAdapter.OnPostInteractionListener {

    private static final String TAG = "FishDetailFragment";
    private static final String ARG_FISH_ID = "fish_id";

    private ImageView imageViewFishDetail;
    private TextView textViewFishNameDetail, textViewFishLatinNameDetail, textViewFishDescriptionDetail;
    private RecyclerView recyclerViewMentioningPosts;
    private SearchView searchViewMentioningPosts;
    private Spinner spinnerSortMentioningPosts;

    private ForumPostAdapter postsAdapter;
    private List<Post> fullMentioningPostList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String fishId;
    private Fish currentFish;

    public FishDetailFragment() {}

    public static FishDetailFragment newInstance(String fishId) {
        FishDetailFragment fragment = new FishDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FISH_ID, fishId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fishId = getArguments().getString(ARG_FISH_ID);
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fish_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewFishDetail = view.findViewById(R.id.imageViewFishDetail);
        textViewFishNameDetail = view.findViewById(R.id.textViewFishNameDetail);
        textViewFishLatinNameDetail = view.findViewById(R.id.textViewFishLatinNameDetail);
        textViewFishDescriptionDetail = view.findViewById(R.id.textViewFishDescriptionDetail);
        recyclerViewMentioningPosts = view.findViewById(R.id.recyclerViewMentioningPosts);
        searchViewMentioningPosts = view.findViewById(R.id.searchViewMentioningPosts);
        spinnerSortMentioningPosts = view.findViewById(R.id.spinnerSortMentioningPosts);

        setupMentioningPostsRecyclerView();
        setupSearchAndSort();

        if (fishId != null) {
            loadFishDetails();
            loadMentioningPosts(); 
        } else {
            Log.e(TAG, "Fish ID is null. Cannot load details.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Fish ID not found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadFishDetails() {
        if (fishId == null) return;
        db.collection("atlas").document(fishId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (getContext() == null || !isAdded()) return;
                if (documentSnapshot.exists()) {
                    currentFish = documentSnapshot.toObject(Fish.class);
                    if (currentFish != null) {
                        currentFish.setId(documentSnapshot.getId()); 
                        updateFishUI();
                    }
                } else {
                    Log.e(TAG, "Fish not found with ID: " + fishId);
                    if (getContext() != null) Toast.makeText(getContext(), "Fish not found.", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading fish details", e);
                if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Error loading fish data.", Toast.LENGTH_SHORT).show();
            });
    }

    private void updateFishUI() {
        if (currentFish == null || getContext() == null) return;
        textViewFishNameDetail.setText(currentFish.getName());
        textViewFishLatinNameDetail.setText(currentFish.getLatin());
        textViewFishDescriptionDetail.setText(currentFish.getDescription());

        String imageUrlToLoad = null;
        if (currentFish.getImageUrl() != null && !currentFish.getImageUrl().isEmpty()) {
            imageUrlToLoad = currentFish.getImageUrl();
        }

        if (imageUrlToLoad != null) {
             Glide.with(getContext())
                 .load(imageUrlToLoad)
                 .placeholder(R.drawable.aquarium_background_placeholder) 
                 .error(R.drawable.aquarium_background_error) 
                 .into(imageViewFishDetail);
        } else {
            imageViewFishDetail.setImageResource(R.drawable.aquarium_background_error);
        }
    }

    private void setupMentioningPostsRecyclerView() {
        postsAdapter = new ForumPostAdapter(getContext(), new ArrayList<>(), this, mAuth);
        recyclerViewMentioningPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMentioningPosts.setAdapter(postsAdapter);
        recyclerViewMentioningPosts.setNestedScrollingEnabled(false);
    }

    private void setupSearchAndSort() {
        searchViewMentioningPosts.setQueryHint("Search in posts...");
        searchViewMentioningPosts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFiltersAndSort();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFiltersAndSort();
                return false;
            }
        });

        spinnerSortMentioningPosts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFiltersAndSort();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void applyFiltersAndSort() {
        List<Post> filteredList = new ArrayList<>();
        String searchQuery = searchViewMentioningPosts.getQuery().toString().toLowerCase().trim();

        // 1. Filter
        for (Post post : fullMentioningPostList) {
            boolean matchesQuery = true;
            if (!searchQuery.isEmpty()) {
                matchesQuery = (post.getPostLabel() != null && post.getPostLabel().toLowerCase().contains(searchQuery)) ||
                               (post.getFullText() != null && post.getFullText().toLowerCase().contains(searchQuery)) ||
                               (post.getUsername() != null && post.getUsername().toLowerCase().contains(searchQuery));
            }
            if (matchesQuery) {
                filteredList.add(post);
            }
        }
        
        // 2. Sort
        // Spinner positions based on arrays.xml: 0=Newest, 1=Oldest, 2=Most Upvotes, 3=Most Comments
        int sortPosition = spinnerSortMentioningPosts.getSelectedItemPosition();

        filteredList.sort((p1, p2) -> {
            switch (sortPosition) {
                case 0: // Newest
                    if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
                    return p2.getTimestamp().compareTo(p1.getTimestamp());
                case 1: // Oldest
                    if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
                    return p1.getTimestamp().compareTo(p2.getTimestamp());
                case 2: // Most Upvotes
                    return Integer.compare(p2.getUpvotes(), p1.getUpvotes());
                case 3: // Most Comments
                    return Integer.compare(p2.getCommentCount(), p1.getCommentCount());
                default:
                    return 0;
            }
        });

        postsAdapter.updatePosts(filteredList);
    }

    private void loadMentioningPosts() {
        if (fishId == null) return;
        db.collection("posts")
                .whereArrayContains("taggedFishIds", fishId)
                .get()
                .addOnCompleteListener(task -> {
                    if (getContext() == null || !isAdded()) return;
                    if (task.isSuccessful()) {
                        fullMentioningPostList.clear();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                post.setPostId(document.getId());
                                fullMentioningPostList.add(post);
                            }
                        }
                        applyFiltersAndSort();
                    } else {
                        Log.e(TAG, "Error loading mentioning posts: ", task.getException());
                        if(getContext() != null) Toast.makeText(getContext(), "Failed to load related posts.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPostClicked(int position) {
        if (position < 0 || position >= postsAdapter.getItemCount()) return;
        Post clickedPost = postsAdapter.getPosts().get(position); 
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
        if (position < 0 || position >= postsAdapter.getItemCount()) return;
        Post postToRepost = postsAdapter.getPosts().get(position);
        if (postToRepost != null && postToRepost.getPostId() != null) {
            CreatePostFragment createPostFragment = CreatePostFragment.newInstance(postToRepost.getPostId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView5, createPostFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onFishTagClicked(String taggedFishId) {
        if (taggedFishId == null || taggedFishId.equals(this.fishId)) return; 
        FishDetailFragment newFishDetailFragment = FishDetailFragment.newInstance(taggedFishId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView5, newFishDetailFragment)
                .addToBackStack(null) 
                .commit();
    }
    
    @Override
    public void onCommentUpvoteClicked(int postPosition, int commentPosition) {}
}
