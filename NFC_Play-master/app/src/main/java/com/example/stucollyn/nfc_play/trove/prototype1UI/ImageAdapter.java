package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by StuCollyn on 07/06/2018.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    ImageView[] imageButtons;
    TextView[] imageDesc;
    int numberOfThumbs = 0;
    boolean imageButtonSelected = false;
    Activity storyGallery;
    ArrayList<File> filesOnTag;
    int[] colourCode;
    HashMap<File, Bitmap> imageMap;
    HashMap<File, File> folderToImageRef;
    int mode;

    public ImageAdapter(Activity storyGallery, Context c, int numberOfThumbs, ArrayList<File> filesOnTag, int[] colourCode, HashMap<File, File> folderToImageRef, HashMap<File, Bitmap> imageMap, int mode) {

        this.storyGallery = storyGallery;
        mContext = c;
        this.numberOfThumbs = numberOfThumbs;
        imageButtons = new ImageView[numberOfThumbs];
        imageDesc = new TextView[numberOfThumbs];
        this.filesOnTag = filesOnTag;
        this.colourCode = colourCode;
        this.imageMap = imageMap;
        this.folderToImageRef = folderToImageRef;
        this.mode = mode;

        /*
        Log.i("Files in Folder ASize: ", String.valueOf(filesOnTag.size()));
        Log.i("Number of thumbs: ", String.valueOf(numberOfThumbs));



        for(int i=0; i<filesOnTag.size(); i++) {
            Log.i("Files in Folder Array: ", filesOnTag.get(i).toString());
        }
        */


        for (Map.Entry<File,File> entry : folderToImageRef.entrySet()) {
            File key = entry.getKey();
            File value = entry.getValue();

//            Log.i("Folders with images: ", "Key: " + key + ", Value: " + value);
        }

        for (Map.Entry<File,Bitmap> entry : imageMap.entrySet()) {
            File key = entry.getKey();
            Bitmap value = entry.getValue();

//            Log.i("Images Bitmaps: ", "Key: " + key + ", Value: " + value);
        }

//        Log.i("Folder to image ref: ", folderToImageRef.toString());
//        Log.i("Image map: ", imageMap.toString());
    }

    @Override
    public int getCount() {
        return numberOfThumbs;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new TextView for each item referenced by the Adapter

    File[] FilesForThumbnail(int position) {

        String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.stucollyn.nfc_play/files/Stories/"+filesOnTag.get(position).getName();
        File directory = new File(path);
        File[] files = directory.listFiles();

        return files;
    }

    boolean CheckForPicture(File[] files, int position, boolean coverExists) {


        return coverExists;
    }

    File GetPicture(File[] files) {

        File file = null;

        for(int i = 0; i<files.length; i++) {

            String extension = FilenameUtils.getExtension(files[i].toString());
            String fileName = files[i].toString();
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        View grid;

        if (convertView == null) {

            LayoutInflater inflater;
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.activity_story_gallery_grid_item, null);





//            File[] files = FilesForThumbnail(position);
//            File file = GetPicture(files);
//
//
//
//            if(file!= null) {
//
//                Bitmap coverConv = ShowPicture(file);
//                imageButtons[position].setImageBitmap(coverConv);
//                imageButtons[position].setBackgroundColor(currentColour);
//
//            }

        }

        else {

            grid = (View) convertView;
        }

        int currentColour = colourCode[position];

        TextView imageCaption = (TextView) grid.findViewById(R.id.grid_item_text);
        imageButtons[position] = (ImageView) grid.findViewById(R.id.grid_item_background);
        imageCaption.setText(filesOnTag.get(position).getName());
        imageButtons[position].setBackgroundColor(currentColour);



            if(!imageMap.isEmpty()) {

            File value = folderToImageRef.get(filesOnTag.get(position));


            for (Map.Entry<File,File> entry : folderToImageRef.entrySet()) {
                File key = entry.getKey();
                File values = entry.getValue();

//                Log.i("Folders with images: ", "Key: " + key + ", Value: " + values);
//
            }

                for (Map.Entry<File,Bitmap> entry : imageMap.entrySet()) {
                    File key = entry.getKey();
                    Bitmap values = entry.getValue();

//                    Log.i("Images Bitmaps: ", "Key: " + key + ", Value: " + values);

                }


           Bitmap bitmap = imageMap.get(filesOnTag.get(position));


             imageButtons[position].setImageBitmap(bitmap);
            }
/*
            if(!imageMap.isEmpty()) {
                if (folderToImageRef.containsKey(filesOnTag[position])) {

                    File value = folderToImageRef.get(filesOnTag[position]);

                    Log.i("value File: ", value.getName());

                    Bitmap bitmap = imageMap.get(value);

                    Log.i("value File: ", bitmap.toString());
                    //imageButtons[position].setImageBitmap(bitmap);

                }
            }

           */


        imageButtons[position].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File[] files = FilesForThumbnail(position);

//                    Log.i("Files: ", String.valueOf(filesOnTag.size()));

//                Log.i("Position Onclick: ", String.valueOf(position));

                //ThumbnailSelected();
                Intent intent = new Intent(storyGallery.getApplicationContext(), StoryGallerySaveOrView.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Orientation", mode);
                intent.putExtra("StoryDetails", filesOnTag.get(position));
                intent.putExtra("filesOnTag", files);
                storyGallery.getApplicationContext().startActivity(intent);
                storyGallery.overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        });

        return grid;
    }

    void ThumbnailSelected() {

        imageButtonSelected = true;
    }

    boolean checkButtonSelection() {

        return  imageButtonSelected;
    }

    // references to our images
    private Integer[] mThumbIds = {

            R.drawable.nfc_icon, R.drawable.nfc
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7
    };
}