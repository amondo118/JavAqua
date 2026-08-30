package com.example.javaqua.forum;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.main.ThemeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePostFragment extends Fragment {

    private static final String TAG = "CreatePostFragment";
    private static final String ARG_REPOST_ID = "repost_id";

    private EditText editTextPostLabel, editTextFullPostText;
    private Button buttonSubmitPost;
    private TextView textViewRepostIndicator;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String repostPostId = null;

    private final List<String> taggedFishIdsInPost = new ArrayList<>();
    private final List<FishPreview> availableFishPreviews = new ArrayList<>();
    private final List<ForegroundColorSpan> appliedSpans = new ArrayList<>();
    private Atlas atlas;


    public CreatePostFragment() {
    }

    public static CreatePostFragment newInstance(@Nullable String repostPostId) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
        if (repostPostId != null) {
            args.putString(ARG_REPOST_ID, repostPostId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null && getArguments().containsKey(ARG_REPOST_ID)) {
            repostPostId = getArguments().getString(ARG_REPOST_ID);
        }
        if (getContext() != null) {
            atlas = new Atlas(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextPostLabel = view.findViewById(R.id.editTextPostLabel);
        editTextFullPostText = view.findViewById(R.id.editTextFullPostText);
        buttonSubmitPost = view.findViewById(R.id.buttonSubmitPost);
        textViewRepostIndicator = view.findViewById(R.id.textViewRepostIndicator);


        if (repostPostId != null) {
            textViewRepostIndicator.setText("Reposting content...");
            textViewRepostIndicator.setVisibility(View.VISIBLE);

        } else {
            textViewRepostIndicator.setVisibility(View.GONE);
        }

        buttonSubmitPost.setOnClickListener(v -> createNewPost());

        loadFishPreviews();

        editTextFullPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                applyTagHighlighting(s);
            }
        });
    }

    private void loadFishPreviews() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FishPreview> previews = atlas != null ? atlas.getCachedFishPreviewsList() : null;
            if (previews != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    availableFishPreviews.clear();
                    availableFishPreviews.addAll(previews);
                    Collections.sort(availableFishPreviews, (f1, f2) -> {
                        String name1 = f1.getName();
                        String name2 = f2.getName();
                        if (name1 == null && name2 == null) return 0;
                        if (name1 == null) return 1;
                        if (name2 == null) return -1;
                        return Integer.compare(name2.length(), name1.length());
                    });
                    Log.d(TAG, "Loaded and sorted " + availableFishPreviews.size() + " fish previews.");
                    
                    // Re-apply tagging in case user already typed something
                    applyTagHighlighting(editTextFullPostText.getText());
                });
            }
        });
    }

    private void applyTagHighlighting(Editable s) {
        if (s == null) return;
        
        for (ForegroundColorSpan span : appliedSpans) {
            s.removeSpan(span);
        }
        appliedSpans.clear();
        taggedFishIdsInPost.clear();

        if (availableFishPreviews.isEmpty()) return;
        if (getContext() == null) return;

        Pattern pattern = Pattern.compile("@([\\w\\s]+?)(?=\\s@|\\n|$)");
        Matcher matcher = pattern.matcher(s.toString());

        while (matcher.find()) {
            String fishNameMention = matcher.group(1); 
            if (fishNameMention != null) {
                String userTypedFishNamePart = fishNameMention.trim().toLowerCase();
                for (FishPreview fish : availableFishPreviews) {
                    if (fish.getName() != null && !fish.getName().isEmpty()) {
                        String actualFishName = fish.getName().trim().toLowerCase();
                        if (userTypedFishNamePart.startsWith(actualFishName)) {
                            if (fish.getId() != null) {
                                if (!taggedFishIdsInPost.contains(fish.getId())) {
                                    taggedFishIdsInPost.add(fish.getId());
                                }
                            }

                            ForegroundColorSpan span = new ForegroundColorSpan(ThemeUtils.getThemeColor(getContext(), R.attr.tagTextColor));
                            int spanStart = matcher.start(0); 
                            int spanEnd = spanStart + 1 + actualFishName.length();

                            if (spanStart >= 0 && spanEnd <= s.length() && spanStart < spanEnd) {
                                s.setSpan(span, spanStart, spanEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                appliedSpans.add(span);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void createNewPost() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to post.", Toast.LENGTH_SHORT).show();
            return;
        }

        String postLabel = editTextPostLabel.getText().toString().trim();
        String fullText = editTextFullPostText.getText().toString().trim();

        if (TextUtils.isEmpty(postLabel)) {
            editTextPostLabel.setError("Post label cannot be empty.");
            return;
        }
        if (TextUtils.isEmpty(fullText)) {
            editTextFullPostText.setError("Post content cannot be empty.");
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            if (getContext() == null || !isAdded()) return;
            String username = userDoc.exists() ? userDoc.getString("username") : "Anonymous";

            Map<String, Object> post = new HashMap<>();
            post.put("userId", userId);
            post.put("username", username);
            post.put("postLabel", postLabel);
            post.put("fullText", fullText);
            post.put("timestamp", FieldValue.serverTimestamp());
            post.put("upvotes", 0);
            post.put("downvotes", 0);
            post.put("commentCount", 0);
            post.put("upvotedBy", new ArrayList<String>());
            post.put("downvotedBy", new ArrayList<String>());
            post.put("taggedFishIds", new ArrayList<>(taggedFishIdsInPost)); 

            if (repostPostId != null) {
                post.put("linkedPostId", repostPostId);
            }

            db.collection("posts")
                    .add(post)
                    .addOnSuccessListener(documentReference -> {
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), "Post created successfully!", Toast.LENGTH_SHORT).show();
                        }
                        if (getActivity() != null) {
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            if (fm.getBackStackEntryCount() > 0) {
                                fm.popBackStack();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), "Error creating post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        Log.e(TAG, "Error creating post", e);
                    });

        }).addOnFailureListener(e -> {
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Error fetching user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "Error fetching user details for post creation", e);
        });
    }
}
