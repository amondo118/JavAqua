package com.example.javaqua.myaqua;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.FishPreview;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AquariumAdapter extends RecyclerView.Adapter<AquariumAdapter.ViewHolder> {

    private final List<AquariumGridItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AquariumGridItem item);
    }

    public static class AquariumGridItem {
        private final String id;
        private final String name;
        private final String backgroundUrl;
        private final double volumeLiters;
        private final double temperature;
        private final List<FishPreview> fishPreviews;
        private final com.example.javaqua.atlas.SuggestionEngine.Rating status;

        public AquariumGridItem(String id, String name, String backgroundUrl, double volumeLiters, double temperature, List<FishPreview> fishPreviews, com.example.javaqua.atlas.SuggestionEngine.Rating status) {
            this.id = id;
            this.name = name;
            this.backgroundUrl = backgroundUrl;
            this.volumeLiters = volumeLiters;
            this.temperature = temperature;
            this.fishPreviews = fishPreviews;
            this.status = status;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getBackgroundUrl() { return backgroundUrl; }
        public double getVolumeLiters() { return volumeLiters; }
        public double getTemperature() { return temperature; }
        public List<FishPreview> getFishPreviews() { return fishPreviews; }
        public com.example.javaqua.atlas.SuggestionEngine.Rating getStatus() { return status; }
    }

    public AquariumAdapter(List<AquariumGridItem> initialItems, OnItemClickListener listener) {
        if (initialItems != null) this.items.addAll(initialItems);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aquarium_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(final List<AquariumGridItem> newItems) {
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
                AquariumGridItem oldItem = items.get(oldItemPosition);
                AquariumGridItem newItem = newItems.get(newItemPosition);
                return Objects.equals(oldItem.getName(), newItem.getName()) &&
                       Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                       Objects.equals(oldItem.getBackgroundUrl(), newItem.getBackgroundUrl()) &&
                       oldItem.getFishPreviews().size() == newItem.getFishPreviews().size();
            }
        });

        items.clear();
        if (newItems != null) items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AnimatedAquariumView animatedAquariumView;
        TextView textViewAquariumName;
        View statusIndicator;

        public ViewHolder(View itemView) {
            super(itemView);
            animatedAquariumView = itemView.findViewById(R.id.animatedAquariumPreview);
            textViewAquariumName = itemView.findViewById(R.id.textViewAquariumName);
            statusIndicator = itemView.findViewById(R.id.tankStatusIndicator);
        }

        public void bind(final AquariumGridItem item, final OnItemClickListener listener) {
            textViewAquariumName.setText(item.getName());

            if (item.getStatus() != com.example.javaqua.atlas.SuggestionEngine.Rating.UNKNOWN) {
                statusIndicator.setVisibility(View.VISIBLE);
                GradientDrawable drawable = (GradientDrawable) statusIndicator.getBackground();
                switch (item.getStatus()) {
                    case GREEN: drawable.setColor(Color.GREEN); break;
                    case YELLOW: drawable.setColor(Color.YELLOW); break;
                    case RED: drawable.setColor(Color.RED); break;
                }
            } else {
                statusIndicator.setVisibility(View.GONE);
            }

            animatedAquariumView.setAnimationEnabled(false);
            animatedAquariumView.setAquariumData(item.getBackgroundUrl(), item.getFishPreviews());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}