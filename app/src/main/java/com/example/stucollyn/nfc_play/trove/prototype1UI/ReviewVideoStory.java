package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.stucollyn.nfc_play.R;

import java.io.File;

public class ReviewVideoStory extends AppCompatActivity {

    TextView instruction;
    MediaController mediaController, fullScreenMediaController;
    VideoView captured_video, full_sized_video;
    ImageButton expand_video_button, shrink_video_button;
    ImageView captured_video_background, full_screen_video_background;
    boolean is_fullscreen_video_on = false;
    File videoFile;
    Uri story_directory_uri;
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
        getSupportActionBar().setTitle("Play Video Story");
        videoFile = (File)getIntent().getExtras().get("VideoFile");
        setContentView(R.layout.activity_review_video_story);

        captured_video = (VideoView) findViewById(R.id.review_video_story_captured_video);
        full_sized_video = (VideoView) findViewById(R.id.review_video_story_full_sized_video);
        expand_video_button = (ImageButton) findViewById(R.id.review_video_story_expand_video_button);
        shrink_video_button = (ImageButton) findViewById(R.id.review_video_story_shrink_video_button);
        captured_video_background = (ImageView) findViewById(R.id.review_video_story_captured_video_background);
        full_screen_video_background = (ImageView) findViewById(R.id.review_video_story_full_screen_video_background);
        mediaController = new MediaController(this);
        fullScreenMediaController = new MediaController(this);

        story_directory_uri = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                videoFile);

        ShowVideo();
    }

    public void ShowVideo() {

        captured_video.setVisibility(View.VISIBLE);
        captured_video_background.setVisibility(View.VISIBLE);
        captured_video.setVideoURI(story_directory_uri);
        mediaController.setAnchorView(captured_video);
        captured_video.setMediaController(mediaController);
        captured_video.start();

    }

    @Override
    public void onBackPressed() {

        finish();
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