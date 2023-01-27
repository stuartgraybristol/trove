package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
This class is called when the user requests to view a story - either audio or image, from the archive or after scanning a tag.
In response, this class plays any audio media files and launches a ShowStoryContentDialog box, showing the associated image.
 */

public class ShowStoryContent {

    //Activity variables
    Context context;
    Activity activity;

    //Story playback
    ShowStoryContentDialog newFragment;
    MediaPlayer mPlayer;
    File[] filesOnTag;
    FragmentManager ft;


    //ShowStoryContent constructor
    public ShowStoryContent(MediaPlayer mPlayer, Context context, Activity activity, File[] filesOnTag) {

        this.mPlayer = mPlayer;
        this.context = context;
        this.activity = activity;
        this.filesOnTag = filesOnTag;
        ft = ((FragmentActivity)activity).getSupportFragmentManager();
    }

    //Run when a story file is selected within the archive
    void checkFilesOnArchive(){

        for(int i=0; i<filesOnTag.length; i++) {

            String extension = FilenameUtils.getExtension(filesOnTag[i].toString());
            String fileName = filesOnTag[i].toString();

            if(extension.equalsIgnoreCase("mp4")) {

                String substring=fileName.substring(fileName.lastIndexOf("/")+1);

                Uri story_directory_uri = FileProvider.getUriForFile(context,
                        "com.example.android.fileprovider",
                        filesOnTag[i]);

                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(context, story_directory_uri);
                    mPlayer.prepare();
                    mPlayer.start();
                }

                catch (IOException e) {
                    Log.e("Error", "prepare() failed");
                }

            }

            if(extension.equalsIgnoreCase("jpg")) {

                showStoryContentDialog(filesOnTag[i]);
            }
        }
    }

    //Run when a story file is selected from a tag scan
    void checkFilesOnTag(){

        for(int i=0; i<filesOnTag.length; i++) {

            Log.i("Files on Tag", String.valueOf(filesOnTag[i].toString()));

            String extension = FilenameUtils.getExtension(filesOnTag[i].toString());
            String fileName = filesOnTag[i].toString();

            if(extension.equalsIgnoreCase("mp3")) {

                Uri story_directory_uri = FileProvider.getUriForFile(context,
                        "com.example.android.fileprovider",
                        filesOnTag[i]);

                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(context, story_directory_uri);
                    mPlayer.prepare();
                    mPlayer.start();
                }

                catch (IOException e) {
                    Log.e("Error", "prepare() failed");
                }

            }

            if(extension.equalsIgnoreCase("jpg")) {

                showStoryContentDialog(filesOnTag[i]);
            }
        }
    }

    //Return fragment
    ShowStoryContentDialog returnDialog() {

        return newFragment;
    }

    //Close any fragments left open
    void closeOpenFragments(ShowStoryContentDialog currentFragment) {

        if(currentFragment!=null) {

            currentFragment.dismiss();
        }
    }

    //Launch new fragment dialog activity
    void showStoryContentDialog(File imageFile) {
        // Create an instance of the dialog fragment and show it
        Bundle bundle = new Bundle();
        ft = ((FragmentActivity)activity).getSupportFragmentManager();
        newFragment = new ShowStoryContentDialog();
        bundle.putSerializable("ImageFile", imageFile);
        newFragment.setArguments(bundle);
        newFragment.setCancelable(false);
        newFragment.show(ft, "mydialog");
    }
}