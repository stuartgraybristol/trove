package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.LinearLayout.LayoutParams;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudStoryGallery extends AppCompatActivity {

    int mode;
    String queryType;
    private StorageReference mStorageRef;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    LinearLayout linearLayout;
    Context context;
    String storyName;
    EditText storySearchBar;
    ImageButton searchButton;
    LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap;
    ArrayList<Button> storyButtonArrayList;

    GridView gridview;
    Activity activity;
    ProgressBar progressBar;
    ArrayList<File> folders;
    LinkedHashMap<File, List<File>> folderFiles;
    HashMap<File, File> folderImages;
    HashMap<File, Bitmap> imageFiles;
    File[] files;
    int colourCounter;
    int currentColour;
    int[] colourCode;
    int numberOfThumbs;
    CloudImageAdapter imageAdapter;
    ArrayList<String> fullList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Cloud Story Gallery");

        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        queryType = (String) getIntent().getExtras().get("QueryType");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_cloud_story_gallery);
//        linearLayout = findViewById(R.id.test_linear);
        storySearchBar = findViewById(R.id.search_bar);
        searchButton = (ImageButton) findViewById(R.id.search);
        context = this;
        storyButtonArrayList = new ArrayList<Button>();
        context = this;
        activity = this;
        progressBar = (ProgressBar) findViewById(R.id.cloud_progressbar);
        queryFireStoreDatabase();

        storySearchBar.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() == 0) {
                    setupImageAdapter(storyRecordMap, fullList);
                }

                else {

                    orderByStoryName(storyRecordMap);
                }
            }

        });
    }

    void setupImageAdapter(LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap, ArrayList<String> storyRecords) {

        progressBar.setVisibility(View.INVISIBLE);
        gridview = (GridView) findViewById(R.id.gridview);


        colourCounter = 0;
        currentColour = Color.parseColor("#756bc7");

        colourCode = new int[storyRecords.size()];


        for (int i = 0; i < storyRecords.size(); i++) {

            if (colourCounter == 0) {

                currentColour = Color.parseColor("#756bc7");
                colourCounter++;
            } else if (colourCounter == 1) {

                currentColour = Color.parseColor("#ffb491");
                colourCounter++;
            } else if (colourCounter > 1) {

                currentColour = Color.parseColor("#54b8a9");
                colourCounter = 0;
            }

            colourCode[i] = currentColour;

        }

        Log.i("StoryMApSize: ", storyRecordMap.size() + ", " + storyRecordMap.toString());

        //new LoadImages().execute();
        numberOfThumbs = storyRecords.size();
        gridview.setAdapter(imageAdapter = new CloudImageAdapter(this, this, numberOfThumbs, colourCode, mode, storyRecords, queryType, storyRecordMap));

    }

    public void SearchByNameButton(View view) {

        for (int i = 0; i < storyButtonArrayList.size(); i++) {

            storyButtonArrayList.get(i).setVisibility(View.GONE);
        }
        storyButtonArrayList.clear();
        orderByStoryName(storyRecordMap);
    }

    void showAllStories(LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap) {
        ArrayList<String> mediaItems = new ArrayList<String>();
        fullList = mediaItems;

        for (Map.Entry<String, ArrayList<StoryRecord>> entry : storyRecordMap.entrySet()) {

            String value = entry.getValue().get(0).getStoryName();
            mediaItems.add(value);
        }
            setupImageAdapter(storyRecordMap, mediaItems);

            /*

            Button valueTV = new Button(context);
            valueTV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            //                                valueTV.setText(document.getId().toString() + " => " + document.getData().toString());
            //valueTV.setText("Story ID: " + document.getId().toString() + " = " +
            //     Html.fromHtml(linkedText)+"\n");
            valueTV.setText(Html.fromHtml(value));
            valueTV.setTextSize(30);
            valueTV.setClickable(true);
            valueTV.setMovementMethod(LinkMovementMethod.getInstance());
            linearLayout.addView(valueTV);

            */
    }

    boolean patternMatch(String value) {

        String findMe = value;
        String searchMe = storySearchBar.getText().toString();
        if (findMe.equals("")) {
            return true;
        }

        if (searchMe == null || findMe == null || searchMe.equals("")) {
            return false;
        }

        if(findMe.toLowerCase().contains(searchMe)) {

            return true;
        }

        Pattern p = Pattern.compile(findMe, Pattern.CASE_INSENSITIVE + Pattern.LITERAL);
        Matcher m = p.matcher(searchMe);
        return m.find();
    }

    void orderByStoryName(LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap) {

        ArrayList<String> mediaItems = new ArrayList<String>();
        boolean matchMade = false;
        for (Map.Entry<String, ArrayList<StoryRecord>> entry : storyRecordMap.entrySet()) {

            String value = entry.getValue().get(0).getStoryName();
            matchMade = patternMatch(value);

            if (matchMade) {

                mediaItems.add(value);
                setupImageAdapter(storyRecordMap, mediaItems);
//                Button valueTV = new Button(context);
//                valueTV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
//                        LayoutParams.WRAP_CONTENT));
//                valueTV.setText(Html.fromHtml(value));
//                valueTV.setTextSize(30);
//                valueTV.setClickable(true);
//                valueTV.setMovementMethod(LinkMovementMethod.getInstance());
//                linearLayout.addView(valueTV);
//                storyButtonArrayList.add(valueTV);
            }
        }
    }

    void orderByDate(LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap) {

        ArrayList<String> storyNames = new ArrayList<String>();
        ArrayList<String> storyDates = new ArrayList<String>();

        for (Map.Entry<String, ArrayList<StoryRecord>> entry : storyRecordMap.entrySet()) {

            String date = entry.getValue().get(0).getStoryDate();
            String name = entry.getValue().get(0).getStoryName();
            storyNames.add(name);
            storyDates.add(date);
        }

        setupImageAdapter(storyRecordMap, storyNames);
    }

    void orderByImage(LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap) {

        for (Map.Entry<String, ArrayList<StoryRecord>> entry : storyRecordMap.entrySet()) {

            HashMap<String, String> dates= new HashMap<String, String>();
            String value = entry.getValue().get(0).getStoryType();

            if(value.equals("PictureFile")) {

                ImageButton valueTV = new ImageButton(context);
                valueTV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
//            valueTV.setImageResource();
                valueTV.setClickable(true);
                linearLayout.addView(valueTV);
            }
        }
    }

    void queryFireStoreDatabase() {

        storyRecordMap = new LinkedHashMap<String, ArrayList<StoryRecord>>();

        Log.i("Query: ", "Querying...");
        String userID = mAuth.getCurrentUser().getEmail();
        CollectionReference citiesRef = db.collection("Stories");
        Query query = citiesRef.whereEqualTo("Username", userID).orderBy("Date", Query.Direction.DESCENDING);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.i("Successful Query", document.getId() + " => " + document.getData());

//                                String StoryID = document.getId().toString();
                                String StoryID = document.getData().get("Story ID").toString();
                                String StoryName = document.getData().get("StoryName").toString();
                                String StoryDate = document.getData().get("Date").toString();
                                String URLlink = document.getData().get("URL").toString();
                                String StoryType = document.getData().get("Type").toString();
                                String linkedText = "<b>Story </b>" + StoryID + " = " +
                                        String.format("<a href=\"%s\">Download Link</a> ", URLlink);

                                StoryRecord storyRecord = new StoryRecord(StoryID, StoryName, StoryDate, URLlink, StoryType);

                                if (storyRecordMap.containsKey(StoryName)) {

                                    storyRecordMap.get(StoryName).add(storyRecord);
                                } else {

                                    ArrayList<StoryRecord> storyRecordList = new ArrayList<StoryRecord>();
                                    storyRecordList.add(storyRecord);
                                    storyRecordMap.put(StoryName, storyRecordList);
                                    Log.i("Adding to Test Map: ", StoryName);
                                }
                            }

                            if (queryType.equals("text")) {

                                searchButton.setVisibility(View.VISIBLE);
                                storySearchBar.setVisibility(View.VISIBLE);
                                showAllStories(storyRecordMap);

                            } else if (queryType.equals("image")) {

//                                orderByImage(storyRecordMap);
                            } else if (queryType.equals("date")) {

                                orderByDate(storyRecordMap);
                            }


                            for (Map.Entry<String, ArrayList<StoryRecord>> entry : storyRecordMap.entrySet()) {

                                String key = entry.getKey();
                                ArrayList<StoryRecord> value = entry.getValue();
                            }

                        }

                        else {
                            Log.i("Failed", "error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /*

    public void setupLists(File[] files) {

        for (int i = 0; i < files.length; i++) {

            folders.add(files[i]);
            File[] subFiles = FilesForThumbnail(files[i]);

//            Log.i("Reached 1: File: ", files[i].getName());

            for (int j = 0; j < subFiles.length; j++) {

                put(folderFiles, files[i], subFiles[j]);

//                Log.i("Reached 2: File: Sub", files[i].getName() + ": " + subFiles[j].getName());
            }
        }

        for (Map.Entry<File, List<File>> entry : folderFiles.entrySet()) {
            File key = entry.getKey();
            List<File> value = entry.getValue();

//            Log.i("Reached 3: File: Sub", key.getName() + ": " + value.toString());

            int loadNum = 0;

            for (File element : value) {

                String extension = FilenameUtils.getExtension(element.toString());
                String fileName = element.toString();

                if (extension.equalsIgnoreCase("jpg")) {

                    folderImages.put(key, element);
                    loadNum++;
                    int progressUpdate = (loadNum * 100) / value.size();
                    progressBar.setProgress(progressUpdate);

                    Bitmap test = ShowPicture(element);
//                   Log.i("Test element", test.toString());

                    imageFiles.put(key, ShowPicture(element));

                }
            }

        }

    }

    public static void put(Map<File, List<File>> map, File key, File value) {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<File>());
        }
        map.get(key).add(value);
    }

    File[] FilesForThumbnail(File file) {

        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/" + file.getName();
        File directory = new File(path);
        File[] files = directory.listFiles();

        return files;
    }

    File GetPicture(File[] files) {

        File file = null;

        for (int i = 0; i < files.length; i++) {

            String extension = FilenameUtils.getExtension(files[i].toString());
            String fileName = files[i].toString();

            if (extension.equalsIgnoreCase("jpg")) {

                file = files[i];
            }

        }

        return file;
    }

    Bitmap ShowPicture(File pictureFile) {


        ExifInterface exif = null;
        Bitmap adjustedBitmap;
        try {
            exif = new ExifInterface(pictureFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int smallSizeScaleFactor = Math.min(photoW / 800, photoH / 800);


        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = smallSizeScaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), bmOptions);
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


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(CloudStoryGallery.this, MainMenu.class);
        intent.putExtra("Orientation", mode);
        CloudStoryGallery.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
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


    class LoadImages extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

//            imageView = params[0];

            try {

                setupLists(files);


            } catch (NullPointerException e) {

            } catch (IllegalArgumentException e) {

            }

            return null;

        }

        protected void onPostExecute(Void result) {

            progressBar.setVisibility(View.INVISIBLE);
            gridview.invalidateViews();
            gridview.setAdapter(imageAdapter = new ImageAdapter(activity, context, numberOfThumbs, folders, colourCode, folderImages, imageFiles, mode));

//            for (Map.Entry<File,Bitmap> entry : imageFiles.entrySet()) {
//            File key = entry.getKey();
//            Bitmap value = entry.getValue();
//
//            Log.i("Folders with images: ", "Key: " + key + ", Value: " + value);
//        }
        }
    }
*/

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(CloudStoryGallery.this, StoryGalleryMenu.class);
        intent.putExtra("Orientation", mode);
        CloudStoryGallery.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
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

