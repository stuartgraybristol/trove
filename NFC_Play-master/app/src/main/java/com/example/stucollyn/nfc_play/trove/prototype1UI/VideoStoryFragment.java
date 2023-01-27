package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.stucollyn.nfc_play.R;

/**
 * Created by StuCollyn on 14/05/2018.
 */

public class VideoStoryFragment extends Fragment {

    TextView record_instruction;
    ImageView record_button, captured_video_background, full_screen_video_background;
    VideoView captured_video, full_sized_video;
    ImageButton save_video_button, discard_video_button, expand_video_button, shrink_video_button;
    Button skip_button;
    FragmentActivity listener;
    boolean is_fullscreen_video_on = false;
    MediaController mediaController, fullScreenMediaController;

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
        return inflater.inflate(R.layout.video_story_fragment, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        record_instruction = (TextView) view.findViewById(R.id.video_record_instruction);
        record_button = (ImageView) view.findViewById(R.id.video_record_button);
        save_video_button = (ImageButton) view.findViewById(R.id.save_video_button);
        discard_video_button = (ImageButton) view.findViewById(R.id.discard_video_button);
        skip_button = (Button) view.findViewById(R.id.video_skip_button);
        captured_video = (VideoView) view.findViewById(R.id.captured_video);
        full_sized_video = (VideoView) view.findViewById(R.id.full_sized_video);
        expand_video_button = (ImageButton) view.findViewById(R.id.expand_video_button);
        shrink_video_button = (ImageButton) view.findViewById(R.id.shrink_video_button);
        captured_video_background = (ImageView) view.findViewById(R.id.captured_video_background);
        full_screen_video_background = (ImageView) view.findViewById(R.id.full_screen_video_background);
        mediaController = new MediaController(getActivity());
        fullScreenMediaController = new MediaController(getActivity());


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

    public void TakeVideo(View view) {

        record_button.setImageResource(R.drawable.video_on_min);
        record_instruction.setText("Video Recorder starting...");
        skip_button.setVisibility(View.INVISIBLE);
    }

    public void ShowFullSizedVideo(boolean full_sized_video_on, Uri takenVideo) {

        if(!full_sized_video_on) {

            captured_video.suspend();
            captured_video.setVisibility(View.INVISIBLE);
            expand_video_button.setVisibility(View.INVISIBLE);
            shrink_video_button.setVisibility(View.VISIBLE);
            full_screen_video_background.setVisibility(View.VISIBLE);
            full_sized_video.setVisibility(View.VISIBLE);
            full_sized_video.setVideoURI(takenVideo);
            fullScreenMediaController.setAnchorView(full_sized_video);
            full_sized_video.setMediaController(fullScreenMediaController);
            full_sized_video.start();
        }

        else {
            full_sized_video.setVisibility(View.INVISIBLE);
            full_screen_video_background.setVisibility(View.INVISIBLE);
            shrink_video_button.setVisibility(View.INVISIBLE);
            expand_video_button.setVisibility(View.VISIBLE);
            captured_video.setVisibility(View.VISIBLE);
            captured_video.resume();
            captured_video.start();
//            captured_video.setVideoURI(takenVideo);
//            mediaController.setAnchorView(captured_video);
//            captured_video.setMediaController(mediaController);
//            captured_video.start();
        }

    }

    public void ShowVideo(Uri takenVideo) {

        record_instruction.setText("Press the green tick to save or the red cross to delete and take your video again.");
        captured_video.setVisibility(View.VISIBLE);
        captured_video_background.setVisibility(View.VISIBLE);
        save_video_button.setVisibility(View.VISIBLE);
        discard_video_button.setVisibility(View.VISIBLE);
        expand_video_button.setVisibility(View.VISIBLE);
        record_button.setVisibility(View.INVISIBLE);
        skip_button.setVisibility(View.INVISIBLE);

        captured_video.setVideoURI(takenVideo);
        mediaController.setAnchorView(captured_video);
        captured_video.setMediaController(mediaController);
        captured_video.start();

    }

    public void DiscardVideo() {

        record_instruction.setText("Touch the yellow imageView to take video.");
        captured_video.setVisibility(View.INVISIBLE);
        captured_video_background.setVisibility(View.INVISIBLE);
        save_video_button.setVisibility(View.INVISIBLE);
        discard_video_button.setVisibility(View.INVISIBLE);
        expand_video_button.setVisibility(View.INVISIBLE);
        record_button.setVisibility(View.VISIBLE);
        skip_button.setVisibility(View.VISIBLE);
        record_button.setImageResource(R.drawable.video_off_min);
    }
}