package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/*
The RecordStory activity is used for the recording of an audio story about a new object. In the activity, the user is prompted to either record an audio story
or to visit the story archive. If they choose to record an audio story, they must press and hold the audio record button as they speak their story. Afterwards
they can either choose to rerecord their story or take a picture of the object which relates to the story. If the user seeks to record a story about a previous
object, they can do so by visiting the story archive. Presently audio stories created require a picture of the associated object because in the archive menu,
stories are grouped by object image.
 */
public class RecordStory extends AppCompatActivity {

    //The View group and ImageViews displayed on the activity layout
    ImageView recordButton, cameraButton, archive, back;

    //The animations used on the ImageViews
    AnimatedVectorDrawable recordButtonAnim, backRetrace;
    Drawable recordButtonNonAnim;
    Animation slideout, slidein, fadein, fadeout;

    //Handlers, runnables, and logical components
    Handler animationHandler, animationBackHandler;
    Runnable RecordButtonRunnable;
    Handler idleSaveStoryToArchiveHandler;

    //Firebase - networked activities
    boolean authenticated = false;

    //NFC components and variables
    String tag_data = null;
    NFCInteraction nfcInteraction;
    Tag mytag;
    boolean newStoryReady = false;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    //Recording Controller
    boolean permissionToRecordAccepted = false;
    boolean recordingStatus = false;
    boolean recordButtonRunning = false;
    AudioRecorder audioRecorder;

    //Camera Variables
    private Camera mCamera;
    private CameraPreview mPreview;
    CameraRecorder cameraRecorder;

    //Camera Views
    ImageButton captureButton;
    FrameLayout preview;
    LinearLayout camera_linear;

    //Request Code Variables
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    //File Save Variables
    private static String audioFileName = null;
    File image, story_directory, tag_directory, cover_directory, cloud_directory;

    //trove voice
    CommentaryInstruction commentaryInstruction;


    //Grant permission to record audio and to take pictures (required for some newer Android devices)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Depending on whether the request is audio or picture, attempt to grant request
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

        //If permission to record is denied, close request.
        if (!permissionToRecordAccepted) {

            finish();
        }
    }

    //onCreate is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Layout associated with this activity
        setContentView(R.layout.activity_record_story_kids_ui);
        //Ensure screen always stays on and never dims
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        //Setup new commentary instructor
        commentaryInstruction = new CommentaryInstruction(this, this, false, authenticated);
        //Prompt user to press and hold the record button to record a new story.
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.recordstory1), false, RecordStory.class, "RecordStory");
        //Note - currently disabled. If a user records a story but does not save it to a tag within a certain time period, the story is automatically saved to archive.
        idleSaveStoryToArchiveHandler = new Handler();
    }


    //Initialize all views in activity
    void initializeViews() {

        //Initialize the button image views layouts.
        recordButton = (ImageView) findViewById(R.id.record);
        cameraButton = (ImageView) findViewById(R.id.camera);
        archive = (ImageView) findViewById(R.id.archive);
        back = (ImageView) findViewById(R.id.back);
        captureButton = (ImageButton) findViewById(R.id.button_capture);
    }

    void initializeCamera() {

        //Initialize camera components.
        //Camera_linear is the layout inflated to house the camera content.
        camera_linear = (LinearLayout) findViewById(R.id.camera_linear);
        //The preview frame is overlayed on the layout which shows the camera view.
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        //Get an instance of the phone's camera.
        mCamera = cameraRecorder.getCameraInstance();
        //Find content fort the camera preview frame using the instance of the camera initialized.
        mPreview = new CameraPreview(getApplicationContext(), mCamera);
        //Apply the camera previes content from the camera to the preview frame itself.
        preview.addView(mPreview);
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

    //Animation and Layout Setup
    void initializeAnimations() {

        //Initialize animations
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        recordButtonAnim = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_record_anim_alt);
        recordButtonNonAnim = (Drawable) getDrawable(R.drawable.kids_ui_record_circle);
        backRetrace = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_back_anim_retrace);
        slideout = AnimationUtils.loadAnimation(this, R.anim.slideout);
        slidein = AnimationUtils.loadAnimation(this, R.anim.slidein);

        //Timed handler which times the zig zag animations from the back button.
        animationBackHandler = new Handler();
        //Draw the back button 2 seconds after the activity has loaded up.
        animationBackHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                back.setVisibility(View.VISIBLE);
                Drawable d = back.getDrawable();
                final AnimatedVectorDrawable zigzaganim = (AnimatedVectorDrawable) d;
                zigzaganim.start();
            }
        }, 2000);

        //Undraw the back button
        animationBackHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.kids_ui_back, null);
                d = DrawableCompat.wrap(d);
                DrawableCompat.setTint(d, Color.WHITE);
                back.setImageDrawable(d);
            }
        }, 3000);
    }

    //When called slide view out of sight.
    void slideOutViewAnimation(View view) {

        //Get the current visibility status of the view.
        int visibility = view.getVisibility();

        //If the view is currently visible, execute the animation.
        if(visibility==View.VISIBLE){

            view.startAnimation(slideout);
            view.setVisibility(View.INVISIBLE);
        }
    }

    //When called slide view into initial position.
    void slideInViewAnimation(View view) {

        //Get the current visibility status of the view.
        int visibility = view.getVisibility();

        //If the view is currently invisible, execute the animation.
        if(visibility==View.INVISIBLE){

            view.startAnimation(slidein);
            view.setVisibility(View.VISIBLE);
        }
    }

    //Method for controlling the audio record button.
    void recordButtonController() {

//        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.holdrecordbutton), true, HomeScreen.class);

        //Record button onTouchListener - when the button is touched take appropriate action.
        //Currently the record button will execute the MotionEvent.ACTION_DOWN code as long as the user touches and holds the record button. When the user
        //stops holding the button, MotionEvent.ACTION_UP is triggered.
        recordButton.setOnTouchListener((new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //Stop playing any commentary instructions
                        commentaryInstruction.stopPlaying();
                        //Set the control boolean recordButtonRunning to true - this governs the record button animation
                        recordButtonRunning = true;
                        //Set the control boolean recordingStatus to true - this governs the record button logic
                        recordingStatus = true;
                        //Handle recording actions
                        audioRecordingManager(v);
                        //Handle record button animations
                        recordButtonAnimationController();
                        break;
                    case MotionEvent.ACTION_UP:
                        //Set the control boolean recordButtonRunning to true - this governs the record button animation
                        recordButtonRunning = false;
                        //Set the control boolean recordingStatus to true - this governs the record button logic
                        recordingStatus = false;
                        //Handle recording actions
                        audioRecordingManager(v);
                        //Handle record button animations
                        recordButtonAnimationController();
                        //Prompt user that their audio recording has finished and that they should now take a picture of their object
                        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.takeapicture), false, RecordStory.class, "RecordStory");
                        break;
                }

                return true;
            }
        }));
    }

    //Handle the painting of any background image views.
    void paintViews() {

        //Paint the back button in white
        int paintColour = android.graphics.Color.rgb(253, 195, 204);
        Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.kids_ui_back_anim, null);
        d = DrawableCompat.wrap(d);
        DrawableCompat.setTint(d, paintColour);
        back.setImageDrawable(d);
    }

    //When a new intent is generated by an NFC scanning operation, read its content and take a course of action.
    @Override
    protected void onNewIntent(Intent intent){

        //If the new intent matches the filtered NFC intent, read in the tag's raw data.
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //Display a visible notification showing the object has been found.
            Toast.makeText(this, "Object Found.", Toast.LENGTH_LONG).show();

                //If the new story is ready to be written
                if (newStoryReady) {

                    //Initialize success boolean which returns true only if the nfc interaction has been successful
                    boolean success = false;
                    success = nfcInteraction.doWrite(mytag, tag_data);

                    //If the nfc write process has succeeded, take the following action
                    if (success) {

                        //Return the value of newStoryReady to false as current ready story has been written
                        newStoryReady = false;
                        //Stop the clickability of the current range of image views
                        disableViewClickability();
                        //Stop any active commentary
                        commentaryInstruction.stopPlaying();
                        //Cancel idleSaveStoryToArchiveHandler which automatically saves the current story to the archive
                        cancelIdleStoryCountdown();
                        //Reset the camera to a new preview
                        resetCamera();
                        //Release current camera instance
                        releaseCamera();
//                        nfcInteraction.Complete(success);
                        //Complete the process by navigating to back to HomeScreen activity which will play the new story
                        Complete(success);
                    } else {

                        newStoryReady = true;
                    }
            }
        }
    }

    //Method is called when story recording process is completed. Returns user to HomeScreen to review story.
    void Complete(boolean complete) {

        if(complete) {

            Toast.makeText(this, "Story saved to object.", Toast.LENGTH_LONG ).show();
            Intent intent = new Intent(this, HomeScreen.class);
            intent.putExtra("PreviousActivity", "RecordStory");
            intent.putExtra("Authenticated", authenticated);
            intent.putExtra("NewStory", true);
            intent.putExtra("storyRef", tag_data);
            this.startActivity(intent);
        }
    }

    //Setup new storage folder
    private void SetupStoryLocation() {

        //Delete previous project directories
        deleteDirectories();
        //ensure that newStoryReady is set to false
        newStoryReady = false;
        //Give story name a unique identifier
        String name = UUID.randomUUID().toString();
        //Set the value of the tag data to be the unique identifier of the story
        tag_data = name;
        //Create new story folder based on the name of the new story - this will be home to the new stories as well as any additional stories recorded
        story_directory = new File (getFilesDir() + File.separator + "Stories" + File.separator + name);
        //Create new tag folder based on the name of the new story - this will be home to the stories recorded which are saved to the tags.
        //Currently only a single audio file and picture are associated with one story folder.
        tag_directory = new File (getFilesDir() + File.separator + "Tag" + File.separator + name);
        //Create new cover image folder based on the name of the story - this stores the image representing the object.
        cover_directory = new File (getFilesDir() + File.separator + "Covers" + File.separator + name);
        //A temporary storage directory for files which are being sent and downloaded from the cloud.
        cloud_directory = new File (getFilesDir() + File.separator + "Cloud" + File.separator + name);
        //Make the described directory folders
        story_directory.mkdir();
        tag_directory.mkdir();
        cover_directory.mkdir();
        cloud_directory.mkdir();

        /* Example of saving stories to external directories

//        String newDirectory = LocalStoryFolder + "/" + name;
//        String newDirectory2 = TagFolder + "/" + name;
//        String newDirectory3 = CoverFolder + "/" + name;
//        story_directory = getExternalFilesDir(newDirectory);
//        tag_directory = getExternalFilesDir(newDirectory2);
//        cover_directory = getExternalFilesDir(newDirectory3);

        */
    }

    //Allow users to restart their audio story
    void StoryReset() {

        //Delete Any Previous Recordings

        //Remove Previous Audio Commentary Callbacks
        cancelIdleStoryCountdown();
    }

    //Recording Audio Management
    void recordAudio(View view) {

        //Request permission to record audio (required for some newer Android devices)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        try {
            //Setup a new audio recorder object, used to handle the recording control and storage
            audioRecorder = new AudioRecorder(this, story_directory, tag_directory);
            audioRecorder.startRecording();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        //Set audioFileName in this class to equal the unique value generated in the audioRecorder object
        audioFileName = audioRecorder.getAudioFileName();
    }


    //Control the record button animations
    void recordButtonAnimationController() {

        //Initialize a new handler to handle animation control
        animationHandler = new Handler();

        //Runnable to handle idle trove animation
        RecordButtonRunnable = new Runnable() {

            //Run record button animation
            @Override
            public void run() {
                recordButtonAnim.start();

                //If record button active variable is true, repeat animation once per second - orange animation
                if(recordButtonRunning) {
                    animationHandler.postDelayed(this, 1000);
                }

                //If the record button active variable is false, cancel the animation handler call back and set image to be the default green record button
                else {
                    animationHandler.removeCallbacks(RecordButtonRunnable);
                    recordButton.setImageDrawable(recordButtonNonAnim);
                }
            }
        };

        //Begin runnable
        animationHandler.post(RecordButtonRunnable);
    }

    //Manage the audio recording
    void audioRecordingManager(View view) {

        try {

            //If recording status is to true, reset the current recording environment and then proceed to record new audio
            if (recordingStatus) {

                //Reset previous audio story
                StoryReset();
                //Setup new story location
                SetupStoryLocation();
                //Prepare the correct image views on the screen
                slideOutViewAnimation(archive);
                //Begin to animate the record button
                recordButton.setImageDrawable(recordButtonAnim);
                //Start recording audio
                recordAudio(view);
            }

            else {

                //Show the camera button - the next stage of the recording
                slideInViewAnimation(cameraButton);
                //Stop recording audio
                audioRecorder.stopRecording();
                //Stop animating the record button
                animationHandler.removeCallbacks(RecordButtonRunnable);
                recordButton.setImageDrawable(recordButtonNonAnim);
            }

            //The value of recording status is now opposite in anticipation of the second button event - i.e. start/stop, hold/lift
            recordingStatus = !recordingStatus;
        }

        catch (RuntimeException r) {

        }
    }


    //Enter the story archive
    public void Archive(View view) {

        commentaryInstruction.stopPlaying();
        disableViewClickability();
        archive.setClickable(false);
        releaseCamera();
        Intent intent = new Intent(RecordStory.this, Archive.class);
        intent.putExtra("Authenticated", authenticated);
        RecordStory.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //This method is starts a count down delay - when the delay is done, it saves a completed story to the archive
    //Note: This method is currently disabled
    void idleStoryCountdown() {

        idleSaveStoryToArchiveHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

//                Log.i("Annoying Handler", "Ach");
//                commentaryInstruction.setInputHandler(idleSaveStoryToArchiveHandler);
//                commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.recorddone1), true, ArchiveMainMenu.class, "RecordStory");
            }
        }, 120000);
    }

    //Cancels the countdown for autosaving a story
    void cancelIdleStoryCountdown() {

        idleSaveStoryToArchiveHandler.removeCallbacksAndMessages(null);
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

    //Camera Management
    public void Camera(View view) {

        //Check for permission to use camera
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
            }

            //Try to create a new instance of camera recorder object which handles the recording of photos
        try {
            cameraRecorder = new CameraRecorder(this, this, story_directory, tag_directory, cover_directory, mCamera, mPreview);

            //Wait half a second before it is safe to take a picture - this is a patch for a bug which crashed the app when the user pressed the camera button
            //before all camera components were ready.
            final Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {

                    cameraRecorder.setSafeToTakePicture(true);
                }
            }, 600);


            //Wait half a second until camera selection button has finished animating, then display camera preview etc.
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Launch camera preview frame
                    preview.startAnimation(fadein);
                    //Launch camera layout
                    camera_linear.startAnimation(fadein);
                    //Launch capture button
                    captureButton.startAnimation(fadein);
                    //Make these components visible
                    preview.setVisibility(View.VISIBLE);
                    captureButton.setVisibility(View.VISIBLE);
                    camera_linear.setVisibility(View.VISIBLE);
                    //Create onClickListener for the capture button
                    captureButton.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // get an image from the camera
                                    if(cameraRecorder.getSafeToTakePicture()) {
                                        Log.i("Taking Picture: ", "Safe");
                                        captureButton.setImageResource(R.drawable.kids_ui_record_anim_alt_mini);
                                        mCamera.takePicture(null, null, mPicture);
                                        cameraRecorder.setSafeToTakePicture(false);
                                    }

                                    else {
                                        Log.i("Taking Picture: ", "Unsafe");
                                    }
                                }
                            }
                    );

                }
            }, 500);
        }

        catch (NullPointerException e) {


                Log.i("Error", "Eh nah");
            }
    }

    //When the camera takes a picture, this callback method is called and executed.
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        //After a picture has been taken, prepare the activity to save the audio and picture references to an NFC tag.
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Retrieve taken photo contents
            File pictureFile = cameraRecorder.getOutputMediaFile(MEDIA_TYPE_IMAGE);

            //If there is no file (taking the picture failed), take no action and display a warning message
            if (pictureFile == null) {
                Log.d("Tag", "Error creating media file, check storage permissions");
                return;
            }

            //If a file does exist, begin the activity preparations for saving to NFC
            else {

                //Fade out camera components
                camera_linear.startAnimation(fadeout);
                captureButton.startAnimation(fadeout);
                captureButton.setVisibility(View.GONE);
                preview.setVisibility(View.GONE);
                camera_linear.setVisibility(View.GONE);

                //Write photo data to file
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    //Save copies of the picture in Tag and Cover directories under the current story UUID
                    cameraRecorder.copyFile(pictureFile, cameraRecorder.getTagFile());
                    cameraRecorder.copyFile(pictureFile, cameraRecorder.getCoverFile());
                } catch (FileNotFoundException e) {
                    Log.d("Tag", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("Tag", "Error accessing file: " + e.getMessage());
                }

                //Reset camera for further use
                resetCamera();
                //Mark new story as ready to be saved with audio and picture complete
                newStoryReady = true;
                //Begin the automatic timer for saving to the archive
                idleStoryCountdown();
                slideOutViewAnimation(cameraButton);
                slideInViewAnimation(archive);
                //Run a commentary instruction asking user to scan to a tag
                commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.attachnfc), false, null, "RecordStory");

                //If network connected, attempt to save story to the cloud
                if (authenticated) {
                    //Generate a new unique identifier
                    UUID objectUUID = UUID.randomUUID();
                    //Create new SaveToCloud object, which handles saving the object to the cloud.
                    SaveToCloud saveToCloud = new SaveToCloud(story_directory, objectUUID);
                    saveToCloud.CloudSaveNewObject();

                    }
                }
            }
        };

        //Refresh camera preview
        void resetCamera() {

            if (mCamera != null) {
                mCamera.stopPreview();
                captureButton.setImageResource(R.drawable.kids_ui_record_circle_mini);
                mCamera.startPreview();
            }
        }

        //Stop current camera preview and release current camera instance
        void releaseCamera() {

            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        }

        //Delete the file storage directories for the current story.
        void deleteDirectories() {

            if (story_directory != null && !newStoryReady) {
                deleteStoryDirectory();
            }

            if (tag_directory != null && !newStoryReady) {
                deleteTagDirectory();
            }

            if (cover_directory != null && !newStoryReady) {
                deleteCoverDirectory();
            }
        }

        //Delete the current story from the story directory
        void deleteStoryDirectory() {

            try {

                Log.i("Deleting StoryDirectory", story_directory.toString());
                FileUtils.deleteDirectory(story_directory);
            } catch (IOException e) {

            }

        }

    //Delete the current story from the tag directory
    void deleteTagDirectory() {

            try {

                Log.i("Deleting Tag Directory", tag_directory.toString());
                FileUtils.deleteDirectory(tag_directory);
            } catch (IOException e) {

            }

        }

    //Delete the current story from the cover directory
    void deleteCoverDirectory() {

            try {

                Log.i("Deleting Covr Directory", cover_directory.toString());
                FileUtils.deleteDirectory(cover_directory);
            } catch (IOException e) {

            }

        }

    //Activity Governance
    @Override
    public void onPause(){
        super.onPause();
        nfcInteraction.WriteModeOff(adapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        nfcInteraction.WriteModeOn(adapter, pendingIntent, writeTagFilters);
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
    }

    // Save the current state of these bundled variables.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean("Authenticated", authenticated);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    //Disable all view clickability
    void disableViewClickability() {

        back.setClickable(false);
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.logged_in_write_home_layout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setClickable(false);
        }
    }

    //When the hamburger menu is clicked, run this method which launches AboutAndLogout activity
    public void Hamburger(View view){

        //Ensure no other views can interrupt process by removing their clickability.
        disableViewClickability();
        //Delete any unsaved stories
        deleteDirectories();
        //Reset and release the camera
        resetCamera();
        releaseCamera();
        //Cancel any automatic story archive callbacks or animations
        cancelIdleStoryCountdown();
        animationBackHandler.removeCallbacksAndMessages(null);
        //Stop any commentary instructions
        commentaryInstruction.stopPlaying();
        //Open Hamburger activity
        Intent intent = new Intent(RecordStory.this, AboutAndLogout.class);
        intent.putExtra("PreviousActivity", "RecordStory");
        intent.putExtra("Authenticated", authenticated);
        RecordStory.this.startActivity(intent);
        overridePendingTransition(R.anim.left_to_right_slide_in_activity, R.anim.left_to_right_slide_out_activity);
    }


    //Redirect any back button presses to onBackPressed() method
    public void Back(View view) {

        onBackPressed();
    }

    //When the back button is pressed undertake this code
    @Override
    public void onBackPressed() {

        commentaryInstruction.stopPlaying();
        disableViewClickability();
        deleteDirectories();

        //Reset the camera
        resetCamera();
        //Cancel the auto story save handler
        cancelIdleStoryCountdown();
        //Cancel any animations
        animationBackHandler.removeCallbacksAndMessages(null);
        //Start the back button's animation - retrace
        back.setImageDrawable(backRetrace);
        backRetrace.start();

        //Create handler for changing activity
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                releaseCamera();
                Intent intent = new Intent(RecordStory.this, HomeScreen.class);
                intent.putExtra("PreviousActivity", "RecordStory");
                intent.putExtra("Authenticated", authenticated);
                intent.putExtra("NewStory", false);
                RecordStory.this.startActivity(intent);
//                overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        }, 1000);

    }
}


//
//
//    void AttachToNFCInstruction() {
//
//        Uri audioFileUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.attachtag_app);
//        onPlay(audioFileUri);
//    }
//
//    /*When audio playback buttons are selected for first time, setup new audio media player. When
//    user interacts with playback buttons after audio media player has already been setup, toggle
//    between media player pause and play*/
//    public void onPlay(Uri audioFileUri) {
//
//        setupAudioMediaPlayer(audioFileUri);
//        if (!playbackStatus) {
//            startPlaying();
//            playbackStatus = true;
//        }
//    }
//
//    //Setup new audio media player drawing from audio file location
//    protected void setupAudioMediaPlayer(Uri audioFileUri) {
//        Log.i("audio file", audioFileName);
//
//        try {
//            mPlayer.setDataSource(this, audioFileUri);
//            mPlayer.prepare();
//            mPlayerSetup = true;
//        } catch (IOException e) {
//            Log.e("Error", "prepare() failed");
//        }
//    }
//
//    //Start audio media player and start listening for stop imageView to be pressed
//    public void startPlaying() {
//        mPlayer.start();
//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            public void onCompletion(MediaPlayer mp) {
//
//                mPlayer.stop();
//                mPlayer.reset();
//                playbackStatus = false;
//            }
//        });
//    }
//
//

/*
            new Thread(new Runnable() {
                public void run() {
                    while (recordButton != null) {
                        try {
                            recordButton.post(new Runnable() {
                                @Override
                                public void run() {
                                    recordButtonAnim.start();
                                }
                            });
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();











    public void CompletePictureRecording(View view) {

        pictureFileName = photoPath;
//        recordedMediaHashMap.put("Picture", pictureFileName);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //Picture Processing
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

                new RecordStory.ProcessPicture().execute();
                slideOutViewAnimation(cameraButton);
                slideInViewAnimation(archive);
                commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.attachtag_app), false, null);
                UUID objectUUID = UUID.randomUUID();

                if(authenticated) {
                    SaveToCloud saveToCloud = new SaveToCloud(story_directory, objectUUID);
                    saveToCloud.CloudSaveNewObject();
                }
            }
        }

        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {

//                new RecordStory.ProcessVideo().execute();

            }
        }
    }


   /*
            new Thread(new Runnable() {
                public void run() {
                    while (!cameraRecorder.getPictureSave()) {
                        try {
                            Thread.sleep(200);
                            Log.i("Woo", "Checking");
                        } catch (InterruptedException ignored) {
                        }
                    }

                    Thread.interrupted();
                    new RecordStory.ProcessPicture().execute();
                    slideOutViewAnimation(cameraButton);
                    slideInViewAnimation(archive);
                    commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.attachtag_app), false, null);
                    UUID objectUUID = UUID.randomUUID();

                    if(authenticated) {
                        SaveToCloud saveToCloud = new SaveToCloud(story_directory, objectUUID);
                        saveToCloud.CloudSaveNewObject();
                    }
                    //do something
                }
            }).start();


class ProcessPicture extends AsyncTask<View, Void, Void> {

        Bitmap processedBitmap;

        @Override
        protected Void doInBackground(View... params) {

//            imageView = params[0];

            try {

//                cameraRecorder.PictureProcessing();
//                photoPath = cameraRecorder.getPhotoPath();
//                photoUri = cameraRecorder.getPhotoURI();
////                picture_story_fragment.setPictureBoxDimensions(pictureRecorder.getRotationInDegrees());
//                processedBitmap = cameraRecorder.getAdjustedBitmap();

            } catch (NullPointerException e) {

            } catch (IllegalArgumentException e) {

            }

            return null;

        }

        protected void onPostExecute(Void result) {

            newStoryReady = true;
            idleStoryCountdown();
        }
    }


            */
