package com.example.javaqua.atlas;

import android.content.Context;
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

import java.util.List;

public class FishAdapterForDetailView extends RecyclerView.Adapter<FishAdapterForDetailView.ViewHolder> {

    private List<FishPreview> fishPreviewList;
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fish_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FishPreview fishPreview = fishPreviewList.get(position);
        holder.bind(fishPreview, position, listener);
    }

    @Override
    public int getItemCount() {
        return fishPreviewList == null ? 0 : fishPreviewList.size();
    }

    public void updateData(List<FishPreview> newFishPreviewList) {
        this.fishPreviewList = newFishPreviewList;
        notifyDataSetChanged();
    }

    public void itemUpdated(int position) {
        notifyItemChanged(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView latinView;
        TextView descriptionView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initializing views from item_fish_preview.xml
            imageView = itemView.findViewById(R.id.imageView);
            nameView = itemView.findViewById(R.id.nameView);
            latinView = itemView.findViewById(R.id.latinView);
            descriptionView = itemView.findViewById(R.id.descriptionView);
        }

        public void bind(final FishPreview fishPreview, final int position, final OnFishItemClickListener listener) {
            nameView.setText(fishPreview.getName());
            latinView.setText(fishPreview.getLatin());

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
                        .load(fishPreview.getImageSML())
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
