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
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
The Archive class groups stories together based on the object which they relate to. A series of image thumbnails are presented for the user to scroll through and
select by touching on an image. If the user is offline, the images returned are uploaded from local internal memory folders but if they are online, the images are
returned from Google Firebase storage.

To do: This class could make use of dedicated objects used to load local or cloud stories.
 */
public class Archive extends AppCompatActivity {

    //Story folder storage structures
    LinkedHashMap<String, ArrayList<File>> folderFiles;
    HashMap<File, File> folderImages;
    HashMap<File, Bitmap> imageFiles;
    ArrayList<File> validStoryFolders, validCoverFolders;
    LinkedHashMap<String, ArrayList<ObjectStoryRecord>> objectRecordMap;
    ArrayList<File> coverImages;
    HashMap<String, Bitmap> coverImageMap;

    //Thumbnail display
    ArchiveImageAdapter cloudImageAdapter;
    HorizontalGridView gridview;
    int colourCounter;
    int currentColour;
    int[] colourCodeArray;
    int numberOfThumbs;
    ProgressBar progressBar;

    //Activity identifiers
    Context context;
    Activity activity;

    //Image views
    ImageView back;
    AnimatedVectorDrawable backRetrace, backBegin;

    //Animation timings
    Handler animationBackHandler;

    //Firebase components
    private StorageReference mStorageRef;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    boolean authenticated = false;

    //Commentary
    CommentaryInstruction commentaryInstruction;

    //onCreate is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_kids_ui);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gridview = (HorizontalGridView) findViewById(R.id.gridView);
        context = this;
        activity = this;
        //Check for data connection before allowing user to sign in via cloud account
        checkConnection();
        //Initialize the storage structures used to hold files and folders
        initializeStorageStructures();
        //Initialize commentary instructions
        initializeCommentary();
        //Initialize animations
        initializeAnimations();
        //Choose which avenue to load data from - local or cloud
        loadFiles();
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

    //Initialize the commentary instructions.
    void initializeCommentary() {

        commentaryInstruction = new CommentaryInstruction(this, this, false, authenticated);
        commentaryInstruction.onPlay(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.archive), false, RecordStory.class, "RecordStory");
    }

    //Initialize the storage structures used to hold the files and folders loaded from local or cloud storage.
    void initializeStorageStructures() {

        //Initialize the progress bar which is displayed until the data is loaded and displayed in the data structures.
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);
        //FolderFiles lists the unique story identifier and relates it to the collection of files associated with the story, holding them in an arraylist - such as
        //multiple audio files or pictures for each story. E.g. StoryID: 1A , Files: Audio1.mp4, Picture1.jpg, Audio2.mp4, Audio3.mp4
        folderFiles = new LinkedHashMap<String, ArrayList<File>>();
        //FolderImages couples the story folder (named a unique story identifier) with an image file for that story, used to identify the story.
        folderImages = new HashMap<File, File>();
        //Every image file loaded cannot be displayed in jpg format in the ImageView and must be converted to a Bitmap for display. This HashMap
        //associates each image file with a Bitmap version.
        imageFiles = new HashMap<File, Bitmap>();
        //Associates the String name of each story folder (unique story identifier) with it's associated bitmap
        coverImageMap = new HashMap<String, Bitmap>();
    }

    //Animation Setup
    void initializeAnimations() {

        //Back button initialization
        back = (ImageView) findViewById(R.id.back);
        //BackBegin is the animated vector drawable used to initially show the image
        backBegin = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_back_anim);
        //BackBegin is the animated vector drawable used to hide the image
        backRetrace = (AnimatedVectorDrawable) getDrawable(R.drawable.kids_ui_back_anim_retrace);

        //Handler which starts the show back button animation after 2 seconds and after 3 seconds paints the animation in white
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

                Drawable d = VectorDrawableCompat.create(getResources(), R.drawable.kids_ui_back, null);
                d = DrawableCompat.wrap(d);
                DrawableCompat.setTint(d, Color.WHITE);
                back.setImageDrawable(d);

            }
        }, 3000);
    }

    //The method is used to generate and variate thumbnail background shape colours for local stories. After 3 colours have been used, we return to the first colour.
    void LocalThumbnailColours() {

        //Set colour counter to 0
        colourCounter = 0;
        //Initialize first colour
        currentColour = Color.parseColor("#756bc7");;
        //Initialize array list of colours based on the length of the number of story folders (unique story identifiers) required.
        colourCodeArray = new int[validStoryFolders.size()];

        for (int i = 0; i < validStoryFolders.size(); i++) {

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

            colourCodeArray[i] = currentColour;
        }
    }

    //The method is used to generate and variate thumbnail background shape colours for cloud stories. After 3 colours have been used, we return to the first colour.
    void thumbnailColours() {

        //Set colour counter to 0
        colourCounter = 0;
        //Initialize first colour
        currentColour = Color.parseColor("#756bc7");
        //Initialize array list of colours based on the length of the number of story folders (unique story identifiers) required.
        colourCodeArray = new int[coverImageMap.size()];

        //Loop over the coverImageMap of every story cover image (the first picture taken in the creation of the story about an object)
        for (int i = 0; i < coverImageMap.size(); i++) {

            //Paint first image thumbnail #756bc7
            if(colourCounter==0) {

                currentColour = Color.parseColor("#756bc7");
                colourCounter++;
            }

            //Paint first image thumbnail #ffb491
            else if(colourCounter==1) {

                currentColour = Color.parseColor("#ffb491");
                colourCounter++;
            }

            //Paint first image thumbnail #54b8a9
            else if (colourCounter>1) {

                currentColour = Color.parseColor("#54b8a9");
                colourCounter = 0;
            }

            //Save current colour
            colourCodeArray[i] = currentColour;
        }
    }

    //Check if user is currently connected to the network and load in stories accordingly - either from local storage or the cloud storage
    void loadFiles() {

        //If the user is connected to the network (authenticated = true), run cloud setup and then query FireStore Database for results
        if(authenticated) {

            CloudSetup();
            queryFireStoreDatabase();
        }

        //Else, run local setup and launch new AsyncTask to load local files
        else {

            LocalSetup();
            new LoadLocalImages().execute(authenticated);
        }
    }

    //Setup FireStore database connection
    void CloudSetup() {

        //Connect to FireBase, get a storage reference for user's cloud store bucket, then get user's auth token, then get the user's database schema
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    //Setup and populate local folder array lists - one array list holding the all the story cover folders, the other holding all the
    //story file folders. These array lists should match as the folder names share the same unique story identifier names (e.g. Story1, Story2, Story3 etc.)
    //However, if there is a problem in the storage of this data, it is possible that these lists could differ in length. Hence, later we match these lists
    //according to the number of shared folders.
    void LocalSetup() {

        validCoverFolders = new ArrayList<File>();
        validStoryFolders = new ArrayList<File>();
        File findStoryFolders = new File(getFilesDir() + File.separator + "Stories");
        File findCoverFolders = new File(getFilesDir() + File.separator + "Covers");
        File[] storyFolders  = findStoryFolders.listFiles();
        File[] coverFolders  = findCoverFolders.listFiles();

//        Log.d("LocalSetup", "storyFolders:" + storyFolders.toString());
//        Log.d("LocalSetup", "storyFolders:" + storyFolders.length);

        //Explore each story folder in turn. Traverse the files inside each folder. Ensure there is at least one picture file inside each story folder.
        //If there a valid jpg image, add the story folder to the ArrayList of validStoryFolders.
        for(int i = 0; i<storyFolders.length; i++) {


//            Log.d("LocalSetup", "storyFolders:" + storyFolders[i].getName());

            //Traverse through the list of subfiles for each story folder.
            File subFile = new File(getFilesDir() + File.separator + "Stories" + File.separator + storyFolders[i].getName());
            File[] subFiles = subFile.listFiles();

            //If there are subfiles in the current story folder, find files which match the jpg file extension.
            if (subFile.length() > 0) {

                for (int j = 0; j < subFiles.length; j++) {
                    Log.d("LocalSetup", "storyFolders subFiles:" + subFiles[j].getName());
                    String extension = FilenameUtils.getExtension(subFiles[j].toString());

                    if (extension.equalsIgnoreCase("jpg")) {

                        validStoryFolders.add(storyFolders[i]);
                    }
                }
            }
        }

        //Explore each cover image folder in turn. Ensure there is a picture file in each folder. If so, add the cover image folder to the ArrayList of validCoverFolders.
        //The validCoverFolders will later be compared to the validStoryFolders to ensure there is no mismatch between the number of items.
        for(int i = 0; i<coverFolders.length; i++) {

            Log.d("LocalSetup", "coverFolders:" + storyFolders[i].getName());

            coverFolders[i].getName();

            File subFile = new File(getFilesDir() + File.separator + "Covers" + File.separator + coverFolders[i].getName());
            File[] subFiles = subFile.listFiles();
            if(subFiles.length>0){

                validCoverFolders.add(coverFolders[i]);
            }
        }
    }

    /*Query the FireStore database for all the story items associated with the current user. Every query result should return a private download reference link to the corresponding file
     in the user's FireStore storage bucket e.g. picture file, audio file - which is then downloaded in the getCoverImageCloud method.*/
    void queryFireStoreDatabase() {

        //Create new objectRecord Map LinkedHashMap which is used to create an association between every story name and an array list of ObjectStoryRecords (an object describing different
        //properties of the file itself).
        objectRecordMap = new LinkedHashMap<String, ArrayList<ObjectStoryRecord>>();

        //Get the userID by using the current auth token to return the user's email address
        String userID = mAuth.getCurrentUser().getEmail();
        //Look for the ObjectStory collection in the database
        CollectionReference objectStoryRef = db.collection("ObjectStory");
        //Query the ObjectStory collection for all documents relating to the current user, and return the result in descending order of date
        Query query = objectStoryRef.whereEqualTo("Username", userID).orderBy("Date", Query.Direction.DESCENDING);
        //Listen for the query result
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            //Execute when result is returned
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //If the query was successfully communicated with a result returned, do the following.
                        if (task.isSuccessful()) {
                            //For each result document (each story file reference returned), store the meta-data about the document in a new ObjectStoryRecord
                            for (DocumentSnapshot document : task.getResult()) {

                                //Initialize meta-data variables about document
                                String StoryDate = ""; // Error with server data currently
                                String ObjectName = document.getData().get("objectName").toString();
                                String StoryName = document.getData().get("storyName").toString();
//                                String storyDate = document.getData().get("Date").toString();
                                String URLlink = document.getData().get("URL").toString(); //This is the download FireStore reference link to the file itself
                                String StoryType = document.getData().get("Type").toString();
                                String CoverImage = document.getData().get("Cover").toString();
                                String linkedText = "<b>Story </b>" + ObjectName + " = " +
                                String.format("<a href=\"%s\">Download Link</a> ", URLlink);

                                //Create new ObjectStory Record. For a returned query record this creates an object with all its defining attributes
                                ObjectStoryRecord objectStoryRecord = new ObjectStoryRecord(ObjectName, StoryName, StoryDate, URLlink, StoryType, CoverImage, "Cloud");

                                /*In the following segment of code we create a linked list of all the ObjectStoryRecord objects, called objectRecordMap.
                                objectRecordMap's keys are the UUID of each object in the database, the values are all the ObjectStoryRecords, relating to
                                those keys. If a database query record's object UUID already exists within the objectRecordMap, the query record's ObjectStoryRecord
                                object is added as a value for that key. If not, a new entry is made in objectRecordMap, with a new key (the UUID of the object) and
                                the value to the ArrayList of stories pertaining to that key - the value (the ObjectStoryRecord object)*/
                                if (objectRecordMap.containsKey(ObjectName)) {

                                    objectRecordMap.get(ObjectName).add(objectStoryRecord);
                                }

                                else {

                                    ArrayList<ObjectStoryRecord> objectStoryRecordObjectList = new ArrayList<ObjectStoryRecord>();
                                    objectStoryRecordObjectList.add(objectStoryRecord);
                                    objectRecordMap.put(ObjectName, objectStoryRecordObjectList);
                                }

                                //Download the cover image file from the FireStore bucket
                                if(CoverImage.equals("yes")) {

                                    getCoverImageCloud(ObjectName, URLlink);

                                }
                            }
                        }

                        //To do: Create error message for user if query fails.
                        else {

                            Log.i("Oops", "Oh No!");
                        }
                    }
                });
    }

    //This method handles the download of story cover image files from a user's storage bucket.
    void getCoverImageCloud(String ObjectName, String URLlink) {

        //FireBase storage reference components
        StorageReference gsReference;
        FirebaseStorage storage;
        String userID;
        File story_directory;
        storage = FirebaseStorage.getInstance();
        //Use authorisation token to get the current user id
        userID = mAuth.getCurrentUser().getUid();
        gsReference = storage.getReferenceFromUrl(URLlink);
        //Create local directory to download files into if it does not already exist, and declare a new Bitmap - used to scale the downloaded image
        story_directory = new File(getFilesDir() + File.separator + "Cloud");
        final Bitmap adjustedBitmap;

        try {

            //Create new picture file in the local cloud storage directory
                final File pictureFile = File.createTempFile("images", ".jpg", story_directory);
                final String URL = URLlink;
                final String theObjectName = ObjectName;

                //Open the file reference and set up an onSuccessListener
                gsReference.getFile(pictureFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    //If file successfully found, download it
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        //Local temp file has been created. Scale the image to be memory friendly using ShowPicture method
                        Bitmap adjustedBitmap = ShowPicture(pictureFile);
                        //Create a relationship between the object name returned by the FireBase database query and the corresponding file using a HashMap
                        coverImageMap.put(theObjectName, adjustedBitmap);
                        //Update the number of thumbnails required to present the image gallery and the background colours for each thumbnail
                        thumbnailColours();
                        //Set the progress bar to be invisible
                        progressBar.setVisibility(View.INVISIBLE);
                        //Update the image adapter to reflect the image thumbnails downloaded
                        cloudImageAdapter = new ArchiveImageAdapter(activity, context, numberOfThumbs, folderFiles, colourCodeArray, objectRecordMap, coverImageMap, authenticated, commentaryInstruction);
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

            catch (IOException error) {

            }
    }

    //Load the image thumbnails from local storage
    void setupListsLocal() {

        //Declare new ObjectRecordMap to link story name with an ArrayList of ObjectStoryRecords - meta-data about the file
        objectRecordMap = new LinkedHashMap<String, ArrayList<ObjectStoryRecord>>();

        //Loop through the valid story folders and check whether each story folder subfile is a picture or audio. Use the first picture as the object cover image.
        for (int i = 0; i < validStoryFolders.size(); i++) {

            File[] subFiles = FilesForThumbnail(validStoryFolders.get(i));
            boolean setCoverImage = false;

            //Loop through each story folder subfile
            for (int j = 0; j < subFiles.length; j++) {

                //Variables used to create an ObjectStoryRecord
                String CoverImage = "no";
                String FileContext = "local";
                String FileType = "";
                String extension = FilenameUtils.getExtension(subFiles[j].toString());

                //If the current subfile is a jpg or audio
                    if (extension.equalsIgnoreCase("jpg")&&!setCoverImage) {

//                        coverImage = "yes";
                        FileType = "PictureFile";
                    }

                    if(extension.equalsIgnoreCase("jpg")) {

                        FileType = "PictureFile";
                    }

                    else if(extension.equalsIgnoreCase("mp3")) {

                        FileType = "AudioFile";
                    }

                //Create ObjectStoryRecord for current file
                ObjectStoryRecord objectStoryRecord = new ObjectStoryRecord(validStoryFolders.get(i).getName(), subFiles[j].getName(), "", subFiles[j].getAbsolutePath(), FileType, CoverImage, FileContext);

                //Add if the current valid story folder is in the objectRecord map, add a corresponding ObjectStoryRecord to the map.
                // E.g. Original map may look like (StoryA, [empty]) but now (StoryA, <ObjectStoryRecord1, ObjectStoryRecord2...>)
                if (objectRecordMap.containsKey(validStoryFolders.get(i).getName())) {

                    objectRecordMap.get(validStoryFolders.get(i).getName()).add(objectStoryRecord);
                }

                //If the current valid story is not already in the objectRecord map, add a new entry with both the name of the story and a the list of ObjectStoryRecords
                else {
                    ArrayList<ObjectStoryRecord> objectStoryRecordObjectList = new ArrayList<ObjectStoryRecord>();
                    objectStoryRecordObjectList.add(objectStoryRecord);
                    objectRecordMap.put(validStoryFolders.get(i).getName(), objectStoryRecordObjectList);
                }
            }
        }

        //For each validCoverFolder, get the thumbnail from "valid cover folders" list.
        for (int i = 0; i < validCoverFolders.size(); i++) {

            //For the number of validStoryFolders in the Cover folder, return each file in turn and put it in the coverImageMap
            File thumbnailsFile = Thumbnail(validCoverFolders.get(i));
            getCoverImageLocal(validCoverFolders.get(i).getName(), thumbnailsFile);
        }
    }

    //Put each returned cover image into the coverImageMap which will later be used to display images by the adapter
    void getCoverImageLocal(String ObjectName, File file) {

        final String theObjectName = ObjectName;
        Bitmap adjustedBitmap = ShowPicture(file);

        if (objectRecordMap.containsKey(ObjectName)) {
            coverImageMap.put(theObjectName, adjustedBitmap);
            thumbnailColours();
        }
    }


    public static void put(Map<File, List<File>> map, File key, File value) {
        if(map.get(key) == null){
            map.put(key, new ArrayList<File>());
        }
        map.get(key).add(value);
    }

    //List the containing files for the currently selected story folder (Unique Story Identifier) in the Stories directory and save them in the files array. This stores the each story folder and all its containing files.
    File[] FilesForThumbnail(File file) {

        File directory = new File (getFilesDir() + File.separator + "Stories" + File.separator + file.getName());
        File[] files = directory.listFiles();

        return files;
    }

    //List the containing files for the currently selected cover folder in the Covers directory and saves them in the covers array.
    File Thumbnail(File file) {

        //Change to cover folder ArrayList
        File directory = new File (getFilesDir() + File.separator + "Covers" + File.separator + file.getName());
        File[] covers = directory.listFiles();

        Log.i("Cover Files Folder: ", file.getName());
        Log.i("Cover Files FolSize: ", String.valueOf(covers.length));

        File thumbnail = covers[0];

        return thumbnail;
    }

    //Process picture as a bitmap ready to be displayed in an ImageView, scaled to reduce memory
    //To do: pictures would be better loaded using a dedicated image loader library like Glide
    Bitmap ShowPicture(File pictureFile) {

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
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

    // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        return BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
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
        Intent intent = new Intent(Archive.this, AboutAndLogout.class);
        intent.putExtra("PreviousActivity", "ArchiveMainMenu");
        intent.putExtra("Authenticated", authenticated);
        Archive.this.startActivity(intent);
        overridePendingTransition(R.anim.left_to_right_slide_in_activity, R.anim.left_to_right_slide_out_activity);
    }

    //Forward Back button response to onBackPressed() method
    public void Back(View view) {

        onBackPressed();
    }

    //Logic to take the user back to the home screen - no active button in use for this currently
    public void Home(View view) {

        commentaryInstruction.stopPlaying();
        Intent intent = new Intent(Archive.this, HomeScreen.class);
        intent.putExtra("PreviousActivity", "ArchiveMainMenu");
        intent.putExtra("Authenticated", authenticated);
        Archive.this.startActivity(intent);
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
    }

    // Save the current state of these bundled variables.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean("Authenticated", authenticated);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    //When the back button is pressed undertake this code
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
                Intent intent = new Intent(Archive.this, HomeScreen.class);
                intent.putExtra("PreviousActivity", "ArchiveMainMenu");
                intent.putExtra("Authenticated", authenticated);
                Archive.this.startActivity(intent);
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

    //Async task to handle the loading and process of local thumbnail images on a separate thread - note, FireBase process run on their own thread
    class LoadLocalImages extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... params) {

            boolean authenticated = params[0];
            boolean success = false;


            if(validStoryFolders !=null) {
                setupListsLocal();
                thumbnailColours();
                success = true;
            }

            return success;
        }

        protected void onPostExecute(Boolean success) {

            progressBar.setVisibility(View.INVISIBLE);

            if(success) {
                cloudImageAdapter = new ArchiveImageAdapter(activity, context, numberOfThumbs, folderFiles, colourCodeArray, objectRecordMap, coverImageMap, authenticated, commentaryInstruction);
                gridview.invalidate();
                gridview.setAdapter(cloudImageAdapter);
            }
        }
    }
}

/*
    public void setupLists(File[] validStoryFolders) {

        for (int i = 0; i < validStoryFolders.length; i++) {

            folders.add(validStoryFolders[i]);
            File[] subFiles = FilesForThumbnail(validStoryFolders[i]);

           Log.i("Reached 1: File: ", validStoryFolders[i].getName());

            for (int j = 0; j < subFiles.length; j++) {

                put(folderFiles, validStoryFolders[i], subFiles[j]);

                Log.i("Reached 2: File: Sub", validStoryFolders[i].getName() + ": " + subFiles[j].getName());
            }
        }

        for (Map.Entry<File, List<File>> entry : folderFiles.entrySet()) {
            File key = entry.getKey();
            List<File> value = entry.getValue();

            Log.i("Reached 3: File: Sub", key.getName() + ": " + value.toString());

            int loadNum = 0;

            for(File element : value){

                String extension = FilenameUtils.getExtension(element.toString());
                String fileName = element.toString();

                if (extension.equalsIgnoreCase("jpg")) {

                    folderImages.put(key, element);
                    loadNum++;
                    int progressUpdate = (loadNum*100)/value.size();
                    progressBar.setProgress(progressUpdate);

                    Bitmap test = ShowPicture(element);
                    Log.i("Test element", test.toString());

                    imageFiles.put(key, ShowPicture(element));
                }
            }
        }
    }

    */