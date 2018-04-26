package com.example.sysproc.ghsearchapp;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

public abstract class JSONTask extends AsyncTask<String, String, String> {

    private boolean working = false;

    public boolean isWorking() {
        return working;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        working = true;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(strings[0]);
            connection = (HttpURLConnection) url.openConnection();

            String auth = StaticVariables.LOGIN + ":" + StaticVariables.PASSWORD;
            byte[] bytes = Base64.encode(auth.getBytes(), 0);
            auth = "Basic " + new String(bytes);
            connection.setRequestProperty("Authorization", auth);

            connection.connect();

            switch (connection.getResponseCode()) {
                case 401:
                    return "401";
                case 403:
                    return "403";
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {

            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        onResponseReceived(s);
        working = false;
    }

    public abstract void onResponseReceived(String result);
}
