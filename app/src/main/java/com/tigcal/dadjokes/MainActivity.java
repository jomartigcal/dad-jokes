package com.tigcal.dadjokes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloClient;
import com.tigcal.dadjokes.graphql.GetJoke;
import com.tigcal.dadjokes.util.JokeManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SERVER_URL = "https://icanhazdadjoke.com/graphql";
    private static final String EMPTY_STRING = "";

    private ProgressBar progressBar;
    private TextView jokeTextView;
    private Button jokeButton;
    private SearchView searchView;

    private ApolloClient apolloClient;

    private String jokeQuery = EMPTY_STRING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progress_bar);
        jokeTextView = findViewById(R.id.joke_text_view);

        jokeButton = findViewById(R.id.joke_button);
        jokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJoke();
            }
        });

        displaySavedJoke();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                jokeQuery = query;
                getJoke();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (EMPTY_STRING.equals(s)) {
                    jokeQuery = EMPTY_STRING;
                }
                return false;
            }
        });

        return true;
    }

    private void displaySavedJoke() {
        String jokePref = getString(R.string.joke);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        if (preferences.contains(jokePref)) {
            displayJoke(preferences.getString(jokePref, getString(R.string.joke_tbd)));
        }
    }

    private void getJoke() {
        progressBar.setVisibility(View.VISIBLE);
        jokeTextView.setVisibility(View.INVISIBLE);

        JokeManager.getJoke(jokeQuery, new JokeManager.JokeCallback() {
            @Override
            public void handleSuccess(GetJoke.Joke joke) {
                if (joke != null) {
                    Log.d(TAG, joke.toString());
                    displayJoke(joke.joke());
                    saveJoke(joke.joke());
                } else {
                    displayErrorMessage();
                }
            }

            @Override
            public void handleFailure(Exception exception) {
                Log.e(TAG, "Get Joke Error: " + exception.getMessage());
                displayErrorMessage();
            }
        });
    }

    private void displayJoke(final String joke) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                jokeTextView.setVisibility(View.VISIBLE);

                jokeTextView.setText(joke);
            }
        });
    }

    private void saveJoke(String joke) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        preferences.edit()
                .putString(getString(R.string.joke), joke)
                .apply();
    }

    private void displayErrorMessage() {
        progressBar.setVisibility(View.INVISIBLE);

        Snackbar.make(jokeButton, getString(R.string.joke_error),
                Snackbar.LENGTH_SHORT).show();
    }
}
