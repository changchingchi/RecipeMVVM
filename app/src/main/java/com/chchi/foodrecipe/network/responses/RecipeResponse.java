package com.chchi.foodrecipe.network.responses;


import com.chchi.foodrecipe.models.Recipe;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This is for Retrofit to do the type safe and convert it to JAVA POJO from network responses.
 */
public class RecipeResponse {

    @SerializedName("recipe")
    @Expose
    private Recipe recipe;

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public String toString() {
        return "RecipeResponse{" +
                "recipe=" + recipe +
                '}';
    }
}
