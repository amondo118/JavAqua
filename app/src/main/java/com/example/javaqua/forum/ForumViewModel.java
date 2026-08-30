package com.example.javaqua.forum;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.javaqua.data.repository.ForumRepository;
import com.example.javaqua.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForumViewModel extends ViewModel {
    private final ForumRepository forumRepository;
    private final MutableLiveData<List<Post>> displayedPosts = new MutableLiveData<>();
    private String currentSearchQuery = "";
    private int currentSortPosition = 0;

    public ForumViewModel() {
        this.forumRepository = new ForumRepository();
    }

    public LiveData<List<Post>> getPosts() {
        return displayedPosts;
    }

    public LiveData<Boolean> getIsLoading() {
        return forumRepository.getIsLoading();
    }

    public void fetchPosts() {
        forumRepository.fetchPosts();
    }

    public void setPosts(List<Post> allPosts) {
        filterAndSortPosts(allPosts);
    }

    public void onSearchQueryChanged(String query, List<Post> allPosts) {
        this.currentSearchQuery = query;
        filterAndSortPosts(allPosts);
    }

    public void onSortPositionChanged(int position, List<Post> allPosts) {
        this.currentSortPosition = position;
        filterAndSortPosts(allPosts);
    }

    private void filterAndSortPosts(List<Post> allPosts) {
        if (allPosts == null) return;
        
        List<Post> filtered = new ArrayList<>();
        String query = currentSearchQuery.toLowerCase().trim();

        for (Post post : allPosts) {
            boolean matches = true;
            if (!query.isEmpty()) {
                boolean labelMatches = post.getPostLabel() != null && post.getPostLabel().toLowerCase().contains(query);
                boolean textMatches = post.getFullText() != null && post.getFullText().toLowerCase().contains(query);
                boolean userMatches = post.getUsername() != null && post.getUsername().toLowerCase().contains(query);
                matches = labelMatches || textMatches || userMatches;
            }
            if (matches) {
                filtered.add(post);
            }
        }

        Collections.sort(filtered, (p1, p2) -> {
            switch (currentSortPosition) {
                case 0: // Newest
                    if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
                    return p2.getTimestamp().compareTo(p1.getTimestamp());
                case 1: // Oldest
                    if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
                    return p1.getTimestamp().compareTo(p2.getTimestamp());
                case 2: // Top (Upvotes)
                    return Integer.compare(p2.getUpvotes(), p1.getUpvotes());
                case 3: // Most Comments
                    return Integer.compare(p2.getCommentCount(), p1.getCommentCount());
                default:
                    return 0;
            }
        });

        displayedPosts.setValue(filtered);
    }

    public LiveData<List<Post>> getSourcePosts() {
        return forumRepository.getPosts();
    }
}
