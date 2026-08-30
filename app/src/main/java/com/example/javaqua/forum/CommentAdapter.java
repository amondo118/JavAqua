package com.example.javaqua.forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.main.ThemeUtils;
import com.example.javaqua.main.TimestampUtils;
import com.example.javaqua.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private Context context;
    private CommentItemListener itemListener;
    private FirebaseAuth mAuth; 

    public interface CommentItemListener {
        void onCommentUpvoteClicked(int position);
        void onCommentDownvoteClicked(int position);
        void onCommentReplyClicked(int position);
    }

    public CommentAdapter(Context context, List<Comment> comments, CommentItemListener itemListener, FirebaseAuth auth) {
        this.context = context;
        this.comments = comments;
        this.itemListener = itemListener;
        this.mAuth = auth; 
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(holder.getAdapterPosition());

        holder.usernameTextView.setText(comment.getUsername());
        holder.commentTextView.setText(comment.getText());

        if (comment.getTimestamp() != null) {
            holder.timestampTextView.setText(TimestampUtils.formatRelativeTimestamp(comment.getTimestamp())); // Changed to use TimestampUtils
        } else {
            holder.timestampTextView.setText("No timestamp");
        }

        holder.upvoteCountTextView.setText(String.valueOf(comment.getUpvotes()));
        holder.downvoteCountTextView.setText(String.valueOf(comment.getDownvotes()));

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            List<String> upvotedBy = comment.getUpvotedBy() != null ? comment.getUpvotedBy() : new ArrayList<>();
            List<String> downvotedBy = comment.getDownvotedBy() != null ? comment.getDownvotedBy() : new ArrayList<>();

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

        holder.upvoteButton.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onCommentUpvoteClicked(holder.getAdapterPosition());
            }
        });

        holder.downvoteButton.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onCommentDownvoteClicked(holder.getAdapterPosition());
            }
        });

        holder.replyButton.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onCommentReplyClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
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
    }
}
