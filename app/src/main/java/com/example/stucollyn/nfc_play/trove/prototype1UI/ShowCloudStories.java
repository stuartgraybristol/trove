package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.example.stucollyn.nfc_play.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowCloudStories extends AppCompatActivity {

    LinearLayout linearLayout;
    int mode;
    String queryType, storyName;
    ArrayList<StoryRecord> storyRecords;
    HashMap<ImageButton, StoryRecord> buttonRecord;
    HashMap<ImageButton, String> buttonStoryType;
    HashMap<ImageButton, String> buttonStoryName;
    ImageButton[] mediaButton;
    Bundle bundle;
    Intent intent;
    int j;
    StoryRecord thisStorySend;
    TextView storyTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.trove_logo_action_bar);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Show Cloud Story");
        setContentView(R.layout.activity_show_cloud_stories);
        mode = (Integer) getIntent().getExtras().get("Orientation");
        queryType = (String) getIntent().getExtras().get("QueryType");
        storyName = (String) getIntent().getExtras().get("StoryName");
        storyRecords = (ArrayList<StoryRecord>) getIntent().getExtras().get("StoryRecordArray");
        storyTitleText = (TextView) findViewById(R.id.story_title_text);
        linearLayout = (LinearLayout) findViewById(R.id.cloud_stories_linear);
        mediaButton = new ImageButton[storyRecords.size()];
        listMediaItems();
    }

    void listMediaItems() {

        buttonRecord = new HashMap<ImageButton, StoryRecord>();
        buttonStoryType = new HashMap<ImageButton, String>();
        buttonStoryName = new HashMap<ImageButton, String>();

        for(int i=0; i<storyRecords.size(); i++) {

            intent = new Intent(getApplicationContext(), ShowCloudStoryContent.class);
            bundle = new Bundle();
            StoryRecord thisStory = storyRecords.get(i);
            mediaButton[i] = new ImageButton(this);
            mediaButton[i].setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            //                                valueTV.setText(document.getId().toString() + " => " + document.getData().toString());
            //valueTV.setText("Story ID: " + document.getId().toString() + " = " +
            //     Html.fromHtml(linkedText)+"\n");
//            mediaButton.setText(storyRecords.get(i).toString());

            if (storyRecords.get(i).getStoryType().equals("WrittenFile")) {

                mediaButton[i].setImageResource(R.drawable.written_media);
            }

            else if (storyRecords.get(i).getStoryType().equals("PictureFile")) {

                mediaButton[i].setImageResource(R.drawable.camera_media);
            }

            else if (storyRecords.get(i).getStoryType().equals("VideoFile")) {

                mediaButton[i].setImageResource(R.drawable.video_media);
            }

            else if (storyRecords.get(i).getStoryType().equals("AudioFile")) {

                mediaButton[i].setImageResource(R.drawable.audio_media);
            }

            buttonRecord.put(mediaButton[i], storyRecords.get(i));
            linearLayout.addView(mediaButton[i]);
        }

        for(int i=0; i<mediaButton.length; i++) {

            mediaButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bundle.putInt("Orientation", mode);
                    bundle.putString("StoryName", buttonRecord.get(v).getStoryName());
                    bundle.putString("StoryType", buttonRecord.get(v).getStoryType());
                    bundle.putSerializable("StoryRecordArray", buttonRecord.get(v));
                    intent.putExtras(bundle);
                    getApplicationContext().startActivity(intent);
//                    getApplicationContext().overridePendingTransition(R.anim.splash_screen_fade_in, R.anim.full_fade_out);
                }
            });
        }

        storyTitleText.setText(storyRecords.get(0).getStoryName());
    }

    @Override
    public void onBackPressed() {

        finish();
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
