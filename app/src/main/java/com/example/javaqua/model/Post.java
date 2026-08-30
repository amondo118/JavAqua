package com.example.javaqua.model;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {
    private String postId;
    private String userId;
    private String username;
    private String postLabel;
    private String fullText;
    private List<String> taggedFishIds;
    private Date timestamp;
    private int upvotes;
    private int downvotes;
    private int commentCount;
    private List<String> upvotedBy;
    private List<String> downvotedBy;
    @Exclude
    private List<Comment> comments;
    private String linkedPostId;
    @Exclude
    private boolean isExpanded;

    public Post() {
        this.isExpanded = false;
        this.comments = new ArrayList<>();
        this.commentCount = 0;
        this.upvotedBy = new ArrayList<>();
        this.downvotedBy = new ArrayList<>();
    }

    public Post(String userId, String username, String postLabel, String fullText, List<String> taggedFishIds, String linkedPostId) {
        this.userId = userId;
        this.username = username;
        this.postLabel = postLabel;
        this.fullText = fullText;
        this.taggedFishIds = taggedFishIds;
        this.linkedPostId = linkedPostId;
        this.timestamp = null;
        this.upvotes = 0;
        this.downvotes = 0;
        this.commentCount = 0;
        this.isExpanded = false;
        this.comments = new ArrayList<>();
        this.upvotedBy = new ArrayList<>();
        this.downvotedBy = new ArrayList<>();
    }

    @Exclude
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPostLabel() { return postLabel; }
    public void setPostLabel(String postLabel) { this.postLabel = postLabel; }
    public String getFullText() { return fullText; }
    public void setFullText(String fullText) { this.fullText = fullText; }
    public List<String> getTaggedFishIds() { return taggedFishIds; }
    public void setTaggedFishIds(List<String> taggedFishIds) { this.taggedFishIds = taggedFishIds; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public List<String> getUpvotedBy() { return upvotedBy; }
    public void setUpvotedBy(List<String> upvotedBy) { this.upvotedBy = upvotedBy; }
    public List<String> getDownvotedBy() { return downvotedBy; }
    public void setDownvotedBy(List<String> downvotedBy) { this.downvotedBy = downvotedBy; }
    @Exclude
    public List<Comment> getComments() { return comments; }
    @Exclude
    public void setComments(List<Comment> comments) { this.comments = comments; }
    public String getLinkedPostId() { return linkedPostId; }
    public void setLinkedPostId(String linkedPostId) { this.linkedPostId = linkedPostId; }
    @Exclude
    public boolean isExpanded() { return isExpanded; }
    @Exclude
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
