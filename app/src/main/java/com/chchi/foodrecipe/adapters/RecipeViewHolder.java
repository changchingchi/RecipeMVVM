package com.chchi.foodrecipe.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chchi.foodrecipe.R;

public class RecipeViewHolder extends RecyclerView.ViewHolder {

    TextView title, publisher, socialScore;
    ImageView imageView;
    onRecipeClickListener recipeClickListener;
    ProgressBar progressBar;

    public RecipeViewHolder(@NonNull View itemView, onRecipeClickListener recipeClickListener) {
        super(itemView);

        this.recipeClickListener = recipeClickListener;
        this.title = itemView.findViewById(R.id.recipe_title);
        this.publisher = itemView.findViewById(R.id.recipe_publisher);
        this.socialScore = itemView.findViewById(R.id.recipe_social_score);
        this.imageView = itemView.findViewById(R.id.recipe_image);
        this.progressBar = itemView.findViewById(R.id.item_progress_bar);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipeClickListener.onRecipeClick(getAdapterPosition());
            }
        });
    }
}
