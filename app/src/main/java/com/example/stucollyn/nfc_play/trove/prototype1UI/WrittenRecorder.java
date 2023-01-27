package com.example.stucollyn.nfc_play.trove.prototype1UI;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by StuCollyn on 07/06/2018.
 */

class WrittenRecorder extends Application {

    WrittenStoryFragment written_story_fragment;
    Context context;
    Activity activity;
    File story_directory;
    String written_story_fragment_text_content = "", writtenPath;

    WrittenRecorder(WrittenStoryFragment written_story_fragment, Context context, Activity activity, File story_directory) {

        this.written_story_fragment = written_story_fragment;
        this.context = context;
        this.activity = activity;
        this.story_directory = story_directory;
        written_story_fragment_text_content = written_story_fragment.getTextContent();
        Log.i("Written text 2: ", written_story_fragment_text_content);
    }

    void createWrittenFile() throws IOException {

        // Create an text file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String textFileName = "TEXT_" + timeStamp + "_";
        File storageDir;
        File textFile = null;

        if (Build.VERSION.SDK_INT >= 19) {
            storageDir = story_directory;
        } else {
            storageDir = story_directory;
        }

        textFile = File.createTempFile(textFileName, ".txt", storageDir);

            //if file doesnt exists, then create it
            if (!textFile.exists()) {
                textFile.createNewFile();
                Log.i("Woohoo", textFile.getName());
                Log.i("Woohoo1", textFile.getAbsolutePath());

            }

        try {

            FileOutputStream fOut = new FileOutputStream(textFile.getAbsoluteFile(),true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            //Write the header : in your case it is : Student class Marsk (only one time)
            myOutWriter.write(written_story_fragment_text_content);
            myOutWriter.flush();


        }

        catch (IOException e) {

            e.printStackTrace();
            Log.i("Failed", "Failed to write");


        }

        // Save a file: path for use with ACTION_VIEW intents
        writtenPath = textFile.getAbsolutePath();
        Log.i("text location", writtenPath);

    }

    public String getWrittenFilePath() {

        return writtenPath;
    }

}
