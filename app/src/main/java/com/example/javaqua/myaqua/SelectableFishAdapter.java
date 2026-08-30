package com.example.javaqua.myaqua;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javaqua.R;
import com.example.javaqua.atlas.FishPreview;

import java.util.ArrayList;
import java.util.List;

public class SelectableFishAdapter extends RecyclerView.Adapter<SelectableFishAdapter.ViewHolder> {

    private List<FishPreview> fishList;
    private ArrayList<String> selectedFishIds = new ArrayList<>();

    public SelectableFishAdapter(List<FishPreview> fishList) {
        this.fishList = (fishList == null) ? new ArrayList<>() : fishList;
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
        if (fish == null) return;

        holder.textViewFishName.setText(fish.getName());
        holder.textViewFishLatinName.setText(fish.getLatin());

        // Set checkbox state
        holder.checkBoxSelected.setChecked(selectedFishIds.contains(fish.getId()));

        holder.checkBoxSelected.setOnClickListener(view -> {
            if (holder.checkBoxSelected.isChecked()) {
                if (!selectedFishIds.contains(fish.getId())) {
                    selectedFishIds.add(fish.getId());
                }
            } else {
                selectedFishIds.remove(fish.getId());
            }
        });


        holder.itemView.setOnClickListener(v -> {
            holder.checkBoxSelected.toggle();

        });
    }

    @Override
    public int getItemCount() {
        return fishList.size();
    }

    public ArrayList<String> getSelectedFishIds() {
        return selectedFishIds;
    }

    public void submitList(List<FishPreview> newFishList) {
        this.fishList = (newFishList == null) ? new ArrayList<>() : newFishList;

        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxSelected;
        TextView textViewFishName;
        TextView textViewFishLatinName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxSelected = itemView.findViewById(R.id.checkBoxFishSelected);
            textViewFishName = itemView.findViewById(R.id.textViewFishNameSelectable);
            textViewFishLatinName = itemView.findViewById(R.id.textViewFishLatinNameSelectable);
        }
    }
}
