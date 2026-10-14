package com.example.javaqua.atlas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.javaqua.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishAdapterForDetailView extends RecyclerView.Adapter<FishAdapterForDetailView.ViewHolder> {

    private List<FishPreview> fishPreviewList;
    private Map<String, Integer> counts = new HashMap<>();
    private OnFishItemClickListener listener;
    private Context context;


    public interface OnFishItemClickListener {
        void onFishItemClick(FishPreview fishPreview, int position);
    }

    public FishAdapterForDetailView(Context context, List<FishPreview> fishPreviewList, OnFishItemClickListener listener) {
        this.context = context;
        this.fishPreviewList = fishPreviewList;
        this.listener = listener;
    }

    public void setCounts(Map<String, Integer> counts) {
        this.counts = counts != null ? counts : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fish_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FishPreview fishPreview = fishPreviewList.get(position);
        int count = counts.getOrDefault(fishPreview.getId(), 1);
        holder.bind(fishPreview, count, position, listener);
    }

    @Override
    public int getItemCount() {
        return fishPreviewList == null ? 0 : fishPreviewList.size();
    }

    public void updateData(List<FishPreview> newFishPreviewList) {
        this.fishPreviewList = newFishPreviewList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView latinView;
        TextView descriptionView;
        View ratingIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameView = itemView.findViewById(R.id.nameView);
            latinView = itemView.findViewById(R.id.latinView);
            descriptionView = itemView.findViewById(R.id.descriptionView);
            ratingIndicator = itemView.findViewById(R.id.ratingIndicator);
        }

        public void bind(final FishPreview fishPreview, int count, final int position, final OnFishItemClickListener listener) {
            String nameText = fishPreview.getName();
            if (count > 1) nameText += " (x" + count + ")";
            nameView.setText(nameText);
            
            latinView.setText(fishPreview.getLatin());

            if (fishPreview.getSuggestionRating() != SuggestionEngine.Rating.UNKNOWN) {
                ratingIndicator.setVisibility(View.VISIBLE);
                GradientDrawable drawable = (GradientDrawable) ratingIndicator.getBackground();
                switch (fishPreview.getSuggestionRating()) {
                    case GREEN: drawable.setColor(Color.GREEN); break;
                    case YELLOW: drawable.setColor(Color.YELLOW); break;
                    case RED: drawable.setColor(Color.RED); break;
                }
            } else {
                ratingIndicator.setVisibility(View.GONE);
            }

            RequestOptions glideOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop();

            if (fishPreview.isExpanded() && fishPreview.getDescription() != null && !fishPreview.getDescription().isEmpty()) {
                descriptionView.setText(fishPreview.getDescription());
                descriptionView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(fishPreview.getImageUrl())
                        .apply(glideOptions)
                        .into(imageView);
            } else {
                descriptionView.setVisibility(View.GONE);
                Glide.with(context)
                        .load(fishPreview.getImageUrl()) // Preview usually wants imageURL or SML
                        .apply(glideOptions)
                        .into(imageView);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFishItemClick(fishPreview, position);
                }
            });
        }
    }
}