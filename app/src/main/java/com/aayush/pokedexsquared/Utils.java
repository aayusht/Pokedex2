package com.aayush.pokedexsquared;

import android.app.Activity;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aayush on 2018-02-24.
 * All of these methods / classes / interfaces as generalizable to different activities
 */

class Utils {
    final static String BASEURL = "https://pokeapi.co/api/v2/pokemon/";

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Basically a wrapper for some method that takes in a JSONObject and does something
     * with it. THIS IS NOT AN ACTUAL RUNNABLE. This is just how we handle lambdas in Java.
     * You implement this interface with a bunch of different classes, but you can always
     * call the run method, which will come in handy later
     */
    interface JSONRunnable {
        void run(JSONObject jsonObject);
    }

    /**
     * AsyncTasks should always be static if they do any UI stuff, otherwise memory leaks can occur.
     * It looks weird, but it opens more possibilities to organizing your code better.
     */
    static class FetchJSONTask extends AsyncTask<Void, Void, JSONObject> {
        final String urlString;

        /*
         * This can be any JSONRunnable! Let's say we had some other activity that needed us to
         * get some weather data and use the JSONObject from the GET request to update a
         * RecyclerView or something. All we need to do is define a new JSONRunnable whose run
         * method updates the RecyclerView given a JSONObject, and call
         *
         * new FetchJSONTask(urlString, WeatherJSONRunnable r, WeatherActivity.this).exectute()
         *
         * No need to copy paste this AsyncTask over and over!
         */
        final JSONRunnable r;

        /* Because of the way android handles activities, you shouldn't have them in an AsyncTask
         * a WeakReference is like a barebones copy that will accomplish what we need */
        final WeakReference<Activity> activityReference;

        FetchJSONTask(String urlString, JSONRunnable r, Activity activity) {
            this.urlString = urlString;
            this.r = r;
            this.activityReference = new WeakReference<>(activity); //make it a reference
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL(urlString);
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
        protected void onPostExecute(final JSONObject jsonObject) {
            /*
             * Ok so first we're getting the activity from the reference with .get(). It's ok to
             * use the activity as long as it's not in an instance variable. Then we run our
             * JSONRunnable's run method with the retrieved JSONObject, making sure it's on the
             * UI thread with the syntax below.
             */
            activityReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    r.run(jsonObject);
                }
            });
        }
    }
}
