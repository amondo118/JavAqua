package com.example.javaqua.atlas;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.javaqua.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FishPreviewAdapter extends RecyclerView.Adapter<FishPreviewAdapter.ViewHolder> {
    private static final String TAG = "FishPreviewAdapter";
    private final List<FishPreview> items = new ArrayList<>();
    private final DetailsLoader detailsLoader;
    private final RequestOptions glideOptions;

    public interface DetailsLoader {
        void loadDetailsFor(FishPreview item, int position);
    }

    public FishPreviewAdapter(DetailsLoader detailsLoader) {
        this.detailsLoader = detailsLoader;
        this.glideOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
    }

    public void submitList(final List<FishPreview> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems == null ? 0 : newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Objects.equals(items.get(oldItemPosition).getId(), newItems.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                FishPreview oldItem = items.get(oldItemPosition);
                FishPreview newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getName(), newItem.getName()) &&
                       Objects.equals(oldItem.getSuggestionRating(), newItem.getSuggestionRating()) &&
                       oldItem.isExpanded() == newItem.isExpanded();
            }
        });

        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fish_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<FishPreview> getCurrentList() {
        return new ArrayList<>(items);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView latinView;
        private final TextView descriptionView;
        private final View ratingIndicator;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameView = itemView.findViewById(R.id.nameView);
            latinView = itemView.findViewById(R.id.latinView);
            descriptionView = itemView.findViewById(R.id.descriptionView);
            ratingIndicator = itemView.findViewById(R.id.ratingIndicator);
        }

        public void bind(final FishPreview item, final int position) {
            if (item == null) return;

            nameView.setText(item.getName());
            latinView.setText(item.getLatin());

            if (item.getSuggestionRating() != SuggestionEngine.Rating.UNKNOWN) {
                ratingIndicator.setVisibility(View.VISIBLE);
                GradientDrawable drawable = (GradientDrawable) ratingIndicator.getBackground();
                switch (item.getSuggestionRating()) {
                    case GREEN: drawable.setColor(Color.GREEN); break;
                    case YELLOW: drawable.setColor(Color.YELLOW); break;
                    case RED: drawable.setColor(Color.RED); break;
                }
            } else {
                ratingIndicator.setVisibility(View.GONE);
            }

            if (item.isExpanded()) {
                descriptionView.setText(item.getDescription());
                descriptionView.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .apply(glideOptions)
                        .into(imageView);
            } else {
                descriptionView.setVisibility(View.GONE);
                Glide.with(itemView.getContext())
                        .load(item.getImageSML())
                        .apply(glideOptions)
                        .into(imageView);
            }

            itemView.setOnClickListener(v -> {
                detailsLoader.loadDetailsFor(item, position);
            });
        }
    }
}