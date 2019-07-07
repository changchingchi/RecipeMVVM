package com.chchi.foodrecipe.Recipe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chchi.foodrecipe.BaseActivity;
import com.chchi.foodrecipe.R;
import com.chchi.foodrecipe.models.Recipe;

/**
 * When user click particular recipe, this activity expects a Parcelable recipe object to
 * pass in the information to inflate the UI.
 * <p>
 * Recipe object contains rId for another rest api call. WE need to make extracall to obtain the
 * ingredients list from here. we need another MVVM for this flow.
 */
public class RecipeActivity extends BaseActivity {
    public static final String RECIPE = "RECIPE";
    private static final String TAG = "RecipeActivity";
    TextView title, ingredients, socialScore;
    ImageView imageView;
    LinearLayout recipeIngredientContainer;
    ScrollView scrollView;

    RecipeViewModel recipeViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_recipe);
        recipeViewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);

        imageView = findViewById(R.id.recipe_image);
        title = findViewById(R.id.recipe_title);
        socialScore = findViewById(R.id.recipe_social_score);
        recipeIngredientContainer = findViewById(R.id.ingredients_container);
        scrollView = findViewById(R.id.parent_scrollview);

        showProgressBar(true);
        subscribeObservers();
        getInComingIntent();
    }

    //we need to parse the intent and get recipe id, and make the query from here.
    private void getInComingIntent() {
        if (getIntent().hasExtra(RECIPE)) {
            Recipe recipe = getIntent().getParcelableExtra(RECIPE);
            Log.d(TAG, "getInComingIntent: recipe title : " + recipe.getTitle() + " id: " + recipe.getRecipe_id());
            Log.d(TAG, "getInComingIntent: makeing query");
            searchSingleRecipeApi(recipe.getRecipe_id());
        }
    }

    private void subscribeObservers() {
        recipeViewModel.getSingleRecipe().observe(this, new Observer<Recipe>() {
            @Override
            public void onChanged(Recipe recipe) {
                //what to do when response came back? --> inflate the UI.

                //if user click another recipe after viewing the first recipe, we need to show the progress bar at parent actvitiy instead of previous recipe cached in VM.
                //since VM doest die, when new activity associate with the same VM, this onChanged will be called even the first time, so the first onChanged method could have
                //cached recipe. and then the recipeViewModel.getRecipeId() then will have latest one.
                //we check if they are the same to know if this is a new recipe and to launch progrss bar.
                if (recipe != null) {
                    if (recipe.getRecipe_id().equals(recipeViewModel.getRecipeId())) {
                        Log.d(TAG, "onChanged: recipe: " + recipe.toString());
                        setRecipeProperties(recipe);
                        recipeViewModel.setRetrivedRecipe(true);
                    }
                }
            }
        });

        //if time out ever happened in apiclient, livedata bubble up all the way to here to inform us.
        recipeViewModel.isRecipeRequestTimeout().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean && !recipeViewModel.isRetrivedRecipe()) {
                    //time out
                    Log.d(TAG, "onChanged: timeout. ");
                    displayErrorScreen();
                }
            }
        });
    }

    private void searchSingleRecipeApi(String recipeId) {
        recipeViewModel.searchSingleRecipeApi(recipeId);
    }

    private void setRecipeProperties(Recipe recipe) {

        if (recipe != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipe.getImage_url())
                    .into(this.imageView);

            title.setText(recipe.getTitle());
            socialScore.setText(String.valueOf(Math.round(recipe.getSocial_rank())));

            //dynamically add textview into container.
            recipeIngredientContainer.removeAllViews();
            for (String ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(this);
                textView.setText(ingredient);
                textView.setTextSize(15);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                recipeIngredientContainer.addView(textView);
            }

            scrollView.setVisibility(View.VISIBLE);
            showProgressBar(false);
        }
    }

    private void displayErrorScreen(){
        Toast.makeText(this, "timeout", Toast.LENGTH_LONG).show();
        finish();
    }
}
