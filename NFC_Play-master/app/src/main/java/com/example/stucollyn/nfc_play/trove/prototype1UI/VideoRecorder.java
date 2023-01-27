package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by StuCollyn on 06/06/2018.
 */

class VideoRecorder  extends Application {

    //File Save Variables
    private static String videoFileName = null;
    File story_directory, video;
    private Context context;
    Uri videoURI;
    String videoPath;
    Activity activity;
    String story_directory_path;

    VideoRecorder(Activity activity, Context context, File story_directory, String story_directory_path) {
        this.context=context;
        this.story_directory = story_directory;
        this.activity = activity;
        this.story_directory_path = story_directory_path;

    }

    void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(context.getPackageManager()) != null) {

            // Create the File where the photo should go
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // progressToRecordStory only if the File was successfully created
            if (videoFile != null) {
              //  videoURI = FileProvider.getUriForFile(this,
                //        "com.example.android.fileprovider",
                  //      videoFile);

                videoURI = FileProvider.getUriForFile(context, "com.example.android.fileprovider", videoFile);
                Log.i("video URI: ", videoURI.toString());
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                activity.startActivityForResult(takeVideoIntent, 200);
            }
        }
    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MPEG4_" + timeStamp + "_";
        File storageDir;

        if (Build.VERSION.SDK_INT >= 19) {
            //storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            storageDir = story_directory;
        }

        else {
            //storageDir = context.getExternalFilesDir(Environment.getExternalStorageDirectory() + "/Documents");
            storageDir = story_directory;
        }

        video = File.createTempFile(videoFileName, ".mp4", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        videoPath = video.getAbsolutePath();
        return video;
    }

    void VideoProcessing() {

        MediaMetadataRetriever m = new MediaMetadataRetriever();

        m.setDataSource(videoPath);
        Bitmap thumbnail = m.getFrameAtTime();
//
        if (Build.VERSION.SDK_INT >= 17) {
            String s = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

            Log.e("Rotation", s);
        }
    }

    Uri getVideoURI() {

        return videoURI;
    }

    String getVideoPath() {

        return videoPath;
    }
}
