package com.example.javaqua.atlas;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.javaqua.R;
import com.example.javaqua.model.Fish;

public class FishDetailDialog extends Dialog {
    public FishDetailDialog(@NonNull Context context, Fish details) {
        super(context);
        setContentView(R.layout.dialog_fish_details);

        TextView nameView = findViewById(R.id.nameView);
        TextView latinView = findViewById(R.id.latinView2);
        TextView descriptionView = findViewById(R.id.descriptionView);
        ImageView imageView = findViewById(R.id.imageView);


        nameView.setText(details.getName());
        latinView.setText(details.getLatin());
        descriptionView.setText(details.getDescription());

        Glide.with(context)
                .load(details.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView);
    }
}
