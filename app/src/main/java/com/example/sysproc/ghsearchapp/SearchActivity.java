package com.example.sysproc.ghsearchapp;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private ListViewAdapter adapter;
    private ProgressBar progressBar;
    private JSONTask jsonTask;
    private ListView listView;
    private String query;
    private String queryUrl;
    private Thread queryDelayThread;
    private int totalCount = 0;
    private int page;
    private ArrayList<SearchListItem> listItems;
    private Parcelable lvState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        page = 1;
        listView = (ListView) findViewById(R.id.list_view);

        if (savedInstanceState != null) {
            listItems = (ArrayList<SearchListItem>)getLastCustomNonConfigurationInstance();
        } else {
            listItems = new ArrayList<SearchListItem>();
        }

        adapter = new ListViewAdapter(SearchActivity.this, listItems);
        listView.setAdapter(adapter);

        Intent i = getIntent();
        if (i != null && i.hasExtra("description")) {
            listItems.add(new SearchListItem(i.getStringExtra("login"),
                    i.getStringExtra("description"),
                    i.getStringExtra("imageUrl"),
                    i.getStringExtra("userUrl"),
                    adapter));
            i.removeExtra("description");
        }

        if (lvState != null) {
            listView.onRestoreInstanceState(lvState);
        }

        setListViewFooter();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (queryUrl != null && totalCount != 0 && totalCount > totalItemCount && listView.getLastVisiblePosition() == listItems.size() - 1) {
                    if (jsonTask == null) {
                        executeJSONTask();
                    } else {
                        if (!jsonTask.isWorking()) {
                            executeJSONTask();
                        }
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = listItems.get(position).getRepoUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return listItems;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("query", query);
        outState.putString("queryUrl", queryUrl);
        outState.putInt("totalCount", totalCount);
        outState.putInt("page", page);

        lvState = listView.onSaveInstanceState();
        outState.putParcelable("lvState", lvState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        query = savedInstanceState.getString("query");
        queryUrl = savedInstanceState.getString("queryUrl");
        totalCount = savedInstanceState.getInt("totalCount");
        page = savedInstanceState.getInt("page");

        lvState = savedInstanceState.getParcelable("lvState");

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            onNewQuery(query);
        }
    }

    private void onNewQuery(String query) {
        if (!query.equals("") && !query.equals(this.query)) {
            this.query = query;
            queryUrl = makeQueryUrl(query);
            clearJsonTask(jsonTask);
            listItems.clear();
            adapter.notifyDataSetChanged();
            totalCount = 0;
            page = 1;

            progressBar.setVisibility(View.VISIBLE);

            executeJSONTask();
        }
    }

    private String makeQueryUrl(String query) {
        return "https://api.github.com/search/repositories?q=" + query.replace(' ', '+');
    }

    @SuppressLint("StaticFieldLeak")
    private void executeJSONTask() {
        jsonTask = new JSONTask() {
            @Override
            public void onResponseReceived(String result) {
                addMoreItems(result);
            }
        };

        progressBar.setVisibility(View.VISIBLE);

        jsonTask.execute(queryUrl + "&page=" + page + "&per_page=25");
        page++;
    }

    private void addMoreItems(String jsonString) {
        JSONArray items = null;
        try {
            JSONObject object = new JSONObject(jsonString);

            if (object.has("message")) {
                Toast.makeText(this, getText(R.string.exceeded_rate_limit), Toast.LENGTH_SHORT).show();
                return;
            }

            totalCount = object.getInt("total_count");
            if (totalCount == 0) {
                Toast.makeText(this, getText(R.string.no_repo), Toast.LENGTH_SHORT).show();
            }
            items = object.getJSONArray("items");
        } catch (JSONException e) {
            Toast.makeText(this, getText(R.string.exceeded_rate_limit),Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            Toast.makeText(this, getText(R.string.exceeded_rate_limit),Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < items.length(); i++) {
            try {
                SearchListItem item = SearchListItem.fromJsonObject(items.getJSONObject(i), adapter);
                listItems.add(item);
            } catch (JSONException e) {}
        }

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    public static void clearJsonTask(AsyncTask<?, ?, ?> jsonTask) {
        if (jsonTask != null) {
            if (!jsonTask.isCancelled()) {
                jsonTask.cancel(true);
            }
            jsonTask = null;
        }
    }

    @Override
    public void onBackPressed() {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        if (StaticVariables.LOGIN.equals("")) {
            MenuItem overflowItem = menu.findItem(R.id.login_or_leave);
            overflowItem.setTitle(R.string.sign_in);
        } else {
            MenuItem overflowItem = menu.findItem(R.id.login_or_leave);
            overflowItem.setTitle(R.string.sign_out);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (queryDelayThread != null && queryDelayThread.isAlive()) {
                    queryDelayThread.interrupt();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (queryDelayThread != null && queryDelayThread.isAlive()) {
                    queryDelayThread.interrupt();
                }

                queryDelayThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
                            intent.setAction(Intent.ACTION_SEARCH);
                            intent.putExtra(SearchManager.QUERY, newText);
                            SearchActivity.this.startActivity(intent);
                        } catch (InterruptedException e) {}
                    }
                };
                queryDelayThread.start();

                return false;
            }

        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.login_or_leave) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setListViewFooter() {
        View view = LayoutInflater.from(this).inflate(R.layout.footer_listview_progressbar, null);
        this.progressBar = view.findViewById(R.id.progressBar);

        listView.addFooterView(this.progressBar, null, false);
    }

    class ListViewAdapter extends ArrayAdapter {
        ArrayList<SearchListItem> items;

        ListViewAdapter(Context context, ArrayList<SearchListItem> items) {
            super(context, R.layout.listview_raw, R.id.idTitle, items);
            this.items = items;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.listview_raw, parent, false);

            ImageView myImage = (ImageView) row.findViewById(R.id.idPic);
            TextView myTitle = (TextView) row.findViewById(R.id.idTitle);
            TextView myDesc = (TextView) row.findViewById(R.id.idDesciption);

            myTitle.setText(items.get(position).getTitle());
            myDesc.setText(items.get(position).getDescription());
            myImage.setImageBitmap(items.get(position).getImage());

            return row;
        }

    }

}
