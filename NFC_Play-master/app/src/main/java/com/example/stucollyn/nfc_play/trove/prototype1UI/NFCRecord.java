package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/* The NewStoryRecord Activity is the main class for dealing with recording of different media.
* It takes in the types of media defined in StoryMediaChooser and lets the user record stories
* using the types selected. The Activity launches different fragments to deal with the different
* media, and communicates events with the fragments which they then display to the user.*/
public class NFCRecord extends AppCompatActivity implements Serializable {

    //General Variables
    boolean record_button_on, video_record_button_on, recordingStatus = false,
            playbackStatus = false, mPlayerSetup = false, fullSizedPicture = false,
            permissionToRecordAccepted = false, isFullSizedVideo = false;
    int fragmentArrayPosition = 0, rotationInDegrees;
    String testData = "Lies", audioPath, photoPath, videoPath, writtenPath;
    HashMap<String, String> recordedMediaHashMap = new HashMap<String, String>();
    Bitmap adjustedFullSizedBitmap, adjustedBitmap;

    //Media Recorder Variables
    MediaRecorder recordStory;
    MediaPlayer.OnCompletionListener audio_stop_listener;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    //Fragment Variables
    ArrayList<Fragment> fragmentNameArray;
    Layout pictureLayout;
    AudioStoryFragment audio_story_fragment;
    PictureStoryFragment picture_story_fragment;
    VideoStoryFragment video_story_fragment;
    WrittenStoryFragment written_story_fragment;
    FragmentTransaction ft;
    ArrayList<String> selectedMedia;

    //Request Code Variables
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    //File Save Variables
    private static String audioFileName = null, pictureFileName = null, videoFileName = null;
    File image, video;
    Uri videoURI, photoUri;
    File story_directory;
    String story_directory_path;
    Uri story_directory_uri;
    String tag_data;

    //Classes
    AudioRecorder audioRecorder;
    VideoRecorder videoRecorder;
    PictureRecorder pictureRecorder;
    WrittenRecorder writtenRecorder;

    View v;
    View button;
    int mode;

    //Grant permission to record audio (required for some newer Android devices)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    //onCreate called when Activity begins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       ActivityInit();
    }

    void ActivityInit() {
        setContentView(R.layout.activity_record);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        v = getLayoutInflater().inflate(R.layout.activity_record, null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBarSetup();

        //Request permission to record audio (required for some newer Android devices)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        /*Receive array list of selected media types from StoryMediaChooser Activity and copy this
        list to a selectedMedia array list in this Activity*/
        selectedMedia = (ArrayList<String>) getIntent().getSerializableExtra("Fragments");
        InitFragments();
        SetupStoryLocation();
    }

//    public Date getDateFromString(String datetoSaved){
//
//        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//        try {
//            Date date = format.parse(datetoSaved);
//            return date ;
//        } catch (ParseException e){
//            return null ;
//        }
//
//    }

    //Setup new storage folder
    private void SetupStoryLocation() {

        String packageLocation = ("/Stories");
        String timeStamp = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH).format(new Date());
        String name = UUID.randomUUID().toString();
        String newDirectory = packageLocation + "/" + name;
        tag_data = name;
        story_directory = getExternalFilesDir(newDirectory);
        // story_directory_uri = FileProvider.getUriForFile(this,
        //       "com.example.android.fileprovider",
        //     story_directory);
        story_directory_path = story_directory.getAbsolutePath();
    }

    //Setup action bar
    private void ActionBarSetup() {

        //Display both title and image, and a back imageView in action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set and show trove logo in action bar
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Set page title shown in action bar
        getSupportActionBar().setTitle("Create New Story");
    }

    //Initialize fragments for use
    private void InitFragments() {

        //Initialize new fragment instances
        audio_story_fragment = new AudioStoryFragment();
        picture_story_fragment = new PictureStoryFragment();
        video_story_fragment = new VideoStoryFragment();
        written_story_fragment = new WrittenStoryFragment();
        fragmentNameArray = new ArrayList<Fragment>();

        /*Create and populate hash map, linking strings of possible media types with a corresponding
        fragment*/
        HashMap<String, Fragment> mediaFragmentLookup = new HashMap<String, Fragment>();
        mediaFragmentLookup.put("Audio", audio_story_fragment);
        mediaFragmentLookup.put("Picture", picture_story_fragment);
        mediaFragmentLookup.put("Video", video_story_fragment);
        mediaFragmentLookup.put("Written", written_story_fragment);

        /*Iterate through the array list of the user's selected media (e.g. String [Audio, Picture,
        Video, Written]). For every selected media entry, look up the corresponding fragment in the
        hash map. Add this fragment to an array list of fragments to be used in this current story.*/
        for (int i = 0; i < selectedMedia.size(); i++) {

            fragmentNameArray.add(mediaFragmentLookup.get(selectedMedia.get(i)));
        }

        //Open first fragment in the fragment array list
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragmentNameArray.get(fragmentArrayPosition));
        ft.commit();
    }

    //When red audio record imageView is pressed, activate audio recording sequence
    public void AudioRecordButton(View view) {

//        new ProcessAudio().execute(view);

            recordingStatus = !recordingStatus;
            audio_story_fragment.AudioRecordButtonSwitch(recordingStatus, button);
            onRecord(recordingStatus, button);
            audioFileName = audioRecorder.getAudioFileName();
    }

    /*When audio recording is started, try to startRecording(). When audio recording is stopped,
    stopRecording() and change fragment views accordingly*/
    public void onRecord(boolean start, View view) {
        if (start) {

            try {
                audioRecorder = new AudioRecorder(this, story_directory);
                audioRecorder.startRecording();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

        } else {
            audioRecorder.stopRecording();
            audio_story_fragment.PlayBackAndSaveSetup(view);
        }
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

        audio_story_fragment.PlaybackButtonSwitch(playbackStatus, view);
    }

    //Setup new audio media player drawing from audio file location
    protected void setupAudioMediaPlayer() {
        Log.i("audio file", audioFileName);

        Uri audioFileUri = FileProvider.getUriForFile(this,
                "com.example.android.fileprovider",
                audioRecorder.getAudioFile());

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(this, audioFileUri);
            mPlayer.prepare();
            mPlayerSetup = true;
        } catch (IOException e) {
            Log.e("Error", "prepare() failed");
        }
    }

    //Start audio media player and start listening for stop imageView to be pressed
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
            audio_story_fragment.PlaybackButtonSwitch(playbackStatus, view);
        }
    }

    public void DiscardAudio(View view) {

        audioRecorder.DiscardAudio();
        audio_story_fragment.ResetView(view);
    }

    public void CompleteAudioRecording(View view) {

        recordedMediaHashMap.put("Audio", audioFileName);
        UpdateFragment();
    }


    //Picture Recording

    public void PictureRecordButton(View view) {

        try {

            picture_story_fragment.TakePicture(view);
            pictureRecorder = new PictureRecorder(this, this, story_directory);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    pictureRecorder.dispatchTakePictureIntent();
                }
            }, 500);
        } catch (NullPointerException e) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //Picture Processing
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

                new ProcessPicture().execute();
            }
        }

        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {

               new ProcessVideo().execute();

            }
        }
    }

    public void FullSizedPicture(View view) {


        Intent openFullSize = new Intent(Intent.ACTION_VIEW);
        openFullSize.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //Uri photoURI = FileProvider.getUriForFile(this,
        //  "com.example.android.fileprovider",
        //   file);
        openFullSize.setDataAndType(photoUri, "image/");
        openFullSize.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(openFullSize);
        //Intent openFullSize = new Intent(Intent.ACTION_VIEW, Uri.parse(photoPath));
        try {
            startActivity(openFullSize);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(NFCRecord.this,
                    "Error showing image", Toast.LENGTH_LONG).show();
        }
    }

    public void DiscardPicture(View view) {

        picture_story_fragment.DiscardPicture();
    }

    public void CompletePictureRecording(View view) {

        pictureFileName = photoPath;
        recordedMediaHashMap.put("Picture", pictureFileName);
        UpdateFragment();
    }

    //Video Recording
    public void VideoRecordButton(View view) {

        try {
            video_story_fragment.TakeVideo(view);
            videoRecorder = new VideoRecorder(this, this, story_directory, story_directory_path);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    //dispatchTakeVideoIntent();
                    videoRecorder.dispatchTakeVideoIntent();
                }
            }, 1000);
        } catch (NullPointerException e) {

        }

    }

    public void FullSizedVideo(View view) {

        video_story_fragment.ShowFullSizedVideo(isFullSizedVideo, videoURI);
        isFullSizedVideo = !isFullSizedVideo;
    }

    public void DiscardVideo(View view) {

        video_story_fragment.DiscardVideo();
    }

    public void CompleteVideoRecording(View view) {

        recordedMediaHashMap.put("Video", videoPath);
        UpdateFragment();
    }


    //Written Recording
    public void WriteStory(View view) {

        written_story_fragment.StartWritingNotification(view);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                written_story_fragment.StartWriting();
            }
        }, 500);

    }

    public void DiscardWrittenStory(View view) {

        written_story_fragment.DiscardWriting();
    }

    public void CompleteWrittenStory(View view) {

        try {
            writtenRecorder = new WrittenRecorder(written_story_fragment, this, this, story_directory);
            writtenRecorder.createWrittenFile();
            writtenPath = writtenRecorder.getWrittenFilePath();
        } catch (IOException e) {

        }

        recordedMediaHashMap.put("Written", writtenPath);
        UpdateFragment();
    }

    public void UpdateFragment() {

        if (fragmentArrayPosition < fragmentNameArray.size() - 1) {

            fragmentArrayPosition++;
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_frame, fragmentNameArray.get(fragmentArrayPosition));
            ft.commit();
        } else {

            Intent intent = new Intent(NFCRecord.this, NewStoryReview.class);
            intent.putExtra("Orientation", mode);
            intent.putExtra("RecordedMedia", recordedMediaHashMap);
            intent.putExtra("StoryDirectory", story_directory);
            intent.putExtra("TagData", tag_data);
            NFCRecord.this.startActivity(intent);
            overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            finish();
        }
    }

    public void Skip(View view) {

        UpdateFragment();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(NFCRecord.this, StoryMediaChooser.class);
        intent.putExtra("Orientation", mode);
        NFCRecord.this.startActivity(intent);
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

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        ActivityInit();
//    }

    class ProcessAudio extends AsyncTask<View, Void, Void> {
        @Override
        protected Void doInBackground(View... params) {

            button = params[0];

            try {
                recordingStatus = !recordingStatus;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        audio_story_fragment.AudioRecordButtonSwitch(recordingStatus, button);
                        onRecord(recordingStatus, button);
                    }
                });


                audioFileName = audioRecorder.getAudioFileName();
            } catch (NullPointerException e) {

            } catch (IllegalArgumentException e) {

            }

            return null;

        }

        protected void onPostExecute(Void result) {

        }
    }

    class ProcessPicture extends AsyncTask<View, Void, Void> {

        Bitmap processedBitmap;

        @Override
        protected Void doInBackground(View... params) {

//            imageView = params[0];

            try {

                pictureRecorder.PictureProcessing();
                photoPath = pictureRecorder.getPhotoPath();
                photoUri = pictureRecorder.getPhotoURI();
                picture_story_fragment.setPictureBoxDimensions(pictureRecorder.getRotationInDegrees());
                processedBitmap = pictureRecorder.getAdjustedBitmap();

            } catch (NullPointerException e) {

            } catch (IllegalArgumentException e) {

            }

            return null;

        }

        protected void onPostExecute(Void result) {

            picture_story_fragment.ShowPicture(processedBitmap);

        }
    }

    class ProcessVideo extends AsyncTask<View, Void, Void> {

        Bitmap processedBitmap;

        @Override
        protected Void doInBackground(View... params) {

//            imageView = params[0];

            try {

                videoRecorder.VideoProcessing();
                videoPath = videoRecorder.getVideoPath();
                videoURI = videoRecorder.getVideoURI();

            } catch (NullPointerException e) {

            } catch (IllegalArgumentException e) {

            }

            return null;

        }

        protected void onPostExecute(Void result) {

            video_story_fragment.ShowVideo(videoURI);

        }
    }
}



// catch (NullPointerException e) {
//
//         Log.i("Exception", "NullPointerException");
//         Bitmap fullSizedBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.baloon1);
//         Matrix matrix = new Matrix();
//         matrix.postRotate(90);
//         adjustedBitmap = Bitmap.createBitmap(fullSizedBitmap, 0, 0, fullSizedBitmap.getWidth(), fullSizedBitmap.getHeight(), matrix, true);
//         }
//
//         catch (IllegalArgumentException e) {
//
//         Log.i("Exception", "IllegalArgumentException");
//         Bitmap fullSizedBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.baloon1);
//         Matrix matrix = new Matrix();
//         matrix.postRotate(90);
//         adjustedBitmap = Bitmap.createBitmap(fullSizedBitmap, 0, 0, fullSizedBitmap.getWidth(), fullSizedBitmap.getHeight(), matrix, true);
//         }
