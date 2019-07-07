package com.chchi.foodrecipe.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chchi.foodrecipe.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    onRecipeClickListener recipeClickListener;

    CircleImageView circleImageView;
    TextView textView;

    public CategoryViewHolder(@NonNull View itemView, onRecipeClickListener recipeClickListener) {
        super(itemView);
        this.recipeClickListener = recipeClickListener;
        circleImageView = itemView.findViewById(R.id.category_image);
        textView = itemView.findViewById(R.id.category_title);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipeClickListener.onCategoryClick(textView.getText().toString());
            }
        });
    }
}
