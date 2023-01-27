package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.io.IOException;

/**
ShowStoryContentDialog is responsible for showing a DialogFragment containing an image of a story file selected.
 */

public class ShowStoryContentDialog extends DialogFragment {

/*  The activity that creates an instance of this dialog fragment must implement this interface in order to receive event callbacks.
    Each method passes the DialogFragment in case the host needs to query it.

    public interface LoginOrSignUpDialogListener {
        public void onLoginDialogPositiveClick(String username, String password);
        public void onLoginDialogNegativeClick(DialogFragment dialog);
    }

    Use this instance of the interface to deliver action events
    LoginDialogFragmentKidsUI.LoginOrSignUpDialogListener mListener; */

    ImageView imageView;
    Uri story_directory_uri;
    Bitmap adjustedFullSizedBitmap, adjustedBitmap;
    int rotationInDegrees;
    File imageFile;

    public void setImageFile(File imageFile) {

        this.imageFile = imageFile;
    }

    //Get the image from the image's file path, generate it as a bitmap and orientate it so that it faces the right way
    public void displayImage() {

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        rotationInDegrees = exifToDegrees(rotation);

//        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//         int smallSizeScaleFactor = Math.min(photoW/500, photoH/500);
//
//
//
//        // Decode the image file into a Bitmap sized to fill the View
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = smallSizeScaleFactor;
//        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
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

        //Set the imageView to the bitmap of the image file
        imageView.setImageBitmap(adjustedBitmap);

        //When you click on the image, close the dialog
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dismiss();
            }
        });
    }

    //Depending on the image orientation, rotate the image
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

    //Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    //Run when the dialog box is created
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        Bundle bundle = this.getArguments();
        imageFile = (File) bundle.getSerializable("ImageFile");        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View prompt = inflater.inflate(R.layout.dialog_show_story_kids_ui, null);
        imageView = (ImageView) prompt.findViewById(R.id.imageView);
        final AlertDialog alertDio = new AlertDialog.Builder(getActivity(), R.style.CustomDialog)
                .setView(prompt)
                .show();

        //Set dialog background to be transparent and colour and the layout to match the parameters of the parent activity
        alertDio.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDio.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        displayImage();

        return alertDio;
    }
}
