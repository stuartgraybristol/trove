package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

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

/*
This class is used to save a new series of story files to the cloud - FireBase database cloud storage.
*/

public class SaveToCloud {

    //FireBase Variables
    private StorageReference mStorageRef;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseStorage storage;

    //File Storage Variables
    File fileDirectory = null;
    String object;
    HashMap<String,String> selectedMedia;
    UUID objectName;
    String fileType;
    String coverImage;

    //SaveToCloud Constructor - requires source file directory and the name of the object the associated story files concern
    SaveToCloud(File fileDirectory, UUID objectName) {

       this.fileDirectory = fileDirectory;
       this.objectName = objectName;

       //Get FireBase cloud storage and database credentials
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    //Prepare to save to cloud story files about an existing object
    //To do: Currently it uploads every file in the containing folder. This may not be appropriate or desired if some of these stories have already been previously
    //uploaded.
    void CloudSaveNewStory() {

        //Find an object's story folder and list the contained files
        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/validStoryFolders/Stories/"+fileDirectory.getName();
        File directory = new File(path);
        File[] files = directory.listFiles();

        //For every file in the story folder, find out what its story type is. Launch an upload operation on each file to the cloud.
        for (int i = 0; i < files.length; i++) {

            String extension = FilenameUtils.getExtension(files[i].toString());
            String fileName = files[i].toString();
            coverImage = "no";

            if (extension.equalsIgnoreCase("jpg")) {

                Log.i("Uploading Audio", files[i].toString());
                fileType = "PictureFile";
            }

            else if (extension.equalsIgnoreCase("mp3")) {

                Log.i("Uploading Audio", files[i].toString());
                fileType = "AudioFile";
            }

            //Upload file to cloud
            uploadToCloud(files[i], i);
        }
    }

    //Prepare to upload a new object and its associated stories to the cloud.
    void CloudSaveNewObject() {

        //Find an object's story folder and list the contained files
        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/validStoryFolders/Stories/"+fileDirectory.getName();
        File directory = new File(path);
        File[] files = directory.listFiles();

        //For every file in the story folder, find out what its story type is. Launch an upload operation on each file to the cloud.
        for (int i = 0; i < files.length; i++) {

                String extension = FilenameUtils.getExtension(files[i].toString());
                String fileName = files[i].toString();
                coverImage = "no";

                if (extension.equalsIgnoreCase("jpg")) {

                    Log.i("Uploading Audio", files[i].toString());
                    fileType = "PictureFile";
                    coverImage = "yes";
//                    File to = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/validStoryFolders/Stories/"+fileDirectory.getName(),".jpg");
//                    validStoryFolders[i].renameTo(to);
                }

                else if (extension.equalsIgnoreCase("mp3")) {

                    Log.i("Uploading Audio", files[i].toString());
                    fileType = "AudioFile";
                }

            //Launch upload file to cloud operation
            uploadToCloud(files[i], i);
            }
    }

    //Upload file to cloud
    void uploadToCloud(File fileToUpload, int i) {

        //Create new upload task
        UploadTask uploadTask;
        Uri file = Uri.fromFile(fileToUpload);
        String number = String.valueOf(i);
        //Use authorization token to get the current user's ID
        String userID = mAuth.getCurrentUser().getUid();
        //Create new storage reference which makes use of the object's unique identifier name as a cloud storage sub-folder
        StorageReference reference = mStorageRef.child(userID).child(objectName.toString()).child(number);
        //Upload object/story details to a database as well as the file cloud storage
        uploadToDatabase(reference);
        //Launch upload operation
        uploadTask = reference.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.i("Mission Failed", "Failed ");
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.i("Mission Accomplished", "Completed ");

                /*


                At this point, delete any temporary local files to save storage.


                 */
            }
        });
    }

    //Upload to database information about the uploaded file
    void uploadToDatabase(StorageReference reference) {

        //Connect with database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getEmail();
        String storage = reference.toString();
        //Generate new random identifier for the story
        String storyName = UUID.randomUUID().toString();

        //Create a Hash Map of values
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("Username", name);
        newUser.put("storyName", storyName.toString());
        newUser.put("Type", fileType);
        newUser.put("URL", storage);
        newUser.put( "Date", FieldValue.serverTimestamp());
        newUser.put( "objectName", objectName.toString());
        newUser.put( "Cover", coverImage);


        db.collection("ObjectStory")
                .add(newUser)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
//                        Log.i("Success", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Log.i("Failure", "Error adding document", e);
                    }
                });
    }

    //Delete local versions of successfully uploaded files
    void DeleteLocalFiles(File fileToDelete) {

        if (fileDirectory.isDirectory()) {
            String[] children = fileDirectory.list();

            if (children.length == 0) {

                fileDirectory.delete();
            }

            fileToDelete.delete();
        }
    }
}
