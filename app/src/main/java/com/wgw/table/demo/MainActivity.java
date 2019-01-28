package com.wgw.table.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void goTableActivity(View view) {
        Intent intent = new Intent(this, TableActivity.class);
        startActivity(intent);
    }
    
    public void goSurfaceTableActivity(View view) {
        Intent intent = new Intent(this, SurfaceTableActivity.class);
        startActivity(intent);
    }
}
