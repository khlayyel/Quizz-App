package com.isamm.weathapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        // Mettre le titre
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("Weather App");
        }


        //open login activity
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity1.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        //open register activity
        TextView textViewRegisterlink = findViewById(R.id.textView_register_link);
        textViewRegisterlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity1.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }
}