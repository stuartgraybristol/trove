package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by StuCollyn on 07/06/2018.
 */

class PictureRecorder extends Application {

    //File Save Variables
    File story_directory, image;
    private Context context;
    String videoPath, photoPath;
    Activity activity;
    int rotationInDegrees;
    Bitmap adjustedBitmap;
    Uri photoURI;

    public PictureRecorder(Activity activity, Context context, File story_directory) {
        this.context = context;
        this.story_directory = story_directory;
        this.activity = activity;
    }

    void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // progressToRecordStory only if the File was successfully created
            if (photoFile != null) {
               // photoURI = FileProvider.getUriForFile(this,
                  //      "com.example.android.fileprovider",
                    //    photoFile);
                photoURI = FileProvider.getUriForFile(context, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //new ProcessPicture().execute();
                activity.startActivityForResult(takePictureIntent, 100);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir;

        if (Build.VERSION.SDK_INT >= 19) {
            //storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            storageDir = story_directory;
        }

        else {
            //storageDir = getExternalFilesDir(Environment.getExternalStorageDirectory() + "/Documents");
            storageDir = story_directory;
        }

        image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        Log.i("File Name", "1" + photoPath);
        return image;
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

    void PictureProcessing() {

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        rotationInDegrees = exifToDegrees(rotation);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int smallSizeScaleFactor = Math.min(photoW / 200, photoH / 200);


        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = smallSizeScaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
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
    }

    void fullScreenPicture() {
          /* if(!fullSizedPicture) {
            Bitmap fullSizedBitmap = BitmapFactory.decodeFile(photoPath);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            adjustedFullSizedBitmap = Bitmap.createBitmap(fullSizedBitmap, 0, 0, fullSizedBitmap.getWidth(), fullSizedBitmap.getHeight(), matrix, true);
        }

        fullSizedPicture = !fullSizedPicture;
        picture_story_fragment.ShowFullSizedPicture(fullSizedPicture, adjustedFullSizedBitmap);
        */

        //        Intent openFullSize = new Intent(Intent.ACTION_VIEW);
//        openFullSize.setDataAndType(Uri.fromFile(file), "image/");
//        openFullSize.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    }

    Bitmap getAdjustedBitmap() {

        return adjustedBitmap;
    }

    int getRotationInDegrees() {

        return rotationInDegrees;
    }

    String getPhotoPath() {

        return photoPath;
    }

    Uri getPhotoURI() {

        return photoURI;
    }

}
