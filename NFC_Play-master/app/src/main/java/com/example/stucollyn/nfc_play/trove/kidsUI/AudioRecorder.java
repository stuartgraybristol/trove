package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.stucollyn.nfc_play.trove.prototype1UI.NFCRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

/**
The AudioRecorder class is used to record, save, and control audio.
 */
public class AudioRecorder extends Application {

    private MediaRecorder mRecorder = null;

    //File Save Variables
    private static String audioFileName = null, tagFileName = null;
    File story_directory, tag_directory;
    private Context context;
    File audioFile, tagFile = null;

    //AudioRecorder constructor - initialize activity context, as well as file directory locations
    public AudioRecorder(Context context, File story_directory, File tag_directory) {
            this.context=context;
            this.story_directory = story_directory;
            this.tag_directory = tag_directory;

    }

    //Called to begin audio recording
    protected void startRecording() throws IOException {

        // Check for permissions for this device
        checkAudioRecordingPermissions();
        //Create the audio file - the file is created first and before any audio has been recorded
        createAudioFile();
        //Setup recording protocols
        setupAudioRecorder();
        //Start recording
        mRecorder.start();
    }

    //Check the current device is able to record audio
    private void checkAudioRecordingPermissions() {

        // Check for permissions
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If we don't have permissions, ask user for permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((NFCRecord) context, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    //Create the audio output file in advance of recording
    protected void createAudioFile() {

        //Give the audio file a unique id as a name
        UUID storyName = UUID.randomUUID();
        //Convert this UUID to a string
        String imageFileName = storyName.toString();

        //Create two files for the audio - create one in the story_directory and the other tag_directory. The latter is used to retrieve audio when NFC tags are scanned.
        try {

            audioFile = File.createTempFile(imageFileName, ".mp3", story_directory);
            audioFileName = audioFile.getAbsolutePath();

            if(tag_directory!=null) {
                tagFile = File.createTempFile(imageFileName, ".mp3", tag_directory);
                tagFileName = tagFile.getAbsolutePath();
            }
        }

        //If there is an error, log it
        catch (IOException e) {

            Log.i("Error", "Audio file creation failed");
        }

    }

    //Used to copy audio output file in story_directory to the tag_directory - creating a duplicate
    public static void copyFileToTag(File sourceFile, File destFile) throws IOException {

        //If the file's storage destination doesn't exist, create it
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        //If the output file itself does not already exist, create it
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        //Declare File channels
        FileChannel source = null;
        FileChannel destination = null;

        //Apply channel to copy from the source file to the destination file
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }

        //Close channels after try block has ended
        finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    //Return the audio file
    public File getAudioFile() {

        return audioFile;
    }

    //Setup new media recorder and the associated variables required. Note, audio is recorded in MPEG_4 format.
    protected void setupAudioRecorder() {

        // Save a file: path for use with ACTION_VIEW intents
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(audioFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //Prepare the media recorder
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("Error", "prepare() failed");
        }
    }

    //Stop the recording and release the resources
    protected void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        //Copy outputted audio file to the tag directory
        try {

            if(tagFile!=null) {
                copyFileToTag(audioFile, tagFile);
            }
        }
        catch (IOException e) {

        }
    }

    //Delete the recorded audio file
    protected void DiscardAudio() {

        File file = new File(audioFileName);
        file.delete();
    }

    //Return the audioFileName
    protected String getAudioFileName() {

        return audioFileName;
    }

    //Return the tagFileName
    protected String getTagFileName() {

        return tagFileName;
    }


}
