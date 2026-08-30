package com.example.javaqua.atlas;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.javaqua.R;

import java.util.ArrayList;
import java.util.List;

public class FishPreviewAdapter extends RecyclerView.Adapter<FishPreviewAdapter.ViewHolder> {
    private static final String TAG = "FishPreviewAdapter";
    private List<FishPreview> items = new ArrayList<>();
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

    public void submitList(List<FishPreview> newItems) {
        if (newItems != null) {
            items = new ArrayList<>(newItems);
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "submitList called with null items");
            items.clear();
            notifyDataSetChanged();
        }
    }

    public void addItems(List<FishPreview> newItems) {
        if (newItems != null && !newItems.isEmpty()) {
            int startPosition = items.size();
            items.addAll(newItems);
            notifyItemRangeInserted(startPosition, newItems.size());
        }
    }

    public void itemUpdated(int position) {
        notifyItemChanged(position);
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
        FishPreview item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<FishPreview> getCurrentList() {
        return items;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView latinView;
        private final TextView descriptionView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameView = itemView.findViewById(R.id.nameView);
            latinView = itemView.findViewById(R.id.latinView);
            descriptionView = itemView.findViewById(R.id.descriptionView);
        }

        public void bind(final FishPreview item, final int position) {
            if (item == null) return;

            nameView.setText(item.getName());
            latinView.setText(item.getLatin());

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
