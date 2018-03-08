package com.aayush.pokedexsquared;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.aayush.pokedexsquared.Utils.*;

public class MainActivity extends AppCompatActivity {
    ViewPager fragmentPager;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentPager = findViewById(R.id.viewPager);
        fragmentPager.setAdapter(new PokemonPageAdapter(getSupportFragmentManager(), "diglett"));
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
                fragmentPager.setAdapter(new PokemonPageAdapter(getSupportFragmentManager(), s));
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
     * An adapter for our ViewPager (that we named fragmentPager)
     *
     * It works just like a RecyclerView Adapter, except it's a bit simpler. I defined it in the
     * activity, although it's fine to define it in a new file.
     */
    class PokemonPageAdapter extends FragmentStatePagerAdapter {
        String pokemonName = "diglett";

        //our whole pager will be initialized with the activity's fragment manager, and some pokemon name
        public PokemonPageAdapter(FragmentManager fm, String pokemonName) {
            super(fm);
            this.pokemonName = pokemonName;
        }

        //which fragment for which screen (in this case we can just pass in the index of the screen as an argument
        @Override
        public Fragment getItem(int position) {
            return PokemonFragment.newInstance(pokemonName, position);
        }

        //how many fragments
        @Override
        public int getCount() {
            return 2;
        }
    }

    /**
     * A fragment that represents *any* fragment in the viewpager. We just pass in the position in the
     * pager (in this case 0 or 1) and do different things based on that.
     */
    public static class PokemonFragment extends Fragment {
        int position;
        String pokemonName;

        /**
         * WE DON'T USE CONSTRUCTORS. We put all the arguments into a bundle, call the EMPTY constructor
         * (which doesn't do anything) and use the built in set arguments method with the bundle.
         *
         * Note that this method is static.
         * @param pokemonName
         * @param position
         * @return the fragment
         */
        static PokemonFragment newInstance(String pokemonName, int position) {
            PokemonFragment fragment = new PokemonFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putString("name", pokemonName);
            Log.d("name", pokemonName);
            fragment.setArguments(args);
            return fragment;
        }

        /**
         * This method is called before we inflate the layout, so all we can do here is set the arguments
         * from the bundle passed in earlier (which we can retrieve with getArguments())
         * @param savedInstanceState
         */
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.position = getArguments().getInt("position");
            this.pokemonName = getArguments().getString("name");
        }

        @SuppressLint("StaticFieldLeak")
        @Nullable
        @Override
        /**
         * This is where all the work is done. We do different things based on what the position is
         * in the viewPager. Note that we have to inflate the view, do what we want with it, and return it
         */
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view;
            switch(position) {
                case 0:
                    //this line is pretty much just copy paste, but change the layout
                    view = inflater.inflate(R.layout.fragment_pokemon, container, false);
                    new AsyncTask<Void, Void, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(Void... voids) {
                            try {
                                URL url = new URL(BASEURL + pokemonName.toLowerCase());
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("GET");
                                InputStream in = new BufferedInputStream(conn.getInputStream());
                                String response = convertStreamToString(in);
                                return new JSONObject(response);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(JSONObject jsonObject) {
                            //pretty much the same as earlier, remember to use view.findViewById
                            if (jsonObject != null) {
                                TextView name, attack, defense, spatk, spdef, hp, speed;
                                ImageView imageView;
                                name = view.findViewById(R.id.textView);
                                attack = view.findViewById(R.id.attack);
                                defense = view.findViewById(R.id.defense);
                                spatk = view.findViewById(R.id.spatk);
                                spdef = view.findViewById(R.id.spdef);
                                hp = view.findViewById(R.id.hp);
                                speed = view.findViewById(R.id.speed);
                                imageView = view.findViewById(R.id.imageView);
                                try {
                                    name.setText(jsonObject.getString("name"));
                                    JSONArray stats = jsonObject.getJSONArray("stats");
                                    speed.setText(getString(R.string.speed, stats.getJSONObject(0).getInt("base_stat")));
                                    spdef.setText(getString(R.string.spdef, stats.getJSONObject(1).getInt("base_stat")));
                                    spatk.setText(getString(R.string.spatk, stats.getJSONObject(2).getInt("base_stat")));
                                    defense.setText(getString(R.string.defense, stats.getJSONObject(3).getInt("base_stat")));
                                    attack.setText(getString(R.string.attack, stats.getJSONObject(4).getInt("base_stat")));
                                    hp.setText(getString(R.string.hp, stats.getJSONObject(5).getInt("base_stat")));
                                    String imageUrl = jsonObject.getJSONObject("sprites").getString("front_default");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.execute();

                    break;
                default:
                    //if it's not the first page, inflate this other layout file, which has other content
                    //never actually filled this page up but you can do whatever you want here, like
                    //make a get request for the pokemon's moves and fill a recyclerview
                    view = inflater.inflate(R.layout.fragment_moves, container, false);
            }
            return view;
        }
    }

//    /**
//     * So this class implements the interface we made in Utils. This is how lambdas / higher
//     * order functions work in Java. As a JSONRunnable, it can be passed into the AsyncTask
//     */
//    class SetPokemonViews implements JSONRunnable {
//        @Override
//        public void run(JSONObject jsonObject) {
//            if (jsonObject != null) {
//                TextView name, attack, defense, spatk, spdef, hp, speed;
//                ImageView imageView;
//                name = findViewById(R.id.textView);
//                attack = findViewById(R.id.attack);
//                defense = findViewById(R.id.defense);
//                spatk = findViewById(R.id.spatk);
//                spdef = findViewById(R.id.spdef);
//                hp = findViewById(R.id.hp);
//                speed = findViewById(R.id.speed);
//                imageView = findViewById(R.id.imageView);
//                try {
//                    name.setText(jsonObject.getString("name"));
//                    JSONArray stats = jsonObject.getJSONArray("stats");
//                    /* Typically Strings are retrieved from the strings.xml file using
//                    * this getString(R.string.afkjald); In this case, we are using a string
//                    * template in the strings.xml file (look at it in res/values/string.xml),
//                    * so we pass in our arguments into getString as well. Android Studio will not
//                    * autocomplete this for you, but it will complain. */
//                    speed.setText(getString(R.string.speed, stats.getJSONObject(0).getInt("base_stat")));
//                    spdef.setText(getString(R.string.spdef, stats.getJSONObject(1).getInt("base_stat")));
//                    spatk.setText(getString(R.string.spatk, stats.getJSONObject(2).getInt("base_stat")));
//                    defense.setText(getString(R.string.defense, stats.getJSONObject(3).getInt("base_stat")));
//                    attack.setText(getString(R.string.attack, stats.getJSONObject(4).getInt("base_stat")));
//                    hp.setText(getString(R.string.hp, stats.getJSONObject(5).getInt("base_stat")));
//                    String imageUrl = jsonObject.getJSONObject("sprites").getString("front_default");
//                    Glide.with(MainActivity.this).load(imageUrl).into(imageView);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Toast.makeText(MainActivity.this, "Not a pokemon", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}
