package com.chchi.foodrecipe.RecipeList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.chchi.foodrecipe.models.Recipe;
import com.chchi.foodrecipe.repository.RecipeRepository;

import java.util.List;

public class RecipeListViewModel extends ViewModel {

    RecipeRepository recipeRepository;
    //boolean to track if current user is currently viewing recipe and not category page.
    private boolean isViewingRecipe;
    private boolean isPerformaningQuery;

    public RecipeListViewModel() {
        recipeRepository = new RecipeRepository();
    }

    public boolean isPerformaningQuery() {
        return isPerformaningQuery;
    }

    public void setPerformaningQuery(boolean performaningQuery) {
        isPerformaningQuery = performaningQuery;
    }

    public boolean isViewingRecipe() {
        return isViewingRecipe;
    }

    public void setViewingRecipe(boolean viewingRecipe) {
        isViewingRecipe = viewingRecipe;
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipeRepository.getRecipes();
    }


    //repo提供了一個方法 讓vm，而這個方法call webservice. chain together...
    //we exposed method in VM so that view can call VM directly.
    //我們幾乎直接複製Repo裡面的方法過來這裡 為了讓view可以呼叫
    //對照一下結構圖 這樣的方法完成了 livedata chain.
    //livedata會自己呼叫observers 我們只要讓相對應的view observe即可
    public void searchRecipesApi(String query, int pageNumber) {
        this.isViewingRecipe = true;
        this.isPerformaningQuery = true;
        recipeRepository.searchRecipesApi(query, pageNumber);
    }

    public void searchNextPage() {
        if (!isPerformaningQuery
                && isViewingRecipe
                && !isQueryExhausted().getValue()) {
            recipeRepository.searchNextPage();
        }
    }

    public boolean isOnRecipeList() {
        if (this.isPerformaningQuery) {
            //we need to cancel the query, from here we send signal to repo -> apiclient -> cancel query.
            cancelRequest();
            this.isPerformaningQuery = false;
        }

        if (this.isViewingRecipe) {
            this.isViewingRecipe = false;
            return true;
        }
        return false;
    }

    public void cancelRequest() {
        this.recipeRepository.cancelRequest();
    }

    public LiveData<Boolean> isQueryExhausted() {
        return recipeRepository.getIsQueryExhausted();
    }
}
