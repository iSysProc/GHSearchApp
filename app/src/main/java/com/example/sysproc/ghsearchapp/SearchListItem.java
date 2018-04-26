package com.example.sysproc.ghsearchapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SearchListItem implements Parcelable{
    private String title;
    private String description;
    private Bitmap image;
    private String repoUrl;
    private SearchActivity.ListViewAdapter adapter;

    protected SearchListItem(String title, String description, String imageUrl, String repoUrl, SearchActivity.ListViewAdapter adapter) {
        this.title = title;
        this.description = description;
        this.adapter = adapter;
        this.repoUrl = repoUrl;

        new DownloadImageTask().execute(imageUrl);
    }

    protected SearchListItem(Parcel in) {
        title = in.readString();
        description = in.readString();
        image = in.readParcelable(Bitmap.class.getClassLoader());
        repoUrl = in.readString();
    }

    public static final Creator<SearchListItem> CREATOR = new Creator<SearchListItem>() {
        @Override
        public SearchListItem createFromParcel(Parcel in) {
            return new SearchListItem(in);
        }

        @Override
        public SearchListItem[] newArray(int size) {
            return new SearchListItem[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public static SearchListItem fromJsonObject(JSONObject object, SearchActivity.ListViewAdapter adapter) {
        String title = null;
        String description = null;
        String imageUrl = null;
        String repoUrl = null;

        try {
            title = object.getString("full_name");
            description = object.getString("description");
            if (description.equals("null")) description = adapter.getContext().getText(R.string.no_description).toString();
            imageUrl = object.getJSONObject("owner").getString("avatar_url");
            repoUrl = object.getString("html_url");
        } catch (JSONException e) {

        }

        return new SearchListItem(title, description, imageUrl, repoUrl,adapter);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeParcelable(image, flags);
        dest.writeString(repoUrl);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                InputStream in = new URL(urls[0]).openStream();
                Bitmap result = BitmapFactory.decodeStream(in);

                return result;
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap res) {
            if (res != null) {
                image = res;
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

}
