package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.example.stucollyn.nfc_play.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class LocalStoryGallery extends AppCompatActivity {

    ArrayList<File> folders;
    LinkedHashMap<File, List<File>> folderFiles;
    HashMap<File, File> folderImages;
    HashMap<File, Bitmap> imageFiles;
    File[] files;
    ImageAdapter imageAdapter;
    GridView gridview;
    int colourCounter;
    int currentColour;
    int[] colourCode;
    int numberOfThumbs;
    Context context;
    Activity activity;
    int mode;
    ProgressBar progressBar;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mode = (Integer) getIntent().getExtras().get("Orientation");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(mode);
        getSupportActionBar().setTitle("Story Library");
        setContentView(R.layout.activity_story_gallery);
        gridview = (GridView) findViewById(R.id.gridview);
        context = this;
        activity = this;

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/";
        File directory = new File(path);
        files = directory.listFiles();

        folders = new ArrayList<File>();
        folderFiles = new LinkedHashMap<>();
        folderImages = new HashMap<File, File>();
        imageFiles = new HashMap<File, Bitmap>();

        colourCounter = 0;
        currentColour = Color.parseColor("#756bc7");;
        colourCode = new int[files.length];


        for (int i = 0; i < files.length; i++) {

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

        new LoadImages().execute();
        numberOfThumbs = files.length;
//        gridview.setAdapter(imageAdapter = new ImageAdapter(this, this, numberOfThumbs, folders, colourCode, folderImages, imageFiles, mode));
    }

    void downloadFromCloud() throws IOException {

        File localFile = File.createTempFile("images", "jpg");
        StorageReference riversRef = null;
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }

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

            for(File element : value){

                String extension = FilenameUtils.getExtension(element.toString());
                String fileName = element.toString();

                if (extension.equalsIgnoreCase("jpg")) {

                   folderImages.put(key, element);
                    loadNum++;
                    int progressUpdate = (loadNum*100)/value.size();
                    progressBar.setProgress(progressUpdate);

                   Bitmap test = ShowPicture(element);
//                   Log.i("Test element", test.toString());

                   imageFiles.put(key, ShowPicture(element));

                }
            }

        }

    }

    public static void put(Map<File, List<File>> map, File key, File value) {
        if(map.get(key) == null){
            map.put(key, new ArrayList<File>());
        }
        map.get(key).add(value);
    }

    File[] FilesForThumbnail(File file) {

            String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+file.getName();
            File directory = new File(path);
            File[] files = directory.listFiles();

            return files;
        }

    File GetPicture(File[] files) {

        File file = null;

        for(int i = 0; i<files.length; i++) {

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

        Intent intent = new Intent(LocalStoryGallery.this, StoryGalleryMenu.class);
        intent.putExtra("Orientation", mode);
        LocalStoryGallery.this.startActivity(intent);
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
}
