package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by StuCollyn on 07/06/2018.
 */

public class CloudImageAdapter extends BaseAdapter implements Serializable {
    private Context mContext;
    ImageView[] imageButtons;
    TextView[] imageDesc;
    int numberOfThumbs = 0;
    boolean imageButtonSelected = false;
    Activity cloudStoryGallery;
    int[] colourCode;
    int mode;
    String queryType;
    LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap;
    ArrayList<String> storyRecords;

    public CloudImageAdapter(Activity cloudStoryGallery, Context c, int numberOfThumbs, int[] colourCode, int mode, ArrayList<String> storyRecords, String queryType, LinkedHashMap<String, ArrayList<StoryRecord>> storyRecordMap) {

        this.cloudStoryGallery = cloudStoryGallery;
        mContext = c;
        this.numberOfThumbs = numberOfThumbs;
        imageButtons = new ImageView[numberOfThumbs];
        imageDesc = new TextView[numberOfThumbs];
        this.colourCode = colourCode;
        this.mode = mode;
        this.storyRecords = storyRecords;
        this.queryType = queryType;
        this.storyRecordMap = storyRecordMap;
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

    /*

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

    */

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View grid;

        if (convertView == null) {

            LayoutInflater inflater;
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.activity_cloud_story_gallery_grid_item, null);

        } else {

            grid = (View) convertView;
        }

        int currentColour = colourCode[position];
        TextView imageCaption = (TextView) grid.findViewById(R.id.grid_cloud_item_text);
        imageButtons[position] = (ImageView) grid.findViewById(R.id.grid_cloud_item_background);
        imageButtons[position].setBackgroundColor(currentColour);
        imageButtons[position].setClickable(true);
        imageCaption.setTextSize(30);

        if (queryType.equals("text")) {

            imageCaption.setText(storyRecords.get(position));

        } else if (queryType.equals("date")) {

            imageCaption.setText(storyRecords.get(position));
        } else if (queryType.equals("image")) {

        }

        else {

            Log.i("QueryType", "none");
        }


        imageButtons[position].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(cloudStoryGallery.getApplicationContext(), ShowCloudStories.class);
                Bundle bundle = new Bundle();
                bundle.putInt("Orientation", mode);
                bundle.putString("StoryName", storyRecords.get(position));
                bundle.putSerializable("StoryRecordArray", storyRecordMap.get(storyRecords.get(position)));
                bundle.putString("QueryType", queryType);
                intent.putExtras(bundle);
                cloudStoryGallery.getApplicationContext().startActivity(intent);
                cloudStoryGallery.overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
            }
        });

        return grid;
    }
}