package com.example.javaqua.myaqua;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.javaqua.R;
import com.example.javaqua.atlas.FishPreview;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectableFishAdapter extends RecyclerView.Adapter<SelectableFishAdapter.ViewHolder> {

    private final List<FishPreview> fishList = new ArrayList<>();
    private final List<String> selectedFishIds = new ArrayList<>();

    public SelectableFishAdapter() {
    }

    public void setSelectedFishIds(List<String> ids) {
        this.selectedFishIds.clear();
        if (ids != null) this.selectedFishIds.addAll(ids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selectable_fish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FishPreview fish = fishList.get(position);
        holder.bind(fish);
    }

    @Override
    public int getItemCount() {
        return fishList.size();
    }

    public ArrayList<String> getSelectedFishIds() {
        return new ArrayList<>(selectedFishIds);
    }

    public void submitList(final List<FishPreview> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return fishList.size(); }
            @Override public int getNewListSize() { return newList == null ? 0 : newList.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(fishList.get(oldPos).getId(), newList.get(newPos).getId());
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                FishPreview oldItem = fishList.get(oldPos);
                FishPreview newItem = newList.get(newPos);
                return Objects.equals(oldItem.getName(), newItem.getName()) &&
                       Objects.equals(oldItem.getLatin(), newItem.getLatin());
            }
        });
        fishList.clear();
        if (newList != null) fishList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxSelected;
        TextView textViewFishName;
        TextView textViewFishLatinName;
        ImageView imageViewFish;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxSelected = itemView.findViewById(R.id.checkBoxFishSelected);
            textViewFishName = itemView.findViewById(R.id.textViewFishNameSelectable);
            textViewFishLatinName = itemView.findViewById(R.id.textViewFishLatinNameSelectable);
            imageViewFish = itemView.findViewById(R.id.imageViewFish);
        }

        void bind(FishPreview fish) {
            textViewFishName.setText(fish.getName());
            textViewFishLatinName.setText(fish.getLatin());
            Glide.with(itemView).load(fish.getImageUrl()).into(imageViewFish);
            checkBoxSelected.setChecked(selectedFishIds.contains(fish.getId()));

            itemView.setOnClickListener(v -> {
                if (selectedFishIds.contains(fish.getId())) {
                    selectedFishIds.remove(fish.getId());
                } else {
                    selectedFishIds.add(fish.getId());
                }
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}