package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ShowCloudStoryContent extends AppCompatActivity {

    int mode;
    String storyType;
    String storyName;
    StoryRecord thisStory;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    StorageReference riversRef;
    private StorageReference mStorageRef;
    FirebaseStorage storage;
    ImageView showImage;
    ImageView captured_image;
    File pictureFile, writtenFile, videoFile, audioFile;
    Uri story_directory_uri;
    Bitmap adjustedFullSizedBitmap, adjustedBitmap;
    int rotationInDegrees;
    File story_directory;
    String newDirectory;
    StorageReference gsReference;
    String userID;
    MediaController mediaController, fullScreenMediaController;
    VideoView captured_video, full_sized_video;
    ImageButton expand_video_button, shrink_video_button;
    ImageView captured_video_background, full_screen_video_background;
    boolean is_fullscreen_video_on = false;
    ProgressBar progressBar;
    private StringBuilder text = new StringBuilder();
    TextView written_text_story;
    ImageButton pause_play_button, stop_button;
    TextView instruction;
    private MediaPlayer mPlayer = null;
    boolean mPlayerSetup = false, playbackStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Play Cloud Story");
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setContentView(R.layout.activity_show_cloud_story_content);
        showImage = (ImageView) findViewById(R.id.cloud_show_image);
        written_text_story = (TextView) findViewById(R.id.written_text_story);
        pause_play_button = findViewById(R.id.review_audio_story_pause_play_button);
        stop_button = findViewById(R.id.review_audio_story_stop_button);
        instruction = findViewById(R.id.review_audio_story_instruction);
        progressBar = (ProgressBar) findViewById(R.id.cloud_progressbar);
        storyType = (String) getIntent().getExtras().get("StoryType");
        storyName = (String) getIntent().getExtras().get("StoryName");
        thisStory = (StoryRecord) getIntent().getExtras().get("StoryRecordArray");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        newDirectory = ("/Cloud");
        story_directory = getExternalFilesDir(newDirectory);
        userID = mAuth.getCurrentUser().getUid();
        gsReference = storage.getReferenceFromUrl(thisStory.getStoryRef());

        Log.i("story Type", storyType);

        if(storyType.equals("PictureFile")) {

            showImage.setVisibility(View.VISIBLE);
            PictureFile();
        }

        else if(storyType.equals("WrittenFile")) {

            WrittenFile();
        }

        else if(storyType.equals("VideoFile")) {

            VideoFile();
        }

        else if(storyType.equals("AudioFile")) {

            AudioFile();
        }
    }

    void WrittenFile() {

        try {

            writtenFile = File.createTempFile("text", ".txt", story_directory);

            gsReference.getFile(writtenFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.i("Success: ", "Found you stuff");
                    ShowWriting();

                    // Local temp file has been created
                    //ShowWritten();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        } catch (IOException error) {

        }
    }

    void VideoFile() {

        Log.i("VideoFile Found: ", "True");

        try {

            videoFile = File.createTempFile("video", ".mp4", story_directory);

            gsReference.getFile(videoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.i("Success: ", "Found you stuff");

                    // Local temp file has been created
                    ShowVideo();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        } catch (IOException error) {

        }
    }

    void AudioFile() {

        try {

            audioFile = File.createTempFile("audio", ".mp3", story_directory);

            gsReference.getFile(audioFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.i("Success: ", "Found you stuff");

                    // Local temp file has been created
                    ShowAudio();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        } catch (IOException error) {

        }
    }

    void PictureFile() {

        try {

            pictureFile = File.createTempFile("images", ".jpg", story_directory);

            gsReference.getFile(pictureFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.i("Success: ", "Found you stuff");

                    // Local temp file has been created
                    ShowPicture();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        } catch (IOException error) {

        }
    }

    void ShowVideo() {

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

        captured_video.setVisibility(View.VISIBLE);
        captured_video_background.setVisibility(View.VISIBLE);
        captured_video.setVideoURI(story_directory_uri);
        mediaController.setAnchorView(captured_video);
        captured_video.setMediaController(mediaController);
        progressBar.setVisibility(View.INVISIBLE);
        captured_video.start();
    }

    void ShowPicture() {

//        Bitmap bmp = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
//        showImage.setImageResource(R.drawable.audio_icon);

        ExifInterface exif = null;
        try {
           exif = new ExifInterface(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        rotationInDegrees = exifToDegrees(rotation);


        /*
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int smallSizeScaleFactor = Math.min(photoW / 300, photoH / 300);


        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = smallSizeScaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
        */

        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
        Matrix matrix = new Matrix();
        if (rotation != 0f) {
            matrix.preRotate(rotationInDegrees);
        }

        if (rotationInDegrees == 90 || rotationInDegrees == 270) {
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else if (rotationInDegrees == 180) {
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            // adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            adjustedBitmap = bitmap;
        }

//        showImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        showImage.setImageBitmap(adjustedBitmap);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (
                exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    void ShowWriting() {

        progressBar.setVisibility(View.INVISIBLE);
        written_text_story.setVisibility(View.VISIBLE);
        BufferedReader reader = null;

        try {
            FileReader in = new FileReader(writtenFile);
            reader = new BufferedReader(in);

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }

            written_text_story.setText((CharSequence) text);

        }
    }

    void ShowAudio() {

        progressBar.setVisibility(View.INVISIBLE);
        pause_play_button.setVisibility(View.VISIBLE);
        stop_button.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.VISIBLE);
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
