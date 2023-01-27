package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

/**
 * Created by StuCollyn on 14/05/2018.
 */

public class PictureStoryFragment extends Fragment {

    TextView record_instruction;
    ImageView record_button, captured_image, full_sized_picture;
    ImageButton save_picture_button, discard_picture_button;
    Button skip_button;
    FragmentActivity listener;

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (AppCompatActivity) context;
        }
    }

    // This event fires 2nd, before views are created for the fragment
    // The onCreate method is called when the Fragment instance is being created, or re-created.
    // Use onCreate for any standard setup that does not require the activity to be fully created
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.picture_story_fragment, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        record_instruction = (TextView) view.findViewById(R.id.picture_record_instruction);
        record_button = (ImageView) view.findViewById(R.id.picture_record_button);
        save_picture_button = (ImageButton) view.findViewById(R.id.save_picture_button);
        discard_picture_button = (ImageButton) view.findViewById(R.id.discard_picture_button);
        skip_button = (Button) view.findViewById(R.id.picture_skip_button);
        captured_image = (ImageView) view.findViewById(R.id.captured_image);
        full_sized_picture = (ImageView) view.findViewById(R.id.full_sized_picture);
    }

    // This method is called when the fragment is no longer connected to the Activity
    // Any references saved in onAttach should be nulled out here to prevent memory leaks.
    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    // This method is called after the parent Activity's onCreate() method has completed.
    // Accessing the view hierarchy of the parent activity must be done in the onActivityCreated.
    // At this point, it is safe to search for activity View objects by their ID, for example.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void TakePicture(View view) {

        record_button.setImageResource(R.drawable.camera_on_min);
        record_instruction.setText("Camera starting...");
        skip_button.setVisibility(View.INVISIBLE);
    }

    public int getPictureBoxWidth() {

        return captured_image.getWidth();
    }

    public int getPictureBoxHeight() {

        return captured_image.getHeight();
    }

    public void ShowFullSizedPicture(boolean full_sized_picture_on, Bitmap bitmap) {

        if(full_sized_picture_on){

            full_sized_picture.setImageBitmap(bitmap);
            full_sized_picture.setVisibility(View.VISIBLE);
        }

        else {

            full_sized_picture.setVisibility(View.INVISIBLE);
        }

    }

    public void setPictureBoxDimensions(int rotation) {

        if(rotation==90||rotation==-90) {
            captured_image.getLayoutParams().height = 550;
            captured_image.getLayoutParams().width = 450;
        }

        else {
            captured_image.getLayoutParams().height = 400;
            captured_image.getLayoutParams().width = 500;
        }
    }

    public void ShowPicture(Bitmap takenPicture) {

        captured_image.setImageBitmap(takenPicture);
        record_instruction.setText("Press the green tick to save or the red cross to delete and take your photo again.");
        captured_image.setVisibility(View.VISIBLE);
        save_picture_button.setVisibility(View.VISIBLE);
        discard_picture_button.setVisibility(View.VISIBLE);
        record_button.setVisibility(View.INVISIBLE);
        skip_button.setVisibility(View.INVISIBLE);
    }

    public void DiscardPicture() {

        record_instruction.setText("Touch the blue imageView to take picture.");
        captured_image.setVisibility(View.INVISIBLE);
        save_picture_button.setVisibility(View.INVISIBLE);
        discard_picture_button.setVisibility(View.INVISIBLE);
        record_button.setVisibility(View.VISIBLE);
        skip_button.setVisibility(View.VISIBLE);
        record_button.setImageResource(R.drawable.camera_off_min);
    }
}
