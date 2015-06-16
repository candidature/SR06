package com.example.desktop.sr06;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Desktop on 6/16/2015.
 */

class GoogleTask extends AsyncTask<String,Integer,String> {

    public static String TAG = "GoogleTask";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }


    protected void googleSearch(String query) {
        String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
        String charset = "UTF-8";
        URL url = null;
        try {
            url = new URL(address + URLEncoder.encode(query, charset));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    url.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str;

        try {
            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                Log.d(TAG, str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }
}
