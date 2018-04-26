package com.example.sysproc.ghsearchapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    Button buttonGuest;
    Button buttonGit;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonGit = (Button) findViewById(R.id.buttonGit) ;
        buttonGuest = (Button) findViewById(R.id.buttonGuest);
        progressBar = (ProgressBar)findViewById(R.id.loadingProgressBar);
    }

    @SuppressLint("StaticFieldLeak")
    public void buttonClickGit(View v) {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        EditText login = (EditText)findViewById(R.id.login);
        EditText password = (EditText)findViewById(R.id.password);
        StaticVariables.LOGIN = login.getText().toString();
        StaticVariables.PASSWORD = password.getText().toString();

        new JSONTask() {
            @Override
            public void onResponseReceived(String result) {
                switch (result) {
                    case "401":
                        showToast(getString(R.string.bad_credentials));
                        StaticVariables.LOGIN = "";
                        StaticVariables.PASSWORD = "";
                        break;
                    case "403":
                        showToast(getString(R.string.login_attempts_exceeded));
                        StaticVariables.LOGIN = "";
                        StaticVariables.PASSWORD = "";
                        break;
                    default:
                        startSearchActivity(result);
                }
            }
        }.execute("https://api.github.com/user");
    }

    public void buttonClickGuest(View v) {
        Intent i = new Intent(this, SearchActivity.class);
        startActivity(i);
    }

    private void startSearchActivity(String jsonString) {
        Intent intent = new Intent(this, SearchActivity.class);

        try {
            JSONObject object = new JSONObject(jsonString);
            intent.putExtra("login", getString(R.string.welcome) + " " + object.getString("login"));
            intent.putExtra("description", getString(R.string.press_to_open_profile));
            intent.putExtra("imageUrl", object.getString("avatar_url"));
            intent.putExtra("userUrl", object.getString("html_url"));
        } catch (JSONException e) {
            Log.i("LA", "startSearchActivity: " + e.toString());
        }

        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        startActivity(intent);
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
