package com.example.javaqua.myaqua;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.javaqua.R;
import com.example.javaqua.atlas.FishPreview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedFishAdapter extends RecyclerView.Adapter<SelectedFishAdapter.ViewHolder> {

    private final List<FishPreview> fishList = new ArrayList<>();
    private final Map<String, Integer> counts = new HashMap<>();
    private final OnCountChangeListener listener;

    public interface OnCountChangeListener {
        void onCountChanged(String fishId, int newCount);
    }

    public SelectedFishAdapter(OnCountChangeListener listener) {
        this.listener = listener;
    }

    public void setData(List<FishPreview> newList, Map<String, Integer> newCounts) {
        fishList.clear();
        if (newList != null) fishList.addAll(newList);
        counts.clear();
        if (newCounts != null) counts.putAll(newCounts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_fish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FishPreview fish = fishList.get(position);
        int count = counts.getOrDefault(fish.getId(), 1);
        holder.bind(fish, count);
    }

    @Override
    public int getItemCount() {
        return fishList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, latin, countTxt;
        ImageButton btnPlus, btnMinus;

        ViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.imageViewFish);
            name = v.findViewById(R.id.textViewFishName);
            latin = v.findViewById(R.id.textViewFishLatin);
            countTxt = v.findViewById(R.id.textViewCount);
            btnPlus = v.findViewById(R.id.buttonIncrement);
            btnMinus = v.findViewById(R.id.buttonDecrement);
        }

        void bind(FishPreview fish, int count) {
            name.setText(fish.getName());
            latin.setText(fish.getLatin());
            countTxt.setText(String.valueOf(count));
            Glide.with(itemView).load(fish.getImageUrl()).into(img);

            btnPlus.setOnClickListener(v -> listener.onCountChanged(fish.getId(), count + 1));
            btnMinus.setOnClickListener(v -> listener.onCountChanged(fish.getId(), count - 1));
        }
    }
}