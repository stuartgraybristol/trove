package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.stucollyn.nfc_play.R;

public class CloudStoryGalleryMenu extends AppCompatActivity {

    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Cloud Story Gallery Menu");
        setContentView(R.layout.activity_cloud_story_gallery_menu);
    }

    public void SearchText(View view) {

        Intent intent = new Intent(CloudStoryGalleryMenu.this, CloudStoryGallery.class);
        intent.putExtra("Orientation", mode);
        intent.putExtra("QueryType", "text");
        CloudStoryGalleryMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }


    public void SearchImage(View view) {

        Intent intent = new Intent(CloudStoryGalleryMenu.this, CloudStoryGallery.class);
        intent.putExtra("Orientation", mode);
        intent.putExtra("QueryType", "image");
        CloudStoryGalleryMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }


    public void SearchDate(View view) {

        Intent intent = new Intent(CloudStoryGalleryMenu.this, CloudStoryGallery.class);
        intent.putExtra("Orientation", mode);
        intent.putExtra("QueryType", "date");
        CloudStoryGalleryMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }
}
