package com.chchi.foodrecipe.RecipeList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chchi.foodrecipe.BaseActivity;
import com.chchi.foodrecipe.R;
import com.chchi.foodrecipe.Recipe.RecipeActivity;
import com.chchi.foodrecipe.adapters.RecipeRecyclerAdapter;
import com.chchi.foodrecipe.adapters.onRecipeClickListener;
import com.chchi.foodrecipe.models.Recipe;

import java.util.List;

import static com.chchi.foodrecipe.Recipe.RecipeActivity.RECIPE;

public class RecipeListActivity extends BaseActivity {
    private static final String TAG = "RecipeListActivity";
    private RecipeListViewModel recipeListViewModel;
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter recipeRecyclerAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        recipeListViewModel = ViewModelProviders.of(this).get(RecipeListViewModel.class);
        subscribeObservers();

        //init recyclerview
        recyclerView = findViewById(R.id.recipe_list);
        initRecyclerView();

        //init searchView
        searchView = findViewById(R.id.search_view);
        initSearchView();

        if (!recipeListViewModel.isViewingRecipe()) {
            //when first start, user should see category page
            displaySearchCategories();
        }

        //since we have custom tool bar, we need to associate the toolbar by call the follwing
        setSupportActionBar(findViewById(R.id.toolbar));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if currently on receipe list, then we need to navigate back to category.
        if (item.getItemId() == R.id.action_categories) {
            displaySearchCategories();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //here we can observe the recipe livedata
    private void subscribeObservers() {
        recipeListViewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                if (recipes != null && recipeListViewModel.isViewingRecipe()) {
//                    Log.d(TAG, "subscribeObservers: " + recipes.toString());
                    //at this moment we have data set ready --> adaptor-->UI
                    recipeRecyclerAdapter.setRecipes(recipes);

                    //done with query, so set false
                    recipeListViewModel.setPerformaningQuery(false);
                }
            }
        });

        recipeListViewModel.isQueryExhausted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                 if(aBoolean){
                     Log.d(TAG, "onChanged: query exhausted...");
                     recipeRecyclerAdapter.setQueryExhausted();
                 }
            }
        });
    }

    private void displaySearchCategories() {
        recipeListViewModel.setViewingRecipe(false);
        recipeRecyclerAdapter.displaySearchCateories();
    }

    private void initRecyclerView() {
        recipeRecyclerAdapter = new RecipeRecyclerAdapter(new onRecipeClickListener() {
            @Override
            public void onRecipeClick(int position) {
                //when user now on recipe list, when they clicked into the specific recipe,
                //we navigate user to recipe acitvity
                Intent intent = new Intent(RecipeListActivity.this, RecipeActivity.class);
                intent.putExtra(RECIPE, recipeRecyclerAdapter.getRecipeByPosition(position));
                startActivity(intent);
            }

            @Override
            public void onCategoryClick(String category) {
                recipeRecyclerAdapter.displayLoading();
                searchRecipeApi(category, 1);
                //we clear the focus for backpress event. if we dont do this, first back pressed is
                // going to be consumed by focus(thus it clear the focus.)
                searchView.clearFocus();
            }
        });

        recyclerView.setAdapter(recipeRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //this is for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    //no longer be able to scroll, so search for next page.
                    recipeListViewModel.searchNextPage();
                }
            }
        });

    }

    private void initSearchView() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                recipeRecyclerAdapter.displayLoading();
                searchRecipeApi(query, 1);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchRecipeApi(String query, int pageNumber) {
        recipeListViewModel.searchRecipesApi(query, pageNumber);
    }

    @Override
    public void onBackPressed() {
        if (recipeListViewModel.isOnRecipeList()) {
            //on recipe page, then display categories again.
            displaySearchCategories();
        } else {
            //on categories page, then close the app.
            super.onBackPressed();
        }
    }
}
