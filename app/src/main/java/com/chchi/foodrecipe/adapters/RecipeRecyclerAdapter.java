package com.chchi.foodrecipe.adapters;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chchi.foodrecipe.R;
import com.chchi.foodrecipe.models.Recipe;
import com.chchi.foodrecipe.utils.Constants;

import java.util.ArrayList;
import java.util.List;

//the reason we extends RecycleView.ViewHolder instead of our own ViewHolder here is that
//we are using multiple viewholder, so we extends parent class so that we pass  subclass
public class RecipeRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int RECIPE_TYPE = 1;
    public static final int LOADING_TYPE = 2;
    public static final int CATEGORY_TPYE = 3;
    public static final int EXHAUSTED_TYPE = 4;

    private static final String LOADING = "LOADING";
    private static final String EXHAUSTED = "EXHAUSTED";

    private List<Recipe> recipes;
    private onRecipeClickListener recipeClickListener;

    public RecipeRecyclerAdapter(onRecipeClickListener recipeClickListener) {
        this.recipeClickListener = recipeClickListener;
        this.recipes = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // in here we use a switch statment to switch the content of recycler view based on the type. USING the same container.

        View view;

        switch (viewType) {
            case RECIPE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
                return new RecipeViewHolder(view, this.recipeClickListener);
            case LOADING_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_list, parent, false);
                return new LoadingViewHolder(view);
            case CATEGORY_TPYE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_category_list, parent, false);
                return new CategoryViewHolder(view, this.recipeClickListener);
            case EXHAUSTED_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_exhausted_list, parent, false);
                return new SearchExhaustedViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
                return new RecipeViewHolder(view, this.recipeClickListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //we check the type by checking if recipes contains a dummy object we "inject" before network call response.
        if (recipes.get(position).getSocial_rank() == -1) {
            //we simply use social_rank as flag to indicate.
            return CATEGORY_TPYE;
        } else if (recipes.get(position).getTitle().equals(LOADING)) {
            //we simply use some dummy title to indicate loading.
            return LOADING_TYPE;
        } else if (recipes.get(position).getTitle().equals(EXHAUSTED)) {
            //we simply use some dummy title to indicate loading.
            return EXHAUSTED_TYPE;
        } else if (position == recipes.size() - 1
                && position != 0
                && !recipes.get(position).getTitle().equals(EXHAUSTED)) {
            //we simply use some dummy title to indicate loading.
            return LOADING_TYPE;
        } else {
            return RECIPE_TYPE;
        }
    }


    public void setQueryExhausted(){
        hideLoading();
        Recipe exhausted = new Recipe();
        exhausted.setTitle(EXHAUSTED);
        recipes.add(exhausted);
        notifyDataSetChanged();
    }

    private void hideLoading(){
        if(isLoading()){
            for(Recipe r : recipes){
                if(r.getTitle().equals(EXHAUSTED)){
                    recipes.remove(r);
                }
            }
        }
    }


    public void displayLoading() {
        if (!isLoading()) {
            Recipe recipe = new Recipe();
            recipe.setTitle(LOADING);
            List<Recipe> loadingList = new ArrayList<>();
            loadingList.add(recipe);
            this.recipes = loadingList;
            notifyDataSetChanged();
        }
    }

    private boolean isLoading() {
        if (recipes != null && recipes.size() > 0) {
            if (recipes.get(recipes.size() - 1).getTitle().equals(LOADING)) {
                return true;
            }
        }
        return false;
    }


    public void displaySearchCateories() {
        List<Recipe> categoris = new ArrayList<>();
        for (int i = 0; i < Constants.DEFAULT_SEARCH_CATEGORIES.length; i++) {
            Recipe recipe = new Recipe();
            recipe.setTitle(Constants.DEFAULT_SEARCH_CATEGORIES[i]);
            recipe.setImage_url(Constants.DEFAULT_SEARCH_CATEGORY_IMAGES[i]);
            recipe.setSocial_rank(-1);
            categoris.add(recipe);
        }
        this.recipes = categoris;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int itemViewType = getItemViewType(position);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        if (itemViewType == CATEGORY_TPYE) {

            Uri path = Uri.parse("android.resource://com.chchi.foodrecipe/drawable/" + recipes.get(position).getImage_url());

            Glide.with(holder.itemView.getContext())
                    .setDefaultRequestOptions(requestOptions)
                    .load(path)
                    .into(((CategoryViewHolder) holder).circleImageView);

            //cast to subclass since we use super class here.
            ((CategoryViewHolder) holder).textView.setText(recipes.get(position).getTitle());

        } else if (itemViewType == RECIPE_TYPE) {
            Glide.with(holder.itemView.getContext())
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipes.get(position).getImage_url())
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((RecipeViewHolder) holder).progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((RecipeViewHolder) holder).progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(((RecipeViewHolder) holder).imageView);

            //cast to subclass since we use super class here.
            ((RecipeViewHolder) holder).title.setText(recipes.get(position).getTitle());
            ((RecipeViewHolder) holder).publisher.setText(recipes.get(position).getPublisher());
            ((RecipeViewHolder) holder).socialScore.setText(String.valueOf(Math.round(recipes.get(position).getSocial_rank())));
        }
    }

    @Override
    public int getItemCount() {
        return recipes == null ? 0 : recipes.size();
    }

    //someone is going to set receipe to adaptor once data is available so that it will update UI

    //data source -> adaptor -> UI
    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    public Recipe getRecipeByPosition(int position) {
        if(this.recipes!=null && this.recipes.size()>0){
            return this.recipes.get(position);
        }
        return null;
    }
}
