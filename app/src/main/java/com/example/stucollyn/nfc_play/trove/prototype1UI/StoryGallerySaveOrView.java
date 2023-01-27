package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.io.Serializable;

public class StoryGallerySaveOrView extends AppCompatActivity implements Serializable {

    File fileOnTag;
    File[] filesOnTag;
    String tag_data;
    FragmentTransaction ft;
    SaveOrViewFragment saveOrViewFragment;
    ShowTagContentFragment showTagContentFragment;
    int mode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(mode);
        getSupportActionBar().setTitle("Story Library");
        fileOnTag = (File)getIntent().getExtras().get("StoryDetails");
        filesOnTag = (File[]) getIntent().getExtras().getSerializable("filesOnTag");
        tag_data = fileOnTag.getName();
        setContentView(R.layout.activity_save_or_view);
        InitFragments();
        Log.d("File On Tag Name: ", tag_data);
    }

    void InitFragments() {

        saveOrViewFragment = new SaveOrViewFragment();
        showTagContentFragment = new ShowTagContentFragment();
        //Open first fragment in the fragment array list
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, saveOrViewFragment);
        ft.commit();
    }

    public void ViewStory(View view){

//        Intent intent = new Intent(StoryGallerySaveOrView.this, SavedStoryConfirmation.class);
//        StoryGallerySaveOrView.this.startActivity(intent);
//        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);


//        Log.i("Files on tag UID: ", filesOnTag.toString());
        Bundle bundle = new Bundle();
        bundle.putInt("Orientation", mode);
        bundle.putSerializable("filesOnTag", filesOnTag);
        showTagContentFragment.setArguments(bundle);
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, showTagContentFragment);
        ft.commit();
    }

    public void StartSaveStoryToNFC(View view) {

        Intent intent = new Intent(StoryGallerySaveOrView.this, SaveStoryToNFC.class);
        intent.putExtra("Orientation", mode);
        intent.putExtra("TagData", tag_data);
        StoryGallerySaveOrView.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    @Override
    public void onBackPressed() {

//        Intent intent = new Intent(StoryGallerySaveOrView.this, LocalStoryGallery.class);
//        StoryGallerySaveOrView.this.startActivity(intent);
//        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
        finish();
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

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
