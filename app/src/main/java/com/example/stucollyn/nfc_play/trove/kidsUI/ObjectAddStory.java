package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.stucollyn.nfc_play.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 The ObjectAddStory activity is used for the recording of an audio or picture story about an existing object. In the activity, the user can choose to record an audio or
 picture story in the same way it is done in the RecordStory activity. Afterwards, they can either choose to rerecord their story or take a picture of the object which
 relates to the story.
 */

/*
To do: There appears to be an intermittent bug in this activity where the app crashes after recording a new story. The screen goes white and returns to the archive.
To do: There is also no functionality to attach these new stories to nfc tags yet.
 */

public class ObjectAddStory extends AppCompatActivity {

    //The View group and ImageViews displayed on the activity layout
    ImageView recordButton, cameraButton, back;

    //Animations
    Animation fadein, fadeout;
    AnimatedVectorDrawable recordButtonAnim, backBegin, backRetrace;
    Drawable recordButtonNonAnim;
    Animation slideout, slidein;

    //Handlers, runnables, and logical components
    Handler archiveStoryHandler;
    Handler animationHandler, animationBackHandler;
    Runnable RecordButtonRunnable;

    //FireBase
    boolean authenticated = false;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Date FireStoreTime;
    FirebaseStorage storage;
    private StorageReference mStorageRef;

    //NFC Components
    NFCInteraction nfcInteraction;
    Tag mytag;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean newStoryReady = false;

    //File Save Variables
    File story_directory;
    File tag_directory = null;
    File cover_directory = null;
    HashMap<String, ArrayList<ObjectStoryRecord>> objectRecordMap;
    String objectName;

    //Camera Variables
    CameraRecorder cameraRecorder;
    private Camera mCamera;
    private CameraPreview mPreview;
    ImageButton captureButton;
    FrameLayout preview;
    LinearLayout camera_linear;

    //Request Code Variables
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    //File Save Variables
    private static String audioFileName = null, pictureFileName = null, videoFileName = null;
    File image;

    //Recording Controller
    AudioRecorder audioRecorder;
    boolean recordingStatus = false;
    boolean currentlyRecording = false;
    boolean permissionToRecordAccepted = false;

    //Commentary
    CommentaryInstruction commentaryInstruction;

    //Grant permission to record audio (required for some newer Android devices)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case CAMERA_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }

        if (!permissionToRecordAccepted) {

            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Layout associated with this activity
        setContentView(R.layout.activity_object_add_story_kids_ui);
        //Ensure screen always stays on and never dims
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Initialize current object content
        initializeCurrentObject();
        //Initialize views
        initializeViews();
        //Initialize camera components
        initializeCamera();
        //Check for data connection before allowing user to sign in via cloud account
        checkConnection();
        //Initialize commentary instructions
        initializeCommentary();
        //Initialize NFC components
        NFCSetup();
        //Initialize animations
        initializeAnimations();
        //Setup control logic for record button
        recordButtonController();
        //Setup the storage location of new stories
        setupStoryLocation();
        //Begin to animate imageViews
        beginAnimation();
        //Start record button control logic
        recordButtonController();
    }

    //Initialize commentary messages
    void initializeCurrentObject() {

        objectRecordMap = (HashMap<String, ArrayList<ObjectStoryRecord>>) getIntent().getExtras().get("ObjectStoryRecord");
        objectName = (String) getIntent().getExtras().get("objectName");
    }

    //Initialize NFC components.
    void NFCSetup() {

        //Initialize nfcInteraction object, which processes any nfc interactions.
        nfcInteraction = new NFCInteraction(this, this);
        //Initialize adapter to gather NFC tag data.
        adapter = NfcAdapter.getDefaultAdapter(this);
        //Enable external NFC software to launch an intent within this activity.
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] {
                tagDetected
        };
    }

    //Initialize commentary messages
    void initializeCommentary() {

        commentaryInstruction = new CommentaryInstruction(this, this, false, authenticated);
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.addnewobjectstory), false, RecordStory.class, "ObjectAddStory");
    }

    //Check for data connection before allowing user to sign in via cloud account.
    void checkConnection() {

        //Call method which checks for connection.
        boolean isNetworkConnected = isNetworkConnected();

        //If there is a data connection currently available for use, attempt to authenticate login details with Firebase,
        // allow user to login offline but without a profile and access only to local storage.
        if(isNetworkConnected) {

            //If there is an active data connection, set authenticated value to true
            authenticated = true;
        }

        else {

            //If there is an active data connection, set authenticated value to false
            authenticated = false;
        }
    }

    //Check whether a network connection is present.
    private boolean isNetworkConnected() {

        //Use Android connectivity manager to get the status of whether connected to a data connection.
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    void initializeCamera() {

        mCamera = cameraRecorder.getCameraInstance();
        mPreview = new CameraPreview(getApplicationContext(), mCamera);
        preview.addView(mPreview);
    }

    //Initialize views and layout components
    void initializeViews() {

        recordButton = (ImageView) findViewById(R.id.record);
        cameraButton = (ImageView) findViewById(R.id.camera);
        back = (ImageView) findViewById(R.id.back);
        captureButton = (ImageButton) findViewById(R.id.button_capture);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        camera_linear = (LinearLayout) findViewById(R.id.camera_linear);
    }

    //Initialize animations
    void initializeAnimations() {

        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        recordButtonAnim = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_record_anim_alt);
        recordButtonNonAnim = (Drawable) getDrawable(R.drawable.kids_ui_record_circle);
        backRetrace = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_close_2);
        backBegin = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_close_1);
        slideout = AnimationUtils.loadAnimation(this, R.anim.slideout);
        slidein = AnimationUtils.loadAnimation(this, R.anim.slidein);
    }

    //Animation and Layout Setup
    void beginAnimation() {

        animationBackHandler = new Handler();
        animationBackHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                back.setVisibility(View.VISIBLE);
                Drawable d = back.getDrawable();
                final AnimatedVectorDrawable zigzaganim = (AnimatedVectorDrawable) d;
                zigzaganim.start();
            }
        }, 2000);

        animationBackHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.kids_ui_close, null);
                d = DrawableCompat.wrap(d);
                DrawableCompat.setTint(d, Color.WHITE);
                back.setImageDrawable(d);

            }
        }, 3000);
    }

    //Record button logic
    void recordButtonController() {

//        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.holdrecordbutton), true, HomeScreen.class);

        recordButton.setOnTouchListener((new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout());
                        commentaryInstruction.stopPlaying();
                        currentlyRecording = true;
                        recordingStatus = false;
                        recordingManager(v);
                        recordButtonAnimationController();
                        break;
                    case MotionEvent.ACTION_UP:
//                        handler.removeCallbacks(mLongPressed);
                        currentlyRecording = false;
                        recordingStatus = true;
                        recordingManager(v);
                        recordButtonAnimationController();
//                        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.chime), false, HomeScreen.class, "ObjectAddStory");
                        SaveNewStory();
                        break;
                }

                return true;
            }
        }));
    }

    //Paint image views the default colour
    void paintViews() {

        int paintColour = android.graphics.Color.rgb(253, 195, 204);
        Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.kids_ui_back_anim, null);
        d = DrawableCompat.wrap(d);
        DrawableCompat.setTint(d, paintColour);
        back.setImageDrawable(d);
    }

    //Setup new storage folder
    private void setupStoryLocation() {

        archiveStoryHandler = new Handler();
        story_directory = new File (getFilesDir() + File.separator + "Stories" + File.separator + objectName);
    }

    //This handles the animation of the record button
    void recordButtonAnimationController() {

        //Create new Handler to repeat animation on the background thread
        animationHandler = new Handler();

        //Runnable to handle idle trove animation
        RecordButtonRunnable = new Runnable() {

            @Override
            public void run() {
                recordButtonAnim.start();

                //If currently recording, repeat animation in one second intervals
                if(currentlyRecording) {
                    animationHandler.postDelayed(this, 1000);
                }

                //Else, stop the animations and return the button image to default
                else {
                    animationHandler.removeCallbacks(RecordButtonRunnable);
                    recordButton.setImageDrawable(recordButtonNonAnim);
                }
            }
        };

        animationHandler.post(RecordButtonRunnable);
    }


    //This method sends commands to both the audio recorder and the button imageView
    void recordingManager(View view) {

        try {

            //If not already recording, set the record button image to the record drawable, and begin recording
            if (!recordingStatus) {

                recordButton.setImageDrawable(recordButtonAnim);
                //Request permission to record audio (required for some newer Android devices)
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                try {
                    //Create a new AudioRecorder object, responsible for managing the audio recording - preparing the media recorder, creating output files etc.
                    audioRecorder = new AudioRecorder(this, story_directory, null);
                    audioRecorder.startRecording();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                audioFileName = audioRecorder.getAudioFileName();
            }

            //Otherwise, stop recording audio, stop the animations, return the record button image to default, and set the new audio recording as ready to be saved to a tag
            else {

                audioRecorder.stopRecording();
                animationHandler.removeCallbacks(RecordButtonRunnable);
                recordButton.setImageDrawable(recordButtonNonAnim);
                newStoryReady = true;
            }

            //When the button is pressed, recordingStatus becomes the inverse of itself
            recordingStatus = !recordingStatus;
        }

        catch (RuntimeException r) {
        }
    }

    //Check the current system has a camera
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    //When the start camera button is pressed, do the following
    public void Camera(View view) {

        //Check application permission to use the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

        //Create a new CameraRecorder object, and begin to animate camera components into the layout
        try {

            cameraRecorder = new CameraRecorder(this, this, story_directory, tag_directory, cover_directory, mCamera, mPreview);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms

                    camera_linear.startAnimation(fadein);
                    captureButton.startAnimation(fadein);
                    preview.startAnimation(fadein);
                    preview.setVisibility(View.VISIBLE);
                    captureButton.setVisibility(View.VISIBLE);
                    camera_linear.setVisibility(View.VISIBLE);

                    //When the camera 'take photo' button is pressed
                    captureButton.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // get an image from the camera
                                    captureButton.setImageResource(R.drawable.kids_ui_record_anim_alt_mini);
                                    mCamera.takePicture(null, null, mPicture);
                                }
                            }
                    );

                }
            }, 500);
        }

        catch (NullPointerException e) {

        }
    }


    //Callback method for when a picture is returned by the camera
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Return screen to default by closing camera components
            camera_linear.startAnimation(fadeout);
            captureButton.startAnimation(fadeout);
            captureButton.setVisibility(View.GONE);
            preview.setVisibility(View.GONE);
            camera_linear.setVisibility(View.GONE);

            //Get camera snapshot picture in a file
            File pictureFile = cameraRecorder.getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("Tag", "Error creating media file, check storage permissions");
                return;
            }

            //Write byte values to picture file
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("Tag", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Tag", "Error accessing file: " + e.getMessage());
            }

            //Async task for picture processing: currently not in use
//            new RecordStory.ProcessPicture().execute();

            //Generate a new unique identifier for the new image story
            UUID newImageUUID = UUID.randomUUID();

            //If the user is connected to the network, create a new saveToCloud object for saving the story to FireStore storage
            if(authenticated) {
                SaveToCloud saveToCloud = new SaveToCloud(story_directory, newImageUUID);
                saveToCloud.CloudSaveNewStory();
            }

            //Commentary instruction, instructing users to save files on tag
//            commentaryInstruction.setTagData(tag_data);
            SaveNewStory();
        }
    };

    //Reset the current camera to take a new picture
    void ResetCamera() {

        if(mCamera!=null) {
            mCamera.stopPreview();
            captureButton.setImageResource(R.drawable.kids_ui_record_circle_mini);
            mCamera.startPreview();
        }
    }

    //Release the current camera
    void ReleaseCamera() {

        if(mCamera!=null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    //Activity Governance
    @Override
    public void onPause(){
        super.onPause();
//        nfcInteraction.WriteModeOff(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
//        nfcInteraction.WriteModeOn(adapter, pendingIntent, writeTagFilters);
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
    }

    public void onDestroy() {

        super.onDestroy();

    }

    // Restore UI state from the savedInstanceState.
    // This bundle has also been passed to onCreate.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        authenticated = savedInstanceState.getBoolean("Authenticated");
        objectName = savedInstanceState.getString("objectName");
        objectRecordMap = (HashMap<String, ArrayList<ObjectStoryRecord>>) savedInstanceState.getSerializable("ObjectStoryRecord");
    }

    // Save the current state of these bundled variables.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean("Authenticated", authenticated);
        savedInstanceState.putString("objectName", objectName);
        savedInstanceState.putSerializable("ObjectStoryRecord", objectRecordMap);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    //Functionality for a home button
    //Note, not currently in use
    public void Home(View view) {

        back.setClickable(false);
        ResetCamera();
        commentaryInstruction.stopPlaying();
        animationBackHandler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(ObjectAddStory.this, HomeScreen.class);
        intent.putExtra("PreviousActivity", "ObjectAddStory");
        intent.putExtra("Authenticated", authenticated);
        ObjectAddStory.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //This method is called after a new audio or picture story file has been captured
    public void SaveNewStory() {

        commentaryInstruction.stopPlaying();
        back.setClickable(false);
        ResetCamera();
        ReleaseCamera();
        animationBackHandler.removeCallbacksAndMessages(null);
        back.setImageDrawable(backRetrace);
        backRetrace.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ObjectAddStory.this, Archive.class);
                intent.putExtra("objectName", objectName);
                intent.putExtra("ObjectStoryRecord", objectRecordMap);
                intent.putExtra("Authenticated", authenticated);
                ObjectAddStory.this.startActivity(intent);
                ObjectAddStory.this.overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
//                overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        }, 1000);

    }

    //When Hamburger button is clicked launch AboutAndLogout activity
    public void Hamburger(View view){

        commentaryInstruction.stopPlaying();
        Intent intent = new Intent(ObjectAddStory.this, AboutAndLogout.class);
        intent.putExtra("PreviousActivity", "ArchiveMainMenu");
        intent.putExtra("Authenticated", authenticated);
        ObjectAddStory.this.startActivity(intent);
        overridePendingTransition(R.anim.left_to_right_slide_in_activity, R.anim.left_to_right_slide_out_activity);
    }

    //When the back button is pressed, forward to onBackPressed()
    public void Back(View view) {

        onBackPressed();
    }

    //When the back button is pressed return to the ExploreArchiveItem activity of current story folder
    @Override
    public void onBackPressed() {

        commentaryInstruction.stopPlaying();
        back.setClickable(false);
        ResetCamera();
        animationBackHandler.removeCallbacksAndMessages(null);
        back.setImageDrawable(backRetrace);
        backRetrace.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ReleaseCamera();
                Intent intent = new Intent(ObjectAddStory.this, ExploreArchiveItem.class);
                intent.putExtra("objectName", objectName);
                intent.putExtra("ObjectStoryRecord", objectRecordMap);
                intent.putExtra("Authenticated", authenticated);
                ObjectAddStory.this.startActivity(intent);
                ObjectAddStory.this.overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
//                overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        }, 1000);

    }
}
