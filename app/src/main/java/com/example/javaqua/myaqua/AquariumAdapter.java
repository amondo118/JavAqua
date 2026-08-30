package com.example.javaqua.myaqua;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.FishPreview;

import java.util.ArrayList;
import java.util.List;

public class AquariumAdapter extends RecyclerView.Adapter<AquariumAdapter.ViewHolder> {

    private List<AquariumGridItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AquariumGridItem item);
    }

    public static class AquariumGridItem {
        private final String id;
        private final String name;
        private final String backgroundUrl;       // Added
        private final List<FishPreview> fishPreviews; // Added

        public AquariumGridItem(String id, String name, String backgroundUrl, List<FishPreview> fishPreviews) {
            this.id = id;
            this.name = name;
            this.backgroundUrl = backgroundUrl;
            this.fishPreviews = fishPreviews;
        }

        public String getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public String getBackgroundUrl() { return backgroundUrl; } // Added
        public List<FishPreview> getFishPreviews() { return fishPreviews; } // Added
    }

    public AquariumAdapter(List<AquariumGridItem> items, OnItemClickListener listener) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
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
        AquariumGridItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<AquariumGridItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged(); // Consider using DiffUtil for better performance if this is a ListAdapter
    }

    // Removed explicit calls from Adapter's ViewHolder to AnimatedAquariumView's
    // start/stop/cleanup as AnimatedAquariumView handles its own lifecycle
    // via its onAttachedToWindow/onDetachedFromWindow methods.

    // @Override
    // public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
    //     super.onViewAttachedToWindow(holder);
    //     // holder.onViewAttachedToWindow(); // No longer needed here
    // }

    // @Override
    // public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
    //     super.onViewDetachedFromWindow(holder);
    //     // holder.onViewDetachedFromWindow(); // No longer needed here
    // }

    static class ViewHolder extends RecyclerView.ViewHolder {
        AnimatedAquariumView animatedAquariumView;
        TextView textViewAquariumName;

        public ViewHolder(View itemView) {
            super(itemView);
            animatedAquariumView = itemView.findViewById(R.id.animatedAquariumPreview);
            textViewAquariumName = itemView.findViewById(R.id.textViewAquariumName);
        }

        public void bind(final AquariumGridItem item, final OnItemClickListener listener) {
            textViewAquariumName.setText(item.getName());

            animatedAquariumView.setAquariumData(item.getBackgroundUrl(), item.getFishPreviews());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

    }
}
