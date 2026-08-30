package com.example.javaqua.model;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String username;
    private String text;
    private Date timestamp;
    private int upvotes;
    private int downvotes;
    private List<String> upvotedBy;
    private List<String> downvotedBy;

    public Comment() {
        this.upvotes = 0;
        this.downvotes = 0;
        this.upvotedBy = new ArrayList<>();
        this.downvotedBy = new ArrayList<>();
    }

    public Comment(String postId, String userId, String username, String text) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = null;
        this.upvotes = 0;
        this.downvotes = 0;
        this.upvotedBy = new ArrayList<>();
        this.downvotedBy = new ArrayList<>();
    }

    @Exclude
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
    public List<String> getUpvotedBy() { return upvotedBy; }
    public void setUpvotedBy(List<String> upvotedBy) { this.upvotedBy = upvotedBy; }
    public List<String> getDownvotedBy() { return downvotedBy; }
    public void setDownvotedBy(List<String> downvotedBy) { this.downvotedBy = downvotedBy; }
}
