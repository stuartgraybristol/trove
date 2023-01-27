package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
The Commentary Instruction class is used to generate trove's voice and audio instructions.
 */
public class CommentaryInstruction {

    //Activity variables
    Activity activity;
    Context context;

    //Media player variables
    MediaPlayer mPlayer;
    Uri audioFileUri;
    boolean playbackStatus = false;
    boolean authenticated = false;
    Handler inputHandler;
    float volume = 0;

    //Storage variables
    String tag_data;
    String currentActivityName;

    //Commentary Instruction constructor
    public CommentaryInstruction(Activity activity, Context context, boolean playbackStatus, boolean authenticated) {

        this.activity = activity;
        this.context = context;
        this.playbackStatus = playbackStatus;
        this.authenticated = authenticated;
        mPlayer = new MediaPlayer();
    }

    /*When audio playback buttons are selected for first time, setup new audio media player. When
    user interacts with playback buttons after audio media player has already been setup, toggle
    between media player pause and play*/
    public void onPlay(Uri audioFileUri, final boolean onCompleteChangeActivitiy, final Class targetActivityName, String currentActivityName) {

        //Set the volume to 1 when the commentary instruction is played
        volume = 1;
        //Setup the audio media player
        setupAudioMediaPlayer(audioFileUri);
        //If there is not already commentary playing, start playing a new audio commentary
        if (!playbackStatus) {
            startPlaying(onCompleteChangeActivitiy, targetActivityName, currentActivityName);
            playbackStatus = true;
        }
    }

    //Setup new audio media player drawing from audio file location
    protected void setupAudioMediaPlayer(Uri audioFileUri) {

        //If there is already a commentary instruction being played, stop it and reset the player
        try {

            if(mPlayer!=null) {
                mPlayer.stop();
                mPlayer.reset();
                playbackStatus = false;
            }

            //Set the commentary audio file data source
            mPlayer.setDataSource(context, audioFileUri);
            mPlayer.prepare();

            //Catch any errors
        } catch (IOException e) {
            Log.e("Error", "prepare() failed");
        }
    }

    //Start audio media player and start listening for stop imageView to be pressed. Upon completion of instruction
    public void startPlaying(final boolean onCompleteChangeActivity, final Class activityName, final String currentActivityName) {

        //Start the media player
        mPlayer.start();

        //Listen for the current audio instruction to finish, then take appropriate action
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {

                //Stop the media player
                mPlayer.stop();
                //Reset the player
                mPlayer.reset();
                //Set the current playbackStatus to false
                playbackStatus = false;

                //When the instruction is finished, choose whether to launch a new activity or to do nothing
                if(onCompleteChangeActivity) {

                    if(activityName==HomeScreen.class) {

                        HomeScreen();
                    }

                    else if(activityName==Archive.class) {

                        ArchiveMainMenu();
                    }

                    else {

                    }
                }
            }
        });
    }

    //Launch the Archive activity
    void ArchiveMainMenu() {

        Intent intent = new Intent(context, Archive.class);
        intent.putExtra("PreviousActivity", "RecordStory");
        intent.putExtra("Authenticated", authenticated);
        context.startActivity(intent);
    }

    //Launch the HomeScreen activity
    void HomeScreen() {

        Intent intent = new Intent(context, HomeScreen.class);
        intent.putExtra("PreviousActivity", "RecordStory");
        intent.putExtra("Authenticated", authenticated);
        intent.putExtra("NewStory", true);
        intent.putExtra("storyRef", tag_data);
        context.startActivity(intent);
    }

    //Stop the media player and reset it for reuse
    public void stopPlaying() {

        if(mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.reset();
            playbackStatus = false;
        }
    }

    //Return the current media player
    MediaPlayer getmPlayer(){

        return mPlayer;
    }


    //Note: Currently not used. Can be used to faze audio out.
    void startFadeOut(){

        if(mPlayer.isPlaying()) {
            final int FADE_DURATION = 500; //The duration of the fade
            //The amount of time between volume changes. The smaller this is, the smoother the fade
            final int FADE_INTERVAL = 250;
            final int MIN_VOLUME = -1; //The volume will increase from 0 to 1
            int numberOfSteps = FADE_DURATION / FADE_INTERVAL; //Calculate the number of fade steps
            //Calculate by how much the volume changes each step
            final float deltaVolume = MIN_VOLUME / (float) numberOfSteps;

            //Create a new Timer and Timer task to run the fading outside the main UI thread
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    fadeOutStep(deltaVolume); //Do a fade step
                    //Cancel and Purge the Timer if the desired volume has been reached
                    if (volume <= 0) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            };

            timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
        }
    }

    private void fadeOutStep(float deltaVolume){
        mPlayer.setVolume(volume, volume);
        volume += deltaVolume;
    }

}
