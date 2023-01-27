package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

public class StoryGalleryMenu extends AppCompatActivity {

    int mode;
    boolean cloudAvailability = false;
    ImageButton cloudStorage;
    TextView cloudStorageCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setContentView(R.layout.activity_story_gallery_menu);
        cloudStorage = (ImageButton) findViewById(R.id.cloud_storage);
        cloudStorageCaption = (TextView) findViewById(R.id.cloud_title);
        cloudAvailability = isNetworkConnected();

        if(cloudAvailability) {

            cloudStorage.setVisibility(View.VISIBLE);
            cloudStorageCaption.setVisibility(View.VISIBLE);
        }

        else {

            cloudStorage.setVisibility(View.GONE);
            cloudStorageCaption.setVisibility(View.GONE);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void Local(View view){

        Intent intent = new Intent(StoryGalleryMenu.this, LocalStoryGallery.class);
        intent.putExtra("Orientation", mode);
        StoryGalleryMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    public void Cloud(View view){

        Intent intent = new Intent(StoryGalleryMenu.this, CloudStoryGalleryMenu.class);
        intent.putExtra("Orientation", mode);
        StoryGalleryMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //On back button pressed, return to main menu
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(StoryGalleryMenu.this, MainMenu.class);
        intent.putExtra("Orientation", mode);
        StoryGalleryMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //When action bar back button is pressed, call onBackPressed()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
