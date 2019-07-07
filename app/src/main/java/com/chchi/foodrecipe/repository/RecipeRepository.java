package com.chchi.foodrecipe.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.chchi.foodrecipe.models.Recipe;
import com.chchi.foodrecipe.network.RecipeApiClient;

import java.util.List;

/**
 * all viewmodels can only talks to this repositoy and they dont care about where the data from.
 * <p>
 * 從這裡開始repo是從apiclient拿資料 所以在constructor裡面我們init了apiclint
 * 這樣子的過程完成了單一方向的archituture designs. 然後返回的livedata 一路返回去給vm -> view observe.
 * livedata一路bubble up
 */

public class RecipeRepository {

    private static RecipeRepository instance;
    private RecipeApiClient recipeApiClient;
    private String query;
    private int pageNumber;
    private String recipeId;
    private MutableLiveData<Boolean> isQueryExhausted = new MutableLiveData<>();
    //we use mediator live data to halt the change before it goes back to VM
    private MediatorLiveData<List<Recipe>> recipes = new MediatorLiveData<>();

    public RecipeRepository() {
        this.recipeApiClient = RecipeApiClient.getInstance();
        initMediators();
    }

    public static RecipeRepository getInstance() {
        if (instance == null) {
            instance = new RecipeRepository();
        }
        return instance;
    }

    public MutableLiveData<Boolean> getIsQueryExhausted() {
        return isQueryExhausted;
    }

    private void initMediators() {
        LiveData<List<Recipe>> receipeListApiSource = recipeApiClient.getRecipes();
        recipes.addSource(receipeListApiSource, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes1) {
                //when source changed, it comes here first before it goes back.
                if (recipes1 != null) {
                    recipes.setValue(recipes1);
                    doneQuery(recipes1);
                } else {
                    // search DB cached.
                }
            }
        });
    }

    private void doneQuery(List<Recipe> list) {
        if (list != null) {
            if (list.size() % 30 != 0) {
                isQueryExhausted.setValue(true);
            }
        } else {
            isQueryExhausted.setValue(true);
        }
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public LiveData<Recipe> getSingleRecipe() {
        return recipeApiClient.getSingleRecipe();
    }

    public LiveData<Boolean> isRecipeRequestTimeout() {
        return recipeApiClient.isRecipeRequestTimeout();
    }

    //repo提供了一個方法 讓vm，而這個方法call webservice. chain together...
    public void searchRecipesApi(String query, int pageNumber) {
        //at this level, we only do some input validation
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        this.query = query;
        this.pageNumber = pageNumber;
        this.isQueryExhausted.setValue(false);
        recipeApiClient.searchRecipesApi(query, pageNumber);
    }

    //repo提供了一個方法 讓vm，而這個方法call webservice. chain together...
    public void searchSingleRecipeApi(String recipeId) {
        //at this level, we only do some input validation
        this.recipeId = recipeId;
        recipeApiClient.searchSingleRecipesApi(recipeId);
    }

    public void cancelRequest() {
        this.recipeApiClient.cancleRequest();
    }

    public void searchNextPage() {
        searchRecipesApi(this.query, pageNumber + 1);
    }
}

