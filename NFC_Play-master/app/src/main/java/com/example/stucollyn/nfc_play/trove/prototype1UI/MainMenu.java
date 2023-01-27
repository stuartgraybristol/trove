package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.stucollyn.nfc_play.R;
import com.google.firebase.storage.StorageReference;

//The MainMenu Activity is the app home page, where all functionality can be found and selected
public class MainMenu extends AppCompatActivity {

    TextView welcome;
    ImageView miine_mini;
    VideoView top_half__video;
    Button cloud_archive;
    ImageButton miine_library, play_story, record_story, trove_logo_button;
    ConstraintLayout top_holder;
    Context context;
    int mode;
    private StorageReference mStorageRef;

    //onCreate is called when Activity begins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInit();
    }

    void ActivityInit() {

        setContentView(R.layout.activity_main_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        ActionBarSetup();
        initView();
    }

    //Setup action bar
    private void ActionBarSetup() {

        //Display both title and image, and a back button in action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set and show trove logo in action bar
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Set page title shown in action bar
        getSupportActionBar().setTitle("Home");
    }

    //Initialize the Activity view and setup initial visibility settings
    private void initView() {

        miine_mini = (ImageView) findViewById(R.id.miine_mini);
        //cloud_archive = (Button) findViewById(R.id.cloud_archive);
        miine_library = (ImageButton) findViewById(R.id.miine_library);
        play_story = (ImageButton) findViewById(R.id.play_story);
        record_story = (ImageButton) findViewById(R.id.record_story);
        top_half__video = (VideoView) findViewById(R.id.main_menu_video);
        top_holder = (ConstraintLayout) findViewById(R.id.top_half_holder);
        welcome = (TextView) findViewById(R.id.welcome);
        trove_logo_button = (ImageButton) findViewById(R.id.main_menu_video_button);

        // cloud_archive.setVisibility(View.VISIBLE);
//        miine_library.setVisibility(View.VISIBLE);
//        play_story.setVisibility(View.VISIBLE);
//        record_story.setVisibility(View.VISIBLE);
//        welcome.setVisibility(View.VISIBLE);
//        top_half__video.setVisibility(View.VISIBLE);
//        top_holder.setVisibility(View.VISIBLE);
    }

    //Logout and return to the Login Screen
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(MainMenu.this, LoginScreen.class);
        intent.putExtra("Orientation", mode);
        MainMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    public void StartVideo(View view) {

        initMediaController();
        trove_logo_button.setVisibility(View.INVISIBLE);
    }

    //Open cloud archive Activity - to be completed
    public void CloudArchive(View view) {

        cloud_archive.setEnabled(false);
    }

    //Open miine library Activity - to be completed
    public void MiineLibrary(View view) {

        miine_library.setEnabled(false);
    }

    //Open play story Activity
    public void PlayStory(View view) {

        play_story.setEnabled(false);
        Intent intent = new Intent(MainMenu.this, NFCRead.class);
        intent.putExtra("Orientation", mode);
        MainMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);

    }

    //Open new story creation Activity
    public void RecordStory(View view){

        record_story.setEnabled(false);
        Intent intent = new Intent(MainMenu.this, StoryMediaChooser.class);
        intent.putExtra("Orientation", mode);
        MainMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);

    }

    public void StoryLibrary(View view) {
        Intent intent = new Intent(MainMenu.this, StoryGalleryMenu.class);
        intent.putExtra("Orientation", mode);
        MainMenu.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //When action bar back button pressed, implement onBackPressed method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Initialize media controller and begin default video
    private void initMediaController() {

        /*Try to create media controller, load and play the listed URI. If this doesn't exist catch
        the null pointer exception
         */
        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(top_half__video);
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.trove_video);
            mediaController.setAnchorView(top_half__video);
            top_half__video.setMediaController(mediaController);
            top_half__video.setVideoURI(uri);
            top_half__video.start();
        }

        catch (NullPointerException e) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        // Do what you want.
    }

    @Override
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        // Do what you want.
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        // Do what you want.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        // Do what you want.
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        ActivityInit();
//    }

}
