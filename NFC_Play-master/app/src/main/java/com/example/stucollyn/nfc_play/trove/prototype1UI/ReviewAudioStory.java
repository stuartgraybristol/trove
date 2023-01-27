package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.io.IOException;

public class ReviewAudioStory extends AppCompatActivity {

    ImageButton pause_play_button, stop_button;
    TextView instruction;
    private MediaPlayer mPlayer = null;
    boolean mPlayerSetup = false, playbackStatus = false;
    File audioFile;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setTitle("Play Audio Story");
        audioFile = (File)getIntent().getExtras().get("AudioFile");
        setContentView(R.layout.activity_review_audio_story);

        pause_play_button = findViewById(R.id.review_audio_story_pause_play_button);
        stop_button = findViewById(R.id.review_audio_story_stop_button);
        instruction = findViewById(R.id.review_audio_story_instruction);
    }

    public void PlaybackButtonSwitch(boolean audioplaying) {

        instruction.setText("Press the green tick to save or the red cross to delete and record something new.");

        if(!audioplaying) {
            pause_play_button.setImageResource(R.drawable.play_arrow_black);
        }

        else {
            pause_play_button.setImageResource(R.drawable.pause_black);
        }

        audioplaying = !audioplaying;
    }

    /*When audio playback buttons are selected for first time, setup new audio media player. When
  user interacts with playback buttons after audio media player has already been setup, toggle
  between media player pause and play*/
    public void onPlay(View view) {
        if (!mPlayerSetup) {
            setupAudioMediaPlayer();
        }

        if (!playbackStatus) {
            startPlaying(view);
            playbackStatus = true;
        } else {
            pausePlaying(view);
            playbackStatus = false;
        }

        PlaybackButtonSwitch(playbackStatus);
    }

    //Setup new audio media player drawing from audio file location
    protected void setupAudioMediaPlayer() {

         Uri story_directory_uri = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    audioFile);

            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(this, story_directory_uri);
                mPlayer.prepare();
            mPlayerSetup = true;
        } catch (IOException e) {
            Log.e("Error", "prepare() failed");
        }
    }

    //Start audio media player and start listening for stop button to be pressed
    public void startPlaying(View view) {
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying(findViewById(R.id.stop_button));
            }

        });
    }

    //Pause audio media player
    public void pausePlaying(View view) {

        mPlayer.pause();
    }

    //Stop audio media player, delete current media player (requires new setup for future playback)
    public void stopPlaying(View view) {

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mPlayerSetup = false;
            playbackStatus = false;
            PlaybackButtonSwitch(playbackStatus);
        }
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
