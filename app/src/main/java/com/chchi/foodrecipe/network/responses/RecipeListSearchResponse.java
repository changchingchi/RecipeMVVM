package com.chchi.foodrecipe.network.responses;

import com.chchi.foodrecipe.models.Recipe;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *
 *  data modeling from network response, we knew when we are making request,
 *  the key we want is "count" and "recipes"
 *  count is a integer
 *  recipes is a list of recipe.
 *
 */


public class RecipeListSearchResponse {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("recipes")
    @Expose
    private List<Recipe> recipes;

    @Override
    public String toString() {
        return "RecipeListSearchResponse{" +
                "count=" + count +
                ", recipes=" + recipes +
                '}';
    }

    public int getCount() {
        return count;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

}
