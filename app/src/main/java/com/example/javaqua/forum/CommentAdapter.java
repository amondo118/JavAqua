package com.example.javaqua.forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.main.ThemeUtils;
import com.example.javaqua.main.TimestampUtils;
import com.example.javaqua.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments = new ArrayList<>();
    private final Context context;
    private final CommentItemListener itemListener;
    private final FirebaseAuth mAuth; 

    public interface CommentItemListener {
        void onCommentUpvoteClicked(int position);
        void onCommentDownvoteClicked(int position);
        void onCommentReplyClicked(int position);
    }

    public CommentAdapter(Context context, List<Comment> initialComments, CommentItemListener itemListener, FirebaseAuth auth) {
        this.context = context;
        if (initialComments != null) this.comments.addAll(initialComments);
        this.itemListener = itemListener;
        this.mAuth = auth; 
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(final List<Comment> newComments) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return comments.size(); }
            @Override public int getNewListSize() { return newComments == null ? 0 : newComments.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(comments.get(oldPos).getCommentId(), newComments.get(newPos).getCommentId());
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                Comment oldItem = comments.get(oldPos);
                Comment newItem = newComments.get(newPos);
                return oldItem.getUpvotes() == newItem.getUpvotes() &&
                       oldItem.getDownvotes() == newItem.getDownvotes() &&
                       Objects.equals(oldItem.getText(), newItem.getText());
            }
        });
        comments.clear();
        if (newComments != null) comments.addAll(newComments);
        diffResult.dispatchUpdatesTo(this);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, commentTextView, timestampTextView, upvoteCountTextView, downvoteCountTextView;
        ImageButton upvoteButton, downvoteButton, replyButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.textViewCommentUsername);
            commentTextView = itemView.findViewById(R.id.textViewCommentText);
            timestampTextView = itemView.findViewById(R.id.textViewCommentTimestamp);
            upvoteButton = itemView.findViewById(R.id.buttonCommentUpvote);
            upvoteCountTextView = itemView.findViewById(R.id.textViewCommentUpvoteCount);
            downvoteButton = itemView.findViewById(R.id.buttonCommentDownvote);
            downvoteCountTextView = itemView.findViewById(R.id.textViewCommentDownvoteCount);
            replyButton = itemView.findViewById(R.id.buttonCommentReply);
        }

        void bind(Comment comment) {
            usernameTextView.setText(comment.getUsername());
            commentTextView.setText(comment.getText());

            if (comment.getTimestamp() != null) {
                timestampTextView.setText(TimestampUtils.formatRelativeTimestamp(comment.getTimestamp()));
            } else {
                timestampTextView.setText("No timestamp");
            }

            upvoteCountTextView.setText(String.valueOf(comment.getUpvotes()));
            downvoteCountTextView.setText(String.valueOf(comment.getDownvotes()));

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                if (comment.getUpvotedBy() != null && comment.getUpvotedBy().contains(uid)) {
                    upvoteButton.setColorFilter(ThemeUtils.getThemeColor(context, R.attr.upvoteColor));
                } else {
                    upvoteButton.clearColorFilter();
                }
                if (comment.getDownvotedBy() != null && comment.getDownvotedBy().contains(uid)) {
                    downvoteButton.setColorFilter(ThemeUtils.getThemeColor(context, R.attr.downvoteColor));
                } else {
                    downvoteButton.clearColorFilter();
                }
            }

            upvoteButton.setOnClickListener(v -> itemListener.onCommentUpvoteClicked(getAdapterPosition()));
            downvoteButton.setOnClickListener(v -> itemListener.onCommentDownvoteClicked(getAdapterPosition()));
            replyButton.setOnClickListener(v -> itemListener.onCommentReplyClicked(getAdapterPosition()));
        }
    }
}