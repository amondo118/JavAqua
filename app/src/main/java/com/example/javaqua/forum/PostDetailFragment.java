package com.example.javaqua.forum;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishDetailFragment;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.main.ThemeUtils;
import com.example.javaqua.main.TimestampUtils;
import com.example.javaqua.model.Comment;
import com.example.javaqua.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PostDetailFragment extends Fragment implements CommentAdapter.CommentItemListener {

    private static final String TAG = "PostDetailFragment";
    private static final String ARG_POST_ID = "post_id";

    private TextView textViewPostDetailTitle, textViewPostDetailUsername, textViewPostDetailTimestamp, textViewPostDetailFullText, textViewOriginalPostLink;
    private TextView textViewUpvoteCountPostDetail, textViewDownvoteCountPostDetail;
    private ImageButton buttonUpvotePostDetail, buttonDownvotePostDetail;
    private RecyclerView recyclerViewComments;
    private EditText editTextNewComment;
    private Button buttonSubmitComment;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String postId;
    private Post currentPost;
    private Atlas atlas;

    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (getContext() != null) {
            atlas = new Atlas(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textViewPostDetailTitle = view.findViewById(R.id.textViewPostDetailTitle);
        textViewPostDetailUsername = view.findViewById(R.id.textViewPostDetailUsername);
        textViewPostDetailTimestamp = view.findViewById(R.id.textViewPostDetailTimestamp);
        textViewOriginalPostLink = view.findViewById(R.id.textViewOriginalPostLink);
        textViewPostDetailFullText = view.findViewById(R.id.textViewPostDetailFullText);
        textViewUpvoteCountPostDetail = view.findViewById(R.id.textViewUpvoteCountPostDetail);
        textViewDownvoteCountPostDetail = view.findViewById(R.id.textViewDownvoteCountPostDetail);
        buttonUpvotePostDetail = view.findViewById(R.id.buttonUpvotePostDetail);
        buttonDownvotePostDetail = view.findViewById(R.id.buttonDownvotePostDetail);
        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        editTextNewComment = view.findViewById(R.id.editTextNewComment);
        buttonSubmitComment = view.findViewById(R.id.buttonSubmitComment);
        setupRecyclerView();

        if (postId != null) {
            loadPostDetails();
            loadComments();
        } else {
            Log.e(TAG, "Post ID is null. Cannot load details.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Post ID not found.", Toast.LENGTH_SHORT).show();
            }
        }

        buttonSubmitComment.setOnClickListener(v -> submitComment());
        buttonUpvotePostDetail.setOnClickListener(v -> voteForPost(true));
        buttonDownvotePostDetail.setOnClickListener(v -> voteForPost(false));
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(getContext(), commentList, this, mAuth);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void loadPostDetails() {
        if (postId == null) {
            Log.e(TAG, "Post ID is null in loadPostDetails.");
            return;
        }
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !isAdded()) return;
                    if (documentSnapshot.exists()) {
                        currentPost = documentSnapshot.toObject(Post.class);
                        if (currentPost != null) {
                            currentPost.setPostId(documentSnapshot.getId());
                            updatePostUI();
                            updateVoteButtonTint();
                        }
                    } else {
                        Log.e(TAG, "Post not found with ID: " + postId);
                        if (getContext() != null) Toast.makeText(getContext(), "Post not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading post details", e);
                    if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Error loading post.", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFishTagHighlightingToFullText(String fullText) {
        Log.d(TAG, "applyFishTagHighlightingToFullText called with text: \"" + fullText + "\"");

        if (TextUtils.isEmpty(fullText)) {
            Log.w(TAG, "applyFishTagHighlightingToFullText: fullText is empty or null.");
            textViewPostDetailFullText.setText(fullText);
            return;
        }
        if (getContext() == null) {
            Log.w(TAG, "applyFishTagHighlightingToFullText: getContext() is null.");
            textViewPostDetailFullText.setText(fullText);
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<FishPreview> originalFishPreviews = atlas != null ? atlas.getCachedFishPreviewsList() : null;
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded() || getContext() == null) return;

                    if (originalFishPreviews == null || originalFishPreviews.isEmpty()) {
                        Log.w(TAG, "applyFishTagHighlightingToFullText: No fish previews available.");
                        textViewPostDetailFullText.setText(fullText);
                        return;
                    }

                    List<FishPreview> sortedFishPreviews = new ArrayList<>(originalFishPreviews);
                    Collections.sort(sortedFishPreviews, (f1, f2) -> {
                        String name1 = f1.getName();
                        String name2 = f2.getName();
                        if (name1 == null && name2 == null) return 0;
                        if (name1 == null) return 1;
                        if (name2 == null) return -1;
                        return Integer.compare(name2.length(), name1.length());
                    });

                    SpannableString spannableString = new SpannableString(fullText);
                    Pattern pattern = Pattern.compile("@([\\w\\s]+?)(?=\\s@|\\n|$)");
                    Matcher matcher = pattern.matcher(fullText);

                    while (matcher.find()) {
                        String fishNamePartFromRegex = matcher.group(1);
                        if (fishNamePartFromRegex != null) {
                            String trimmedMention = fishNamePartFromRegex.trim();
                            for (FishPreview fish : sortedFishPreviews) {
                                if (fish.getName() != null && !fish.getName().isEmpty()) {
                                    String actualFishName = fish.getName().trim();
                                    if (trimmedMention.toLowerCase().startsWith(actualFishName.toLowerCase())) {
                                        final String clickedFishId = fish.getId();
                                        ClickableSpan clickableSpan = new ClickableSpan() {
                                            @Override
                                            public void onClick(@NonNull View widget) {
                                                FishDetailFragment fishDetailFragment = FishDetailFragment.newInstance(clickedFishId);
                                                getParentFragmentManager().beginTransaction()
                                                        .replace(R.id.fragmentContainerView5, fishDetailFragment)
                                                        .addToBackStack(null)
                                                        .commit();
                                            }
                                        };
                                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ThemeUtils.getThemeColor(getContext(), R.attr.tagTextColor));
                                        int spanStart = matcher.start(0);
                                        int spanEnd = spanStart + 1 + actualFishName.length();

                                        if (spanStart >= 0 && spanEnd <= spannableString.length() && spanStart < spanEnd) {
                                            spannableString.setSpan(clickableSpan, spanStart, spanEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            spannableString.setSpan(colorSpan, spanStart, spanEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    textViewPostDetailFullText.setText(spannableString);
                    textViewPostDetailFullText.setMovementMethod(LinkMovementMethod.getInstance());
                });
            }
        });
    }

    private void updatePostUI() {
        Log.d(TAG, "updatePostUI called.");
        if (currentPost == null) {
            Log.w(TAG, "updatePostUI: currentPost is null. Cannot update UI.");
            return;
        }
        if (getContext() == null) {
            Log.w(TAG, "updatePostUI: getContext() is null. Cannot update UI.");
            return;
        }
        Log.d(TAG, "updatePostUI: currentPost ID: " + currentPost.getPostId());

        textViewPostDetailTitle.setText(currentPost.getPostLabel());
        textViewPostDetailUsername.setText("by " + currentPost.getUsername());
        if (currentPost.getTimestamp() != null) {
            textViewPostDetailTimestamp.setText(TimestampUtils.formatRelativeTimestamp(currentPost.getTimestamp()));
        } else {
            textViewPostDetailTimestamp.setText("No date");
        }

        if (currentPost.getFullText() != null) {
            applyFishTagHighlightingToFullText(currentPost.getFullText());
        } else {
            textViewPostDetailFullText.setText("");
        }

        textViewUpvoteCountPostDetail.setText(String.valueOf(currentPost.getUpvotes()));
        textViewDownvoteCountPostDetail.setText(String.valueOf(currentPost.getDownvotes()));

        if (currentPost.getLinkedPostId() != null && !currentPost.getLinkedPostId().isEmpty()) {
            textViewOriginalPostLink.setVisibility(View.VISIBLE);
            textViewOriginalPostLink.setOnClickListener(v -> {
                PostDetailFragment originalPostFragment = PostDetailFragment.newInstance(currentPost.getLinkedPostId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView5, originalPostFragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            textViewOriginalPostLink.setVisibility(View.GONE);
        }
    }

    private void updateVoteButtonTint() {
        if (currentPost == null || mAuth.getCurrentUser() == null || getContext() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();

        List<String> upvotedBy = currentPost.getUpvotedBy() != null ? currentPost.getUpvotedBy() : new ArrayList<>();
        List<String> downvotedBy = currentPost.getDownvotedBy() != null ? new ArrayList<>(currentPost.getDownvotedBy()) : new ArrayList<>();

        if (upvotedBy.contains(currentUserId)) {
            buttonUpvotePostDetail.setColorFilter(ThemeUtils.getThemeColor(getContext(), R.attr.upvoteColor));
        } else {
            buttonUpvotePostDetail.clearColorFilter();
        }

        if (downvotedBy.contains(currentUserId)) {
            buttonDownvotePostDetail.setColorFilter(ThemeUtils.getThemeColor(getContext(), R.attr.downvoteColor));
        } else {
            buttonDownvotePostDetail.clearColorFilter();
        }
    }

    private void loadComments() {
        if (postId == null) {
            Log.e(TAG, "Post ID is null in loadComments.");
            return;
        }
        db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null || !isAdded()) return;
                    commentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Comment comment = document.toObject(Comment.class);
                        comment.setCommentId(document.getId());
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Comments loaded: " + commentList.size());
                })
                .addOnFailureListener(e -> {
                     Log.e(TAG, "Error loading comments", e);
                     if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Error loading comments.", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitComment() {
        String commentText = editTextNewComment.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            editTextNewComment.setError("Comment cannot be empty.");
            return;
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "You must be logged in to comment.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            if (getContext() == null || !isAdded()) return;
            String username = userDoc.exists() ? userDoc.getString("username") : "Anonymous";

            Map<String, Object> commentData = new HashMap<>();
            commentData.put("postId", postId);
            commentData.put("userId", userId);
            commentData.put("username", username);
            commentData.put("text", commentText);
            commentData.put("timestamp", FieldValue.serverTimestamp());
            commentData.put("upvotes", 0);
            commentData.put("downvotes", 0);
            commentData.put("upvotedBy", new ArrayList<String>());
            commentData.put("downvotedBy", new ArrayList<String>());

            WriteBatch batch = db.batch();
            DocumentReference newCommentRef = db.collection("posts").document(postId)
                                              .collection("comments").document();
            batch.set(newCommentRef, commentData);
            DocumentReference postRef = db.collection("posts").document(postId);
            batch.update(postRef, "commentCount", FieldValue.increment(1));

            batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment added and post commentCount updated successfully.");
                    if (getContext() == null || !isAdded()) return;
                    Toast.makeText(getContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
                    editTextNewComment.setText("");
                    InputMethodManager imm = null;
                    if (getActivity() != null) {
                       imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    if (imm != null && editTextNewComment != null && editTextNewComment.getWindowToken() != null) {
                        imm.hideSoftInputFromWindow(editTextNewComment.getWindowToken(), 0);
                    }
                    Comment newComment = new Comment(postId, userId, username, commentText);
                    newComment.setCommentId(newCommentRef.getId());
                    commentList.add(newComment);
                    loadComments();
                    if (currentPost != null) {
                        currentPost.setCommentCount(currentPost.getCommentCount() + 1);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding comment and updating count", e);
                        if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Failed to post comment.", Toast.LENGTH_SHORT).show();
                });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get user details for comment", e);
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Failed to get user details.", Toast.LENGTH_SHORT).show();
        });
    }

    private void voteForPost(boolean isUpvoteAttempt) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "You must be logged in to vote.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPost == null || currentPost.getPostId() == null) {
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Post data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = firebaseUser.getUid();
        DocumentReference postRef = db.collection("posts").document(currentPost.getPostId());

        db.runTransaction((com.google.firebase.firestore.Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(postRef);
            Post postSnapshot = snapshot.toObject(Post.class);

            if (postSnapshot == null) {
                throw new FirebaseFirestoreException("Post not found in transaction", FirebaseFirestoreException.Code.ABORTED);
            }

            List<String> upvotedBy = postSnapshot.getUpvotedBy() != null ? new ArrayList<>(postSnapshot.getUpvotedBy()) : new ArrayList<>();
            List<String> downvotedBy = postSnapshot.getDownvotedBy() != null ? new ArrayList<>(postSnapshot.getDownvotedBy()) : new ArrayList<>();

            int upvotes = postSnapshot.getUpvotes();
            int downvotes = postSnapshot.getDownvotes();

            boolean alreadyUpvoted = upvotedBy.contains(currentUserId);
            boolean alreadyDownvoted = downvotedBy.contains(currentUserId);

            if (isUpvoteAttempt) {
                if (alreadyUpvoted) {
                    upvotedBy.remove(currentUserId);
                    upvotes--;
                } else {
                    upvotedBy.add(currentUserId);
                    upvotes++;
                    if (alreadyDownvoted) {
                        downvotedBy.remove(currentUserId);
                        downvotes--;
                    }
                }
            } else {
                if (alreadyDownvoted) {
                    downvotedBy.remove(currentUserId);
                    downvotes--;
                } else {
                    downvotedBy.add(currentUserId);
                    downvotes++;
                    if (alreadyUpvoted) {
                        upvotedBy.remove(currentUserId);
                        upvotes--;
                    }
                }
            }
            Map<String, Object> updates = new HashMap<>();
            updates.put("upvotes", upvotes);
            updates.put("downvotes", downvotes);
            updates.put("upvotedBy", upvotedBy);
            updates.put("downvotedBy", downvotedBy);

            transaction.update(postRef, updates);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Post vote transaction successful.");
            if (currentPost != null) {
                 loadPostDetails();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Post vote transaction failed.", e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Failed to register vote: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            loadPostDetails();
        });
    }

    @Override
    public void onCommentUpvoteClicked(int position) {
        if (position < 0 || position >= commentList.size()) return;
        Comment comment = commentList.get(position);
        updateCommentVote(comment.getCommentId(), true, position);
    }

    @Override
    public void onCommentDownvoteClicked(int position) {
        if (position < 0 || position >= commentList.size()) return;
        Comment comment = commentList.get(position);
        updateCommentVote(comment.getCommentId(), false, position);
    }

    @Override
    public void onCommentReplyClicked(int position) {
        if (getContext() == null || !isAdded() || position < 0 || position >= commentList.size()) return;
        Comment commentToReply = commentList.get(position);
        if (commentToReply.getUsername() != null) {
            editTextNewComment.setText("@" + commentToReply.getUsername() + " ");
            editTextNewComment.requestFocus();
            InputMethodManager imm = null;
            if (getActivity() != null) {
                imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            }
            if (imm != null && editTextNewComment != null) {
                imm.showSoftInput(editTextNewComment, InputMethodManager.SHOW_IMPLICIT);
            }
            editTextNewComment.setSelection(editTextNewComment.getText().length());
        }
    }

    private void updateCommentVote(String commentId, boolean isUpvoteAttempt, int position) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "You must be logged in to vote.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (postId == null || commentId == null) {
            Log.e(TAG, "Post ID or Comment ID is null, cannot update vote.");
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Error: Missing ID for vote.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = firebaseUser.getUid();
        DocumentReference commentRef = db.collection("posts").document(postId)
                                       .collection("comments").document(commentId);

        db.runTransaction((com.google.firebase.firestore.Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(commentRef);
            Comment currentCommentState = snapshot.toObject(Comment.class);
            if (currentCommentState == null) {
                throw new FirebaseFirestoreException("Comment not found in transaction", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            List<String> upvotedBy = currentCommentState.getUpvotedBy() != null ? new ArrayList<>(currentCommentState.getUpvotedBy()) : new ArrayList<>();
            List<String> downvotedBy = currentCommentState.getDownvotedBy() != null ? new ArrayList<>(currentCommentState.getDownvotedBy()) : new ArrayList<>();

            int upvotes = currentCommentState.getUpvotes();
            int downvotes = currentCommentState.getDownvotes();

            boolean alreadyUpvoted = upvotedBy.contains(currentUserId);
            boolean alreadyDownvoted = downvotedBy.contains(currentUserId);

            if (isUpvoteAttempt) {
                if (alreadyUpvoted) {
                    upvotedBy.remove(currentUserId);
                    upvotes--;
                } else {
                    upvotedBy.add(currentUserId);
                    upvotes++;
                    if (alreadyDownvoted) {
                        downvotedBy.remove(currentUserId);
                        downvotes--;
                    }
                }
            } else {
                if (alreadyDownvoted) {
                    downvotedBy.remove(currentUserId);
                    downvotes--;
                } else {
                    downvotedBy.add(currentUserId);
                    downvotes++;
                    if (alreadyUpvoted) {
                        upvotedBy.remove(currentUserId);
                        upvotes--;
                    }
                }
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("upvotes", upvotes);
            updates.put("downvotes", downvotes);
            updates.put("upvotedBy", upvotedBy);
            updates.put("downvotedBy", downvotedBy);

            transaction.update(commentRef, updates);
            return null;
        })
        .addOnSuccessListener(aVoid -> {
            loadComments();
            if (getContext() != null && isAdded()) Toast.makeText(getContext(), "Vote successful!", Toast.LENGTH_SHORT).show();
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failed for comment " + commentId, e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Vote failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
             loadComments();
        });
    }
}
