package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaveSelector extends AppCompatActivity {

    File fileDirectory = null;
    String tag_data = "";
    String storyNameString, tag1String, tag2String, tag3String;
    int mode;
    private StorageReference mStorageRef;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Date FireStoreTime;
    FirebaseStorage storage;
    boolean isNetworkConnected;
    HashMap<String,String> selectedMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_selector);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Save New Story");
        fileDirectory = (File)getIntent().getExtras().get("StoryDirectory");
        tag_data = (String)getIntent().getExtras().get("TagData");
        storyNameString = (String)getIntent().getExtras().get("StoryName");
        tag1String = (String)getIntent().getExtras().get("Tag1");
        tag2String = (String)getIntent().getExtras().get("Tag2");
        tag3String = (String)getIntent().getExtras().get("Tag3");
        selectedMedia = new HashMap<String,String>();
        selectedMedia = (HashMap<String,String>)getIntent().getSerializableExtra("RecordedMedia");
        isNetworkConnected = false;
        isNetworkConnected = isNetworkConnected();
        if(isNetworkConnected) {

            ImageButton cloudButton = (ImageButton) findViewById(R.id.save_to_cloud);
            TextView cloudButtonCaption = (TextView) findViewById(R.id.save_to_cloud_caption);
            cloudButton.setVisibility(View.VISIBLE);
            cloudButtonCaption.setVisibility(View.VISIBLE);
        }
    }

    public void SaveLocally(View view){

        LocalSave();
        Intent intent = new Intent(SaveSelector.this, SavedStoryConfirmation.class);
        intent.putExtra("Orientation", mode);
        SaveSelector.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    public void StartSaveStoryToNFC(View view) {

        setFileName();
        Intent intent = new Intent(SaveSelector.this, SaveStoryToNFC.class);
        intent.putExtra("TagData", tag_data);
        intent.putExtra("Orientation", mode);
        SaveSelector.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    public void SaveToCloud(View view) {

        CloudSave();
        Intent intent = new Intent(SaveSelector.this, SavedStoryConfirmation.class);
        intent.putExtra("Orientation", mode);
        SaveSelector.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(SaveSelector.this, NewStorySaveMetadata.class);
        intent.putExtra("StoryDirectory", fileDirectory);
        intent.putExtra("Orientation", mode);
        intent.putExtra("TagData", tag_data);
        intent.putExtra("RecordedMedia", selectedMedia);
        SaveSelector.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    private boolean isNetworkConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void setFileName() {

        File stories = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/");
        File dir = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+fileDirectory.getName());

        if(dir.exists()){
//            Log.i("Name Change", "Found dir");
//            Log.i("FileDirectory", dir.getName());
            File from = new File(stories, dir.getName());
            File to = new File(stories, storyNameString + " " + dir.getName());
            if(from.exists()) {
                from.renameTo(to);
                tag_data = storyNameString + " " + dir.getName();
            }

            else {

//                Log.i("Name Change", "Canny find it mate");
            }
        }

        /*
        String currentFileName = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+fileDirectory;
        currentFileName = currentFileName.substring(1);
        Log.i("Current file name", currentFileName);

        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/");
        File from      = new File(directory, currentFileName);
        File to        = new File(directory, storyNameString);
        from.renameTo(to);

        */
//        Log.i("Directory is", directory.toString());
//        Log.i("Default path is", videoURI.toString());
//        Log.i("From path is", from.toString());
//        Log.i("To path is", to.toString());
    }

    void LocalSave() {

        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+fileDirectory.getName();
        File directory = new File(path);
        File[] files = directory.listFiles();
        setFileName();

        if(!isNetworkConnected) {
            String name = UUID.randomUUID().toString();
            String cloudPath = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/CloudUpload/" + name;
            File cloudDirectory = new File(cloudPath);
            File[] cloudFiles = directory.listFiles();
        }
    }

    void DeleteLocalFiles() {

        Log.i("File Directory", fileDirectory.toString());

        if (fileDirectory.isDirectory())
        {
            String[] children = fileDirectory.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(fileDirectory, children[i]).delete();
            }
        }

        boolean deletedFile = fileDirectory.delete();
    }

    void CloudSave() {

        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+fileDirectory.getName();
        File directory = new File(path);
        UUID storyUUID = UUID.randomUUID();
        String fileType = "";
        File[] files = directory.listFiles();

        if(isNetworkConnected) {

            for (int i = 0; i < files.length; i++) {

                String extension = FilenameUtils.getExtension(files[i].toString());
                String fileName = files[i].toString();

                if (extension.equalsIgnoreCase("jpg")) {

                    Log.i("Uploading Picture", " true");
                    fileType = "PictureFile";
                } else if (extension.equalsIgnoreCase("mp3")) {

                    Log.i("Uploading Audio", " true");
                    fileType = "AudioFile";
                } else if (extension.equalsIgnoreCase("mp4")) {

                    Log.i("Uploading Video", " true");
                    fileType = "VideoFile";
                } else if (extension.equalsIgnoreCase("txt")) {

                    Log.i("Uploading Text", " true");
                    fileType = "WrittenFile";
                }

                uploadToCloud(files[i], storyUUID, fileType);
            }
        }
    }

    void uploadToCloud(File fileToUpload, final UUID storyUUID, final String fileType) {

        UploadTask uploadTask;
        Uri file = Uri.fromFile(fileToUpload);
        String userID = mAuth.getCurrentUser().getUid();
        String name = UUID.randomUUID().toString();
        Log.i("User ID", userID);
        StorageReference riversRef = mStorageRef.child(userID).child(name);
        Log.i("Rivers Ref Name ", " " + name);
        Log.i("Rivers Ref Name Ref ", " " + riversRef.toString());
        uploadToDatabase(storyUUID, fileType, riversRef);
        uploadTask = riversRef.putFile(file);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.i("Mission Accomplished", "Completed ");
                DeleteLocalFiles();

            }
        });
    }

    void uploadToDatabase(UUID storyUUID, String fileType, StorageReference reference) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getEmail();
        String storage = reference.toString();

        Log.i("Reference", reference.toString());


        Map<String, Object> newUser = new HashMap<>();
        newUser.put("Username", name);
        newUser.put("Story ID", storyUUID.toString());
        newUser.put("Type", fileType);
        newUser.put("URL", storage);
        newUser.put( "Date", FieldValue.serverTimestamp());
        newUser.put( "StoryName", storyNameString);
        newUser.put( "Tag 1", tag1String);
        newUser.put( "Tag 2", tag2String);
        newUser.put( "Tag 3", tag3String);

        db.collection("Stories")
                .add(newUser)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("Success", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Failure", "Error adding document", e);
                    }
                });
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
