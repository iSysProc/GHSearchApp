package com.example.sysproc.ghsearchapp;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    //testData
    String[] titles = {"mojombo/grit", "wycats/merb-core", "mojombo/grit", "wycats/merb-core", "mojombo/grit", "wycats/merb-core","mojombo/grit", "wycats/merb-core", "mojombo/grit", "wycats/merb-core"};
    String[] descr = {"**Grit asdfasdfasdfasdfasdfis no longasdfasdfasdfasdfasdfasdfer maintasdfasdfasdfained. Check out libgit2/rugged.** Grit gives you object oriented read/write access to Git repositories via Ruby.", "Merb Core: All you need. None you don't.", "**Grit is no longer maintained. Check out libgit2/rugged.** Grit gives you object oriented read/write access to Git repositories via Ruby.", "Merb Core: All you need. None you don't.", "**Grit is no longer maintained. Check out libgit2/rugged.** Grit gives you object oriented read/write access to Git repositories via Ruby.", "Merb Core: All you need. None you don't.", "**Grit is no longer maintained. Check out libgit2/rugged.** Grit gives you object oriented read/write access to Git repositories via Ruby.", "Merb Core: All you need. None you don't.", "**Grit is no longer maintained. Check out libgit2/rugged.** Grit gives you object oriented read/write access to Git repositories via Ruby.", "Merb Core: All you need. None you don't."};
    int[] images = {R.drawable.first_auth, R.drawable.first_auth, R.drawable.first_auth, R.drawable.second_auth, R.drawable.first_auth, R.drawable.second_auth, R.drawable.first_auth, R.drawable.second_auth, R.drawable.first_auth, R.drawable.second_auth};
    ListView lv;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        lv = (ListView) findViewById(R.id.list_view);
        TestAdapter adapter = new TestAdapter(SearchActivity.this, titles, descr, images);
        lv.setAdapter(adapter);

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                
            }
        });
    }

    class TestAdapter extends ArrayAdapter {

        int[] imageArray;
        String[] titleArray;
        String[] descArray;

        public TestAdapter(Context context, String[] titles1, String[] descr1, int[] images1) {
            super(context, R.layout.listview_raw, R.id.idTitle, titles1);
            this.imageArray = images1;
            this.titleArray = titles1;
            this.descArray = descr1;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.listview_raw, parent, false);
            ImageView myImage = (ImageView) row.findViewById(R.id.idPic);
            TextView myTitle = (TextView) row.findViewById(R.id.idTitle);
            TextView myDesc = (TextView) row.findViewById(R.id.idDesciption);

            //myImage.setImageBitmap(imageArray[position]);
            myImage.setImageResource(imageArray[position]);
            myDesc.setText(descArray[position]);
            myTitle.setText(titleArray[position]);

            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_search; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}
