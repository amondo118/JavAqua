package com.example.javaqua.forum;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.main.ThemeUtils;
import com.example.javaqua.main.TimestampUtils;
import com.example.javaqua.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private OnPostInteractionListener listener;
    private FirebaseAuth mAuth;
    private static final String TAG = "ForumPostAdapter";
    private final Atlas atlas;
    private Map<String, FishPreview> fishCache = new HashMap<>();

    public interface OnPostInteractionListener {
        void onPostClicked(int position);
        void onUpvoteClicked(int position);
        void onDownvoteClicked(int position);
        void onCommentClicked(int position);
        void onRepostClicked(int position);
        void onFishTagClicked(String fishId);
        void onCommentUpvoteClicked(int postPosition, int commentPosition);
    }

    public ForumPostAdapter(Context context, List<Post> posts, OnPostInteractionListener listener, FirebaseAuth auth) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
        this.mAuth = auth;
        this.atlas = new Atlas(context);
        preloadFishData();
    }

    private void preloadFishData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FishPreview> allFish = atlas.getCachedFishPreviewsList();
            if (allFish != null) {
                Map<String, FishPreview> tempMap = new HashMap<>();
                for (FishPreview fish : allFish) {
                    tempMap.put(fish.getId(), fish);
                }
                fishCache = tempMap;
                // If we want to refresh UI once cache is ready:
                // ((Activity)context).runOnUiThread(this::notifyDataSetChanged);
            }
        });
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_post_preview, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(holder.getAdapterPosition());

        holder.usernameTextView.setText(post.getUsername());
        holder.postLabelTextView.setText(post.getPostLabel());
        holder.upvoteCountTextView.setText(String.valueOf(post.getUpvotes()));
        holder.downvoteCountTextView.setText(String.valueOf(post.getDownvotes()));
        holder.commentCountTextView.setText(String.valueOf(post.getCommentCount()));

        if (post.getTimestamp() != null) {
            holder.textViewPostTimestamp.setText(TimestampUtils.formatRelativeTimestamp(post.getTimestamp()));
            holder.textViewPostTimestamp.setVisibility(View.VISIBLE);
        } else {
            holder.textViewPostTimestamp.setText("No date");
            holder.textViewPostTimestamp.setVisibility(View.VISIBLE);
        }

        // Update Vote Button Tints
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            List<String> upvotedBy = post.getUpvotedBy() != null ? post.getUpvotedBy() : new ArrayList<>();
            List<String> downvotedBy = post.getDownvotedBy() != null ? post.getDownvotedBy() : new ArrayList<>();

            if (upvotedBy.contains(currentUserId)) {
                holder.upvoteButton.setColorFilter(ThemeUtils.getThemeColor(context, R.attr.upvoteColor));
            } else {
                holder.upvoteButton.clearColorFilter();
            }

            if (downvotedBy.contains(currentUserId)) {
                holder.downvoteButton.setColorFilter(ThemeUtils.getThemeColor(context, R.attr.downvoteColor));
            } else {
                holder.downvoteButton.clearColorFilter();
            }
        } else {
            holder.upvoteButton.clearColorFilter();
            holder.downvoteButton.clearColorFilter();
        }

        // Fish Tags
        holder.fishTagsLayout.removeAllViews();
        if (post.getTaggedFishIds() != null && !post.getTaggedFishIds().isEmpty()) {
            holder.fishTagsLayout.setVisibility(View.VISIBLE);
            for (String fishId : post.getTaggedFishIds()) {
                TextView tagView = new TextView(context);
                FishPreview preview = fishCache.get(fishId);
                if (preview != null && preview.getName() != null) {
                    tagView.setText("@" + preview.getName());
                } else {
                    tagView.setText("@" + fishId);
                }
                tagView.setBackgroundResource(android.R.color.transparent);
                tagView.setTextColor(ThemeUtils.getThemeColor(context, R.attr.tagTextColor));
                tagView.setPadding(12, 6, 12, 6);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 8, 0);
                tagView.setLayoutParams(params);
                tagView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFishTagClicked(fishId);
                    }
                });
                holder.fishTagsLayout.addView(tagView);
            }
        } else {
            holder.fishTagsLayout.setVisibility(View.GONE);
        }

        if (post.isExpanded()) {
            holder.expandedContentPlaceholder.setVisibility(View.VISIBLE);
            String fullText = post.getFullText();

            if (!TextUtils.isEmpty(fullText) && context != null) {
                SpannableString spannableString = new SpannableString(fullText);
                List<FishPreview> sortedFishPreviews = new ArrayList<>(fishCache.values());
                
                if (!sortedFishPreviews.isEmpty()) {
                    Collections.sort(sortedFishPreviews, (f1, f2) -> {
                        String name1 = f1.getName();
                        String name2 = f2.getName();
                        if (name1 == null && name2 == null) return 0;
                        if (name1 == null) return 1;
                        if (name2 == null) return -1;
                        return Integer.compare(name2.length(), name1.length());
                    });

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
                                                if (listener != null) {
                                                    listener.onFishTagClicked(clickedFishId);
                                                }
                                            }
                                        };

                                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ThemeUtils.getThemeColor(context, R.attr.tagTextColor));
                                        int spanStart = matcher.start(0);
                                        int spanEnd = spanStart + "@".length() + actualFishName.length();

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
                }
                holder.fullTextView.setText(spannableString);
                holder.fullTextView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                holder.fullTextView.setText(fullText);
            }
        } else {
            holder.expandedContentPlaceholder.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPostClicked(holder.getAdapterPosition());
        });

        holder.upvoteButton.setOnClickListener(v -> {
            if (listener != null) listener.onUpvoteClicked(holder.getAdapterPosition());
        });
        holder.downvoteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDownvoteClicked(holder.getAdapterPosition());
        });
        holder.commentButton.setOnClickListener(v -> {
            if (listener != null) listener.onCommentClicked(holder.getAdapterPosition());
        });
        holder.repostButton.setOnClickListener(v -> {
            if (listener != null) listener.onRepostClicked(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    public List<Post> getPosts() {
        return posts;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, postLabelTextView, upvoteCountTextView, downvoteCountTextView, commentCountTextView, textViewPostTimestamp;
        LinearLayout fishTagsLayout, expandedContentPlaceholder;
        ImageButton upvoteButton, downvoteButton, commentButton, repostButton;
        TextView fullTextView;
        RecyclerView commentsRecyclerView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textViewPostUsername);
            postLabelTextView = itemView.findViewById(R.id.textViewPostLabel);
            upvoteCountTextView = itemView.findViewById(R.id.textViewUpvoteCount);
            downvoteCountTextView = itemView.findViewById(R.id.textViewDownvoteCount);
            commentCountTextView = itemView.findViewById(R.id.textViewCommentCount);
            upvoteButton = itemView.findViewById(R.id.buttonUpvotePost);
            downvoteButton = itemView.findViewById(R.id.buttonDownvotePost);
            commentButton = itemView.findViewById(R.id.buttonCommentPost);
            repostButton = itemView.findViewById(R.id.buttonRepostPost);
            fishTagsLayout = itemView.findViewById(R.id.fishTagsLayout);
            expandedContentPlaceholder = itemView.findViewById(R.id.expandedContentPlaceholder);
            fullTextView = itemView.findViewById(R.id.fullTextView);
            commentsRecyclerView = itemView.findViewById(R.id.recyclerViewComments);
            textViewPostTimestamp = itemView.findViewById(R.id.textViewPostTimestamp);
        }
    }
}
