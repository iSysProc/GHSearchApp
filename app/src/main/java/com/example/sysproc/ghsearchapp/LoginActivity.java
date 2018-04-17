package com.example.sysproc.ghsearchapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {
    Button buttonGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        buttonGuest = (Button) findViewById(R.id.buttonGuest);
    }

    public void buttonClickGuest(View v) {
        Intent i = new Intent(this, SearchActivity.class);
        startActivity(i);
    }
}
