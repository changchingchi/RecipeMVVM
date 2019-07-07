package com.chchi.foodrecipe.network;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.chchi.foodrecipe.AppExecutors;
import com.chchi.foodrecipe.models.Recipe;
import com.chchi.foodrecipe.network.responses.RecipeListSearchResponse;
import com.chchi.foodrecipe.network.responses.RecipeResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static com.chchi.foodrecipe.utils.Constants.API_KEY;
import static com.chchi.foodrecipe.utils.Constants.NETWORK_TIMEOUT;

public class RecipeApiClient {

    private static final String TAG = "RecipeApiClient";
    private static RecipeApiClient instance;

    private static MutableLiveData<List<Recipe>> recipes;

    private static MutableLiveData<Recipe> recipe;

    private static MutableLiveData<Boolean> isRecipeRequestTimeout;

    private RetrieveRecipeListRunnable retrieveRecipeListRunnable;

    private RetrieveSingleRecipeRunnable retrieveSingleRecipeRunnable;

    private RecipeApiClient() {
        recipes = new MutableLiveData<>();
        recipe = new MutableLiveData<>();
        isRecipeRequestTimeout = new MutableLiveData<>();
    }

    public static RecipeApiClient getInstance() {
        if (instance == null) {
            instance = new RecipeApiClient();
        }
        return instance;
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public LiveData<Recipe> getSingleRecipe() {
        return recipe;
    }

    public LiveData<Boolean> isRecipeRequestTimeout() {
        return isRecipeRequestTimeout;
    }

    public void searchRecipesApi(String query, int pageNumber) {
        //check if this runnable executed before, if so, remove them.
        if (retrieveRecipeListRunnable != null) {
            retrieveRecipeListRunnable = null;
        }
        retrieveRecipeListRunnable = new RetrieveRecipeListRunnable(query, pageNumber);

        final Future handler = AppExecutors.getInstance().networkIO().submit(retrieveRecipeListRunnable);

        AppExecutors.getInstance().networkIO().schedule(() -> {

            //let user know time out.
            handler.cancel(true);
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void searchSingleRecipesApi(String recipeId) {
        if (retrieveSingleRecipeRunnable != null) {
            retrieveSingleRecipeRunnable = null;
        }
        retrieveSingleRecipeRunnable = new RetrieveSingleRecipeRunnable(recipeId);

        final Future handler = AppExecutors.getInstance().networkIO().submit(retrieveSingleRecipeRunnable);
        isRecipeRequestTimeout.setValue(false);
        AppExecutors.getInstance().networkIO().schedule(() -> {

            //let user know time out.
            isRecipeRequestTimeout.postValue(true);
            handler.cancel(true);
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * in case that we need to cancel, VM -> repo -> client to cancel current query
     */
    public void cancleRequest() {
        if (retrieveRecipeListRunnable != null) {
            retrieveRecipeListRunnable.cancelRequest = true;
        }
        if (retrieveSingleRecipeRunnable != null) {
            retrieveSingleRecipeRunnable.cancelRequest = true;
        }
    }

    private class RetrieveRecipeListRunnable implements Runnable {

        boolean cancelRequest;
        private String query;
        private int pageNumber;

        public RetrieveRecipeListRunnable(String query, int pageNumber) {
            this.query = query;
            this.pageNumber = pageNumber;
            this.cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response response = getRecipes(query, pageNumber).execute();
                if (this.cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    List<Recipe> list = new ArrayList<>(((RecipeListSearchResponse) response.body()).getRecipes());
                    if (pageNumber == 1) {
                        //if this is a first page ( new query), then update the entire list
                        recipes.postValue(list);
                    } else {
                        //if this is after 1st page, we append the result to original list only
                        List<Recipe> cur = recipes.getValue();
                        cur.addAll(((RecipeListSearchResponse) response.body()).getRecipes());
                        recipes.postValue(cur);
                    }
                } else {
                    //if error happened.
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    recipes.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                recipes.postValue(null);
            }
        }

        //not yet trigger until we call execute on call object. line 79.
        private Call<RecipeListSearchResponse> getRecipes(String query, int pageNumber) {
            return ServiceGenerator.getRecipeApi().searchRecipeList(
                    API_KEY,
                    query,
                    pageNumber);
        }
    }

    //    ------ retrieve single recipe -----
    private class RetrieveSingleRecipeRunnable implements Runnable {

        boolean cancelRequest;
        private String recipeId;

        public RetrieveSingleRecipeRunnable(String recipeId) {
            this.recipeId = recipeId;
            this.cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response response = getSingleRecipe(recipeId).execute();
                if (this.cancelRequest) {
                    return;
                }
                if (response.code() == 200) {
                    Recipe r = ((RecipeResponse) response.body()).getRecipe();
                    recipe.postValue(r);

                } else {
                    //if error happened.
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    recipe.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                recipe.postValue(null);
            }
        }

        //not yet trigger until we call execute on call object. line 79.
        private Call<RecipeResponse> getSingleRecipe(String recipeId) {
            return ServiceGenerator.getRecipeApi().getSingleRecipe(
                    API_KEY,
                    recipeId);
        }
    }
}
