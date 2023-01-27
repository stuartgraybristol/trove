package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/*
The explore archive item class is used when an object's story folder has been selected in the archive. This class presents all the audio and video files relating to a
particular object. This is shown as a series of scrollable thumbnails. Upon clicking a thumbnail the file is opened. The user can also choose to select to add to the
number of stories relating to this object.
*/
/*
To do: There is currently no functionality to save specific stories to nfc tags. This is desirable.
 */
public class ExploreArchiveItem extends AppCompatActivity {

    //Story folder storage structures
    HashMap<String, ArrayList<ObjectStoryRecord>> objectRecordMap;
    LinkedHashMap<String, File> fileMap;
    LinkedHashMap<String, Bitmap> storyCoverMap;
    LinkedHashMap<String, String> storyTypeMap;
    ArrayList<ObjectStoryRecord> objectFiles;
    File story_directory;

    //Thumbnail display
    ProgressBar progressBar;
    ExploreImageItemAdapter cloudImageAdapter;
    HorizontalGridView gridview;
    int colourCounter;
    int currentColour;
    int[] colourCode;

    //Activity identifiers
    String objectName;
    Activity activity;
    Context context;

    //Image views and Animations
    ImageView back;
    AnimatedVectorDrawable backRetrace, backBegin;
    Handler animationBackHandler;

    //Firebase components
    boolean authenticated;

    //Commentary
    CommentaryInstruction commentaryInstruction;

    //onCreate is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_archive_item_kids_ui);
        gridview = (HorizontalGridView) findViewById(R.id.gridView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        activity = this;
        context = this;
        //Check for data connection before allowing user to sign in via cloud account
        checkConnection();
        //Initialize the storage structures used to hold files and folders
        initializeStorageStructures();
        //Initialize commentary instructions
        initializeCommentary();
        //Initialize animations
        initializeAnimations();
        //Load the story files for the currently selected story
        loadFiles();
    }

    //Initialize the commentary instructions.
    void initializeCommentary() {

        commentaryInstruction = new CommentaryInstruction(this, this, false, authenticated);
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.exploreobject), false, RecordStory.class, "RecordStory");
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

    //Initialize the storage structures used to hold the files and folders loaded from local or cloud storage.
    void initializeStorageStructures() {

        storyCoverMap = new LinkedHashMap<String, Bitmap>();
        storyTypeMap = new LinkedHashMap<String, String>();
        objectRecordMap = (HashMap<String, ArrayList<ObjectStoryRecord>>) getIntent().getExtras().get("ObjectStoryRecord");
        fileMap = new LinkedHashMap<String, File>();
        objectName = (String) getIntent().getExtras().get("objectName");
    }

    //Animation setup to handle the back button
    void initializeAnimations() {

        //Initialize back button, begin, and retrace AnimatedVectorVariable animations
        back = (ImageView) findViewById(R.id.back);
        backBegin = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_close_1);
        backRetrace = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_close_2);

        //Handler to undertake the animation - One second after the activity starts animate in the back button; Two seconds after activity starts change the back button
        //colour to white.
        animationBackHandler = new Handler();
        animationBackHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                back.setVisibility(View.VISIBLE);
                Drawable d = back.getDrawable();
                final AnimatedVectorDrawable zigzaganim = (AnimatedVectorDrawable) d;
                zigzaganim.start();
            }
        }, 1000);

        animationBackHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                back.setVisibility(View.VISIBLE);
                Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.kids_ui_close, null);
                d = DrawableCompat.wrap(d);
                DrawableCompat.setTint(d, Color.WHITE);
                back.setImageDrawable(d);

            }
        }, 2000);
    }

    //Setup the directory used to temporarily store the files
    File setupStoryDirectory(String ObjectName) {

        String packageLocation = ("/Cloud");
        String newDirectory = packageLocation + "/" + ObjectName;
        story_directory = getExternalFilesDir(newDirectory);

        return story_directory;
    }

    //Decide what should be shown on a story file thumbnail, based on the file type. If the file is audio show an music icon, if picture show image preview.
    //To do: this method can be modified to include written or video content.
    void getStoryCover(String StoryName, String StoryType, File file) {

        //For each file, save the story name in the storyCoverMap which couples it with an image preview. Also, save the story name in the storyTypeMap which
        //couples it with a description of the file type.
        if(StoryType.equalsIgnoreCase("AudioFile")) {

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.audio_icon);
            storyCoverMap.put(StoryName, icon);
            storyTypeMap.put(StoryName, StoryType);
        }

        else if(StoryType.equalsIgnoreCase("PictureFile")) {

            storyCoverMap.put(StoryName, ShowPicture(file));
            storyTypeMap.put(StoryName, StoryType);
        }

        else if (StoryType.equalsIgnoreCase("WrittenFile")) {

        }

        else if(StoryType.equalsIgnoreCase("VideoFile")) {

        }
    }

    //Create the temporary file in internal memory, so that it can be replayed. It saves a temporary local copy rather than streaming content directly from the cloud.
    File TempFile(String StoryName, String StoryType, File story_directory) {

        File file = null;
        String type = "";
        String ext = "";

        //Save file using a different extension based on the type of file it is.
        if(StoryType.equalsIgnoreCase("AudioFile")) {

            type = "audio";
            ext = ".mp3";
        }

        else if(StoryType.equalsIgnoreCase("PictureFile")) {

            type = "images";
            ext = ".jpg";
        }

        else if (StoryType.equalsIgnoreCase("WrittenFile")) {

            type = "text";
            ext = ".txt";
        }

        else if(StoryType.equalsIgnoreCase("VideoFile")) {

            type = "video";
            ext = ".mp4";
        }

        //Create the file
        try {

            file = File.createTempFile(StoryName, ext, story_directory);

            Uri story_directory_uri = FileProvider.getUriForFile(context,
                    "com.example.android.fileprovider",
                    file.getAbsoluteFile());
        }

        //Add exception warning message
        catch (IOException e) {

        }

        return file;
    }

    /*
    We download all the story files for a particular object from the FireStore URL links passed from the Archive activity
     */
    void DownloadFromCloud(String StoryName, String URLLink, String StoryType, File story_directory) {

        final String theStoryType = StoryType;
        final String theStoryName = StoryName;
        StorageReference gsReference;
        FirebaseStorage storage;
        storage = FirebaseStorage.getInstance();
        gsReference = storage.getReferenceFromUrl(URLLink);

        final File file = TempFile(theStoryName, theStoryType, story_directory);

        //As files are successfully returned display them as thumbnails in the adapter view
            gsReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    //Couple the name of the story folder with the corresponding file in a fileMap
                    fileMap.put(theStoryName, file);
                    //Get the cover for each story file
                    getStoryCover(theStoryName, theStoryType, file);
                    //Define background thumbnail colours
                    thumbnailColours();
                    //As file thumbnails are completed, hide the progress bar
                    progressBar.setVisibility(View.INVISIBLE);
                    //Update the image adapter used to display the list of story files
                    cloudImageAdapter = new ExploreImageItemAdapter(activity, context, fileMap.size(), fileMap, colourCode, objectRecordMap, storyCoverMap, storyTypeMap, commentaryInstruction);
                    gridview.invalidate();
                    gridview.setAdapter(cloudImageAdapter);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
    }

    //Decide on how to load the files - from the cloud or local
    void loadFiles() {

        objectFiles = new ArrayList<ObjectStoryRecord>();
        objectFiles = objectRecordMap.get(objectName);
        File story_directory = setupStoryDirectory(objectName);

        //If connected to network, download the relevant story files from the cloud
        if(authenticated) {

            for (int i = 0; i < objectFiles.size(); i++) {

                DownloadFromCloud(objectFiles.get(i).getStoryName(), objectFiles.get(i).getStoryRef(), objectFiles.get(i).getStoryType(), story_directory);
            }
        }

        //If not network connected, launch a new Async Task to handle the loading of local files
        else {
            new LocalFiles().execute();
        }
    }

    //Load the story files from internal local storage
    void LoadLocalFiles(){

        //For all of the object file names listed in the objectFiles array, get the file and put it in a fileMap which couples each story file name with the file
        //itself. Next, get the relevant cover for each story and the appropriate thumbnail colour.
        for(int i=0; i<objectFiles.size(); i++) {

            String path = objectFiles.get(i).getStoryRef();
            File file = new File(path);
            fileMap.put(objectFiles.get(i).getStoryName(), file);
            getStoryCover(objectFiles.get(i).getStoryName(), objectFiles.get(i).getStoryType(), file);
            thumbnailColours();
        }
    }

    //The method is used to generate and variate thumbnail background shape colours for cloud stories. After 3 colours have been used, we return to the first colour.
    void thumbnailColours() {

        //Set colour counter to 0
        colourCounter = 0;
        //Initialize first colour
        currentColour = Color.parseColor("#756bc7");
        //Initialize array list of colours based on the length of the number of story folders (unique story identifiers) required.
        colourCode = new int[fileMap.size()];

        //Loop over the coverImageMap of every story cover image (the first picture taken in the creation of the story about an object)
        for (int i = 0; i < fileMap.size(); i++) {

            if(colourCounter==0) {

                currentColour = Color.parseColor("#756bc7");
                colourCounter++;
            }

            else if(colourCounter==1) {

                currentColour = Color.parseColor("#ffb491");
                colourCounter++;
            }

            else if (colourCounter>1) {

                currentColour = Color.parseColor("#54b8a9");
                colourCounter = 0;
            }

            colourCode[i] = currentColour;
        }
    }

    //To Do: Make Bitmap Rescaler (ShowPicture) its own class as it is reused throughout the app OR consider using dedicated Picture Loaded like Glide.
    Bitmap ShowPicture(File pictureFile) {

        Bitmap adjustedBitmap;

            // Get the dimensions of the View
            int targetW = 300;
            int targetH = 300;

// Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions); // you can get imagePath from file
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

// Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

// Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            adjustedBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);

        return adjustedBitmap;
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

    //Launch Hamburger activity
    public void Hamburger(View view){

        commentaryInstruction.stopPlaying();
        Intent intent = new Intent(ExploreArchiveItem.this, AboutAndLogout.class);
        intent.putExtra("PreviousActivity", "ArchiveMainMenu");
        intent.putExtra("Authenticated", authenticated);
        ExploreArchiveItem.this.startActivity(intent);
        overridePendingTransition(R.anim.left_to_right_slide_in_activity, R.anim.left_to_right_slide_out_activity);
    }

    //Forward all back button presses to onBackPressed()
    public void Back(View view) {

        onBackPressed();
    }

    //Allow user to add another audio or picture file to the current object story folder
    public void AddStory(View view) {

        commentaryInstruction.stopPlaying();
        Intent intent = new Intent(ExploreArchiveItem.this, ObjectAddStory.class);
        intent.putExtra("PreviousActivity", "ExploreArchiveItem");
        intent.putExtra("ObjectStoryRecord", objectRecordMap);
        intent.putExtra("objectName", objectName);
        intent.putExtra("Authenticated", authenticated);
        ExploreArchiveItem.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //Home button functionality - not currently in use
    public void Home(View view) {

        commentaryInstruction.stopPlaying();
        Intent intent = new Intent(ExploreArchiveItem.this, HomeScreen.class);
        intent.putExtra("PreviousActivity", "ExploreArchiveItem");
        intent.putExtra("Authenticated", authenticated);
        ExploreArchiveItem.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //Activity Governance
    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
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

    //When the back button is pressed: stop any commentary instructions, disable the clickability of the back button, and begin animation transition
    @Override
    public void onBackPressed() {

        commentaryInstruction.stopPlaying();
        back.setClickable(false);
        animationBackHandler.removeCallbacksAndMessages(null);
        back.setImageDrawable(backRetrace);
        backRetrace.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ExploreArchiveItem.this, Archive.class);
                intent.putExtra("PreviousActivity", "RecordStory");
                intent.putExtra("Authenticated", authenticated);
                ExploreArchiveItem.this.startActivity(intent);
                overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        }, 1000);

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

    //Async task which allows the loading of local files on a separate thread - note, FireBase can do this on its own thread
    class LocalFiles extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            LoadLocalFiles();

            return null;
        }

        protected void onPostExecute(Void result) {

            progressBar.setVisibility(View.INVISIBLE);
            cloudImageAdapter = new ExploreImageItemAdapter(activity, context, fileMap.size(), fileMap, colourCode, objectRecordMap, storyCoverMap, storyTypeMap, commentaryInstruction);
            gridview.invalidate();
            gridview.setAdapter(cloudImageAdapter);
        }
    }
}
