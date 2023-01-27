package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.stucollyn.nfc_play.R;

public class SavedStoryConfirmation extends AppCompatActivity {

    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_story_confirmation);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("New Story Saved");

        try {

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    Intent intent = new Intent(SavedStoryConfirmation.this, MainMenu.class);
                    intent.putExtra("Orientation", mode);
                    SavedStoryConfirmation.this.startActivity(intent);
                    overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
                }
            }, 3000);
        }

            catch (NullPointerException e) {

        }
        }

}
