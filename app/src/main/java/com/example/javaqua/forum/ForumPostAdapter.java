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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.Atlas;
import com.example.javaqua.atlas.FishPreview;
import com.example.javaqua.data.AppDatabase;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.PostViewHolder> {

    private final List<Post> posts = new ArrayList<>();
    private final Context context;
    private final OnPostInteractionListener listener;
    private final FirebaseAuth mAuth;
    private final Map<String, FishPreview> fishCache = new HashMap<>();
    private final Map<String, CharSequence> spannableCache = new HashMap<>();
    private static final Pattern TAG_PATTERN = Pattern.compile("@([\\w\\s]+?)(?=\\s@|\\n|$)");

    public interface OnPostInteractionListener {
        void onPostClicked(int position);
        void onUpvoteClicked(int position);
        void onDownvoteClicked(int position);
        void onCommentClicked(int position);
        void onRepostClicked(int position);
        void onFishTagClicked(String fishId);
        void onCommentUpvoteClicked(int postPosition, int commentPosition);
    }

    public ForumPostAdapter(Context context, List<Post> initialPosts, OnPostInteractionListener listener, FirebaseAuth auth) {
        this.context = context;
        if (initialPosts != null) this.posts.addAll(initialPosts);
        this.listener = listener;
        this.mAuth = auth;
        preloadFishData();
    }

    private void preloadFishData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Atlas atlas = new Atlas(context);
            List<FishPreview> allFish = atlas.getCachedFishPreviewsList();
            if (allFish != null) {
                for (FishPreview fish : allFish) fishCache.put(fish.getId(), fish);
                spannableCache.clear();
            }
        });
    }

    public void updatePosts(final List<Post> newPosts) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return posts.size(); }
            @Override public int getNewListSize() { return newPosts == null ? 0 : newPosts.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(posts.get(oldPos).getPostId(), newPosts.get(newPos).getPostId());
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                Post oldPost = posts.get(oldPos);
                Post newPost = newPosts.get(newPos);
                return oldPost.getUpvotes() == newPost.getUpvotes() &&
                       oldPost.getDownvotes() == newPost.getDownvotes() &&
                       oldPost.getCommentCount() == newPost.getCommentCount() &&
                       oldPost.isExpanded() == newPost.isExpanded();
            }
        });
        posts.clear();
        if (newPosts != null) posts.addAll(newPosts);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_post_preview, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, postLabelTextView, upvoteCountTextView, downvoteCountTextView, commentCountTextView, textViewPostTimestamp;
        LinearLayout fishTagsLayout, expandedContentPlaceholder;
        ImageButton upvoteButton, downvoteButton, commentButton, repostButton;
        TextView fullTextView;

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
            textViewPostTimestamp = itemView.findViewById(R.id.textViewPostTimestamp);
        }

        void bind(Post post) {
            usernameTextView.setText(post.getUsername());
            postLabelTextView.setText(post.getPostLabel());
            upvoteCountTextView.setText(String.valueOf(post.getUpvotes()));
            downvoteCountTextView.setText(String.valueOf(post.getDownvotes()));
            commentCountTextView.setText(String.valueOf(post.getCommentCount()));

            if (post.getTimestamp() != null) {
                textViewPostTimestamp.setText(TimestampUtils.formatRelativeTimestamp(post.getTimestamp()));
            } else {
                textViewPostTimestamp.setText("No date");
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                if (post.getUpvotedBy() != null && post.getUpvotedBy().contains(uid)) {
                    upvoteButton.setColorFilter(ThemeUtils.getThemeColor(context, R.attr.upvoteColor));
                } else {
                    upvoteButton.clearColorFilter();
                }
                if (post.getDownvotedBy() != null && post.getDownvotedBy().contains(uid)) {
                    downvoteButton.setColorFilter(ThemeUtils.getThemeColor(context, R.attr.downvoteColor));
                } else {
                    downvoteButton.clearColorFilter();
                }
            }

            fishTagsLayout.removeAllViews();
            if (post.getTaggedFishIds() != null && !post.getTaggedFishIds().isEmpty()) {
                fishTagsLayout.setVisibility(View.VISIBLE);
                for (String fid : post.getTaggedFishIds()) {
                    TextView tv = new TextView(context);
                    FishPreview p = fishCache.get(fid);
                    tv.setText("@" + (p != null ? p.getName() : fid));
                    tv.setTextColor(ThemeUtils.getThemeColor(context, R.attr.tagTextColor));
                    tv.setPadding(8, 4, 8, 4);
                    tv.setOnClickListener(v -> listener.onFishTagClicked(fid));
                    fishTagsLayout.addView(tv);
                }
            } else {
                fishTagsLayout.setVisibility(View.GONE);
            }

            if (post.isExpanded()) {
                expandedContentPlaceholder.setVisibility(View.VISIBLE);
                
                CharSequence cached = spannableCache.get(post.getPostId());
                if (cached != null) {
                    fullTextView.setText(cached);
                } else {
                    CharSequence built = buildSpannable(post.getFullText());
                    spannableCache.put(post.getPostId(), built);
                    fullTextView.setText(built);
                }
                fullTextView.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                expandedContentPlaceholder.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onPostClicked(getAdapterPosition()));
            upvoteButton.setOnClickListener(v -> listener.onUpvoteClicked(getAdapterPosition()));
            downvoteButton.setOnClickListener(v -> listener.onDownvoteClicked(getAdapterPosition()));
            commentButton.setOnClickListener(v -> listener.onCommentClicked(getAdapterPosition()));
            repostButton.setOnClickListener(v -> listener.onRepostClicked(getAdapterPosition()));
        }

        private SpannableString buildSpannable(String text) {
            if (TextUtils.isEmpty(text)) return new SpannableString("");
            SpannableString ss = new SpannableString(text);
            Matcher matcher = TAG_PATTERN.matcher(text);
            while (matcher.find()) {
                String mention = matcher.group(1);
                if (mention == null) continue;
                String q = mention.trim().toLowerCase();
                for (FishPreview f : fishCache.values()) {
                    if (f.getName() != null && f.getName().toLowerCase().equals(q)) {
                        int start = matcher.start();
                        int end = matcher.end();
                        ss.setSpan(new ClickableSpan() {
                            @Override public void onClick(@NonNull View w) { listener.onFishTagClicked(f.getId()); }
                        }, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ss.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(context, R.attr.tagTextColor)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                }
            }
            return ss;
        }
    }
}