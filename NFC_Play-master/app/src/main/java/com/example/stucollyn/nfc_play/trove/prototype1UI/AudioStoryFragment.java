package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.content.Context;
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

public class AudioStoryFragment extends Fragment {

    TextView record_instruction;
    ImageView record_button;
    ImageButton pause_play_button, stop_button, save_audio_button, discard_audio_button;
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
        return inflater.inflate(R.layout.audio_story_fragment, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        record_instruction = (TextView) view.findViewById(R.id.record_instruction);
        record_button = (ImageView) view.findViewById(R.id.record_button);
        pause_play_button = (ImageButton) view.findViewById(R.id.pause_play_button);
        stop_button = (ImageButton) view.findViewById(R.id.stop_button);
        save_audio_button = (ImageButton) view.findViewById(R.id.save_audio_button);
        discard_audio_button = (ImageButton) view.findViewById(R.id.discard_audio_button);
        skip_button = (Button) view.findViewById(R.id.skip_button);
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

    public void AudioRecordButtonSwitch(boolean recordingStatus, View view) {

        if(recordingStatus) {
            record_button.setImageResource(R.drawable.button_on_min);
            record_instruction.setText("Start speaking. Press imageView again to finish.");
            skip_button.setVisibility(View.INVISIBLE);
        }

        else {
            record_button.setImageResource(R.drawable.button_off_min);
            record_instruction.setText("Touch the red imageView to record a story.");
        }

    }

   public void PlaybackButtonSwitch(boolean audioplaying, View view) {

       record_instruction.setText("Use the buttons to listen to your story.");

       if(!audioplaying) {
            pause_play_button.setImageResource(R.drawable.play_arrow_black);
        }

        else {
            pause_play_button.setImageResource(R.drawable.pause_black);
        }

        audioplaying = !audioplaying;
   }

   public void PlayBackAndSaveSetup(View view) {

       record_instruction.setText("Press the green tick to save or the red cross to delete and record something new.");
       pause_play_button.setVisibility(View.VISIBLE);
       stop_button.setVisibility(View.VISIBLE);
       save_audio_button.setVisibility(View.VISIBLE);
       discard_audio_button.setVisibility(View.VISIBLE);
       record_button.setVisibility(View.INVISIBLE);
       skip_button.setVisibility(View.INVISIBLE);

   }

    public void ResetView(View view) {

        record_instruction.setText("Touch the red imageView to record a story.");
        pause_play_button.setVisibility(View.INVISIBLE);
        stop_button.setVisibility(View.INVISIBLE);
        save_audio_button.setVisibility(View.INVISIBLE);
        discard_audio_button.setVisibility(View.INVISIBLE);
        record_button.setVisibility(View.VISIBLE);
        skip_button.setVisibility(View.VISIBLE);
    }

}
