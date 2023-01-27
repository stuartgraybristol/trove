package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.stucollyn.nfc_play.R;

public class SelectMode extends AppCompatActivity {

    Button boxMode, ontheGo;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        boxMode = findViewById(R.id.boxmode);
        ontheGo = findViewById(R.id.onthego);
    }

    public void BoxMode(View view) {

        mode = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        Intent intent = new Intent(SelectMode.this, SplashScreen.class);
        intent.putExtra("Orientation", mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SelectMode.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    public void OnTheGo (View view) {

        mode = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        Intent intent = new Intent(SelectMode.this, SplashScreen.class);
        intent.putExtra("Orientation", mode);
        SelectMode.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }
}
