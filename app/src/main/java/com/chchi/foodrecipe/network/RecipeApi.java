package com.chchi.foodrecipe.network;

import com.chchi.foodrecipe.network.responses.RecipeResponse;
import com.chchi.foodrecipe.network.responses.RecipeListSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApi {

    @GET("/api/search")
    Call<RecipeListSearchResponse> searchRecipeList(
            @Query("key") String key,
            @Query("q") String query,
            @Query("page") int page
    );

    @GET("/api/get")
    Call<RecipeResponse> getSingleRecipe(
            @Query("key") String key,
            @Query("rId") String recipe_id
    );
}
