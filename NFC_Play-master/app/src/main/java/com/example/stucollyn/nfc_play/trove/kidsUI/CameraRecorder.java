package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 The CameraRecorder class handles setup and control of the camera, as well as saving and processing taken image.
 */

class CameraRecorder extends Application {

    //File save variables
    File story_directory, tag_directory = null, cover_directory = null, image, tagFile, coverFile;
    String videoPath, photoPath;

    //Activity variables
    private Context context;
    Activity activity;

    //Picture variables
    int rotationInDegrees;
    Bitmap adjustedBitmap;
    Uri photoURI;

    //ImageViews and animations
    Animation fadein, fadeout;

    //Camera components
    ImageButton captureButton;
    FrameLayout preview;
    LinearLayout camera_linear;
    Camera mCamera;
    CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private boolean safeToTakePicture = false;

    //CameraRecorder constructor
    public CameraRecorder(Activity activity, Context context, File story_directory, File tag_directory, File cover_directory, Camera mCamera, CameraPreview mPreview) {
        //Initialize received values for variables
        this.context = context;
        this.story_directory = story_directory;
        this.tag_directory = tag_directory;
        this.cover_directory = cover_directory;
        this.activity = activity;
        this.mCamera = mCamera;
        this.mPreview = mPreview;

        //Initialize layouts and animations
        captureButton = (ImageButton) activity.findViewById(R.id.button_capture);
        preview = (FrameLayout) activity.findViewById(R.id.camera_preview);
        camera_linear = (LinearLayout) activity.findViewById(R.id.camera_linear);
        fadein = AnimationUtils.loadAnimation(context, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(context, R.anim.fadeout);
    }

    //Get instance of the camera and open it.
    public static Camera getCameraInstance() {

        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a front Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.i("Tag", "No Camera here mate");
        }
        return c; // returns null if camera is unavailable
    }

    //Set the boolean denoting the safety to take a picture
    void setSafeToTakePicture(boolean safeToTakePicture) {

        this.safeToTakePicture = safeToTakePicture;
    }

    //Return boolean denoting whether it is safe to take a picture
    boolean getSafeToTakePicture() {

        return safeToTakePicture;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    File getOutputMediaFile(int type) {

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {

            //Give picture story file a UUID
            UUID storyName = UUID.randomUUID();
            String imageFileName = storyName.toString();


            /*Create output media files for the captured picture data - one in story_directory, one int tag_directory, and one in cover_directory. In other parts of
            the app, these three files are used in different ways when retrieving LOCAL files (not cloud based). When displaying the story in the archive explorer the
            file is read from the story_directory. When reading from a NFC tag, it is read from the tag_directory. When displaying story thumbnail covers, it is read
            from the cover_directory.*/
            mediaFile = new File(story_directory.getPath() + File.separator +
                    imageFileName + ".jpg");
            photoPath = mediaFile.getAbsolutePath();

            if (tag_directory != null) {
                Log.i("Cool", "t directory empty" + ": " + tag_directory.toString());
                tagFile = new File(tag_directory.getPath() + File.separator +
                        imageFileName + ".jpg");
            }

            if (cover_directory != null) {
                Log.i("Cool", "c directory empty" + ": " + cover_directory.toString());
                coverFile = new File(cover_directory.getPath() + File.separator +
                        imageFileName + ".jpg");
            }

        } else {
            return null;
        }

        //Return the file
        return mediaFile;
    }

    //Return the tagFile
    File getTagFile() {

        return tagFile;
    }

    //Return the coverFile
    File getCoverFile() {

        return coverFile;
    }

    //This method is used to copy the original picture file to other folders.
    //To do: as this code is used in other parts of the program, this could be made its own class.
    public void copyFile(File sourceFile, File destFile) throws IOException {

        //If the file's storage destination doesn't exist, create it
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        //If the output file itself does not already exist, create it
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        //Declare File channels
        FileChannel source = null;
        FileChannel destination = null;

        //Apply channel to copy from the source file to the destination file
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }

        //Close channels after try block has ended
        finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    //Note - not currently used. Return processed and scaled bitmap
    Bitmap getAdjustedBitmap() {

        return adjustedBitmap;
    }

    //Note - not currently used. Rotate image bitmap
    int getRotationInDegrees() {

        return rotationInDegrees;
    }
}

/* Additional code for taking and processing picture images: currently not required


    private File createImageFile() throws IOException {
        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
        UUID storyName = UUID.randomUUID();;
        String imageFileName = storyName.toString();
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
//        tagImage = File.createTempFile(imageFileName, ".jpg", storageDir, tag_directory);

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        return image;
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

       //Return the photo
    String getPhotoPath() {

        return photoPath;
    }

    Uri getPhotoURI() {

        return photoURI;
    }

    */

