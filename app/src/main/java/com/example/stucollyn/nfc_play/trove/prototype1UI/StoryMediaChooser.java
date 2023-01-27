package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.stucollyn.nfc_play.R;

import java.util.ArrayList;

/*StoryMediaChooser Activity allows user to select the type of media they wish to include in their
new story*/
public class StoryMediaChooser extends AppCompatActivity {

    boolean audio_selected = false, picture_selected = false, video_selected = false,
            written_selected = false, audio = false, picture = false, video = false, written = false;
    ImageButton audio_select_button, picture_select_button, video_select_button, written_select_button,
            audio_confirm_button, picture_confirm_button, video_confirm_button, written_confirm_button,
            confirmation_button;
    String audioMedia = "Audio", pictureMedia = "Picture", videoMedia = "Video",
            writtenMedia = "Written";
    ArrayList<String> selectedMedia;
    int mode;

    //onCreate called when Activity begins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_media_chooser);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBarSetup();
        InitView();

        //Initialize media type array list
        selectedMedia = new ArrayList<String>();
    }

    //Initialize views in Activity
    private void InitView() {

        audio_select_button = findViewById(R.id.audio_media_thumb);
        picture_select_button = findViewById(R.id.picture_media_thumb);
        video_select_button = findViewById(R.id.video_media_thumb);
        written_select_button = findViewById(R.id.written_notes_thumb);
        audio_confirm_button = findViewById(R.id.audio_media_thumb_confirm);
        picture_confirm_button = findViewById(R.id.picture_media_thumb_confirm);
        video_confirm_button = findViewById(R.id.video_media_thumb_confirm);
        written_confirm_button = findViewById(R.id.written_notes_thumb_confirm);
        confirmation_button = findViewById(R.id.confirm_media);
    }

    //Setup action bar
    private void ActionBarSetup() {

        //Display both title and image, and a back button in action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set and show trove logo in action bar
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Set page title shown in action bar
        getSupportActionBar().setTitle("Create New Story");
    }

    //On audio checked/unchecked add/remove from media type array list
    public void AudioSelected(View view) {

        //if audio is previously unchecked, add to array list and add green tick image view
        if(!audio_selected)
        {
            audio_confirm_button.setVisibility(View.VISIBLE);
            addMedia(audioMedia);
        }

        //if audio is previously checked, remove from array list and remove green tick image view
        else
        {
            audio_confirm_button.setVisibility(View.INVISIBLE);
            removeMedia(audioMedia);

        }

        //if audio selected was previously true/false, make false/true
        audio_selected = !audio_selected;
    }

    //On picture checked/unchecked add/remove from media type array list
    public void PictureSelected(View view) {

        //if picture is previously unchecked, add to array list and add green tick image view
        if(!picture_selected)
        {
            picture_confirm_button.setVisibility(View.VISIBLE);
            addMedia(pictureMedia);

        }

        //if picture is previously checked, remove from array list and remove green tick image view
        else
        {
            picture_confirm_button.setVisibility(View.INVISIBLE);
            removeMedia(pictureMedia);

        }

        //if picture selected was previously true/false, make false/true
        picture_selected = !picture_selected;

    }

    //On video checked/unchecked add/remove from media type array list
    public void VideoSelected(View view) {

        //if video is previously unchecked, add to array list and add green tick image view
        if(!video_selected)
        {
            video_confirm_button.setVisibility(View.VISIBLE);
            addMedia(videoMedia);

        }

        //if video is previously checked, remove from array list and remove green tick image view
        else
        {
            video_confirm_button.setVisibility(View.INVISIBLE);
            removeMedia(videoMedia);

        }

        //if video selected was previously true/false, make false/true
        video_selected = !video_selected;

    }

    //On written checked/unchecked add/remove from media type array list
    public void WrittenSelected(View view) {

        //if written is previously unchecked, add to array list and add green tick image view
        if(!written_selected)
        {
            written_confirm_button.setVisibility(View.VISIBLE);
            addMedia(writtenMedia);

        }

        //if written is previously checked, remove from array list and remove green tick image view
        else
        {
            written_confirm_button.setVisibility(View.INVISIBLE);
            removeMedia(writtenMedia);

        }

        //if written selected was previously true/false, make false/true
        written_selected = !written_selected;
    }

    //For a given type of media (audio/picture/video/written), add to selected media array list
    private void addMedia(String string) {

        //Assume given media is not already in array list
        boolean match = false;

        /*If selected media array list is not empty, iterate over list to find out whether or not
        there is an existing entry. If no entry exists, add given media type to array list*/
        if(selectedMedia.size()>0) {

            for (int i = 0; i < selectedMedia.size(); i++) {

                if (string == selectedMedia.get(i)) {

                    match = true;
                }
            }

            if(!match) {

                selectedMedia.add(string);

                Log.i("added media:", selectedMedia.toString());
            }
        }

        //If selected media array list is empty, add given media type to array list
        else {

            selectedMedia.add(string);

            Log.i("added media:", selectedMedia.toString());

        }

        //If at least one media type is selected show confirmation button
        checkConfirm(selectedMedia.size());

    }

    //For a given type of media (audio/picture/video/written), remove from selected media array list
    private void removeMedia(String string) {

        //Assume given media is not already in array list
        boolean match = false;

        /*Iterate over selected media array list to find out whether or not there is an existing
        entry for the given media type. If an entry exists, remove given media type from array
        list*/
        for(int i=0; i<selectedMedia.size(); i++) {

            if(string==selectedMedia.get(i)) {

                match = true;
            }
        }

        if(match) {

            selectedMedia.remove(string);
            Log.i("removed media:", selectedMedia.toString());

        }

        //If at least one media type is selected show confirmation button
        checkConfirm(selectedMedia.size());

    }

    /*If at least one media type is selected show confirmation imageView, otherwise hide confirmation
     imageView*/
    private void checkConfirm(int numberMediaSelected) {


        if (numberMediaSelected > 0) {

            confirmation_button.setVisibility(View.VISIBLE);
        }

        else {

            confirmation_button.setVisibility(View.INVISIBLE);
        }
    }

    //When confirmation button pressed, start RecordStory Activity
    public void Confirm (View view) {

        Intent intent = new Intent(StoryMediaChooser.this, NFCRecord.class);
        intent.putExtra("Orientation", mode);
        intent.putStringArrayListExtra("Fragments", selectedMedia);
        StoryMediaChooser.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //On back button pressed, return to main menu
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(StoryMediaChooser.this, MainMenu.class);
        intent.putExtra("Orientation", mode);
        StoryMediaChooser.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    //When action bar back button is pressed, call onBackPressed()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
