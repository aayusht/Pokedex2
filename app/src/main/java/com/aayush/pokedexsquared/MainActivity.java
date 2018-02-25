package com.aayush.pokedexsquared;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.aayush.pokedexsquared.Utils.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                (new FetchJSONTask(BASEURL + s.toLowerCase(), new SetPokemonViews(), MainActivity.this)).execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
        return true;
    }

    /**
     * So this class implements the interface we made in Utils. This is how lambdas / higher
     * order functions work in Java. As a JSONRunnable, it can be passed into the AsyncTask
     */
    class SetPokemonViews implements JSONRunnable {
        @Override
        public void run(JSONObject jsonObject) {
            if (jsonObject != null) {
                TextView name, attack, defense, spatk, spdef, hp, speed;
                ImageView imageView;
                name = findViewById(R.id.textView);
                attack = findViewById(R.id.attack);
                defense = findViewById(R.id.defense);
                spatk = findViewById(R.id.spatk);
                spdef = findViewById(R.id.spdef);
                hp = findViewById(R.id.hp);
                speed = findViewById(R.id.speed);
                imageView = findViewById(R.id.imageView);
                try {
                    name.setText(jsonObject.getString("name"));
                    JSONArray stats = jsonObject.getJSONArray("stats");
                    /* Typically Strings are retrieved from the strings.xml file using
                    * this getString(R.string.afkjald); In this case, we are using a string
                    * template in the strings.xml file (look at it in res/values/string.xml),
                    * so we pass in our arguments into getString as well. Android Studio will not
                    * autocomplete this for you, but it will complain. */
                    speed.setText(getString(R.string.speed, stats.getJSONObject(0).getInt("base_stat")));
                    spdef.setText(getString(R.string.spdef, stats.getJSONObject(1).getInt("base_stat")));
                    spatk.setText(getString(R.string.spatk, stats.getJSONObject(2).getInt("base_stat")));
                    defense.setText(getString(R.string.defense, stats.getJSONObject(3).getInt("base_stat")));
                    attack.setText(getString(R.string.attack, stats.getJSONObject(4).getInt("base_stat")));
                    hp.setText(getString(R.string.hp, stats.getJSONObject(5).getInt("base_stat")));
                    String imageUrl = jsonObject.getJSONObject("sprites").getString("front_default");
                    Glide.with(MainActivity.this).load(imageUrl).into(imageView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MainActivity.this, "Not a pokemon", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
