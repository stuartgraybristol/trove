package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.stucollyn.nfc_play.R;

import java.io.File;
import java.util.HashMap;

public class NewStorySaveMetadata extends AppCompatActivity {

    EditText storyName, tag1, tag2, tag3;
    int mode;
    File fileDirectory;
    String tag_data;
    HashMap<String,String> selectedMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        setRequestedOrientation(mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Save New Story");
        fileDirectory = (File)getIntent().getExtras().get("StoryDirectory");
        tag_data = (String)getIntent().getExtras().get("TagData");
        selectedMedia = new HashMap<String,String>();
        selectedMedia = (HashMap<String,String>)getIntent().getSerializableExtra("RecordedMedia");
        setContentView(R.layout.activity_new_story_save_metadata);
        storyName = (EditText) findViewById(R.id.enter_story_name);
        tag1 = (EditText) findViewById(R.id.enter_story_tag1);
        tag2 = (EditText) findViewById(R.id.enter_story_tag2);
        tag3 = (EditText) findViewById(R.id.enter_story_tag3);
    }

    public void Confirm (View view) {

        String storyNameString = storyName.getText().toString();
        String tag1String = tag1.getText().toString();
        String tag2String = tag2.getText().toString();
        String tag3String = tag3.getText().toString();

        Intent intent = new Intent(NewStorySaveMetadata.this, SaveSelector.class);
        intent.putExtra("StoryDirectory", fileDirectory);
        intent.putExtra("Orientation", mode);
        intent.putExtra("TagData", tag_data);
        intent.putExtra("StoryName", storyNameString);
        intent.putExtra("Tag1", tag1String);
        intent.putExtra("Tag2", tag2String);
        intent.putExtra("Tag3", tag3String);
        intent.putExtra("RecordedMedia", selectedMedia);
        NewStorySaveMetadata.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
    }

    public void Discard (View view) {

        finish();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(NewStorySaveMetadata.this, NewStoryReview.class);
        intent.putExtra("StoryDirectory", fileDirectory);
        intent.putExtra("Orientation", mode);
        intent.putExtra("TagData", tag_data);
        intent.putExtra("RecordedMedia", selectedMedia);
        NewStorySaveMetadata.this.startActivity(intent);
        overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
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

}
