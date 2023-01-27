package com.example.stucollyn.nfc_play.trove.kidsUI;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/*
The NFCInteraction class is responsible for handling activity NFC intents - to read and write tags.
 */

public class NFCInteraction {

    //Tag variables
    Tag mytag;
    String tag_data = "";
    NdefMessage tagContents;

    //Activity variables
    Context context;
    Activity activity;
    File[] filesOnTag;

    //NFCInteraction constructor
    public NFCInteraction(Context context, Activity activity) {

        this.context = context;
        this.activity = activity;
    }

    //NFC tag read operation
    File[] read(Tag tag, PackageManager pacMan, String packageName) throws IOException, FormatException, IndexOutOfBoundsException, NullPointerException {

        //Initialize a null String
        String s = null;
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        tagContents = ndef.getNdefMessage();

        // get NDEF message details
        NdefMessage ndefMesg = ndef.getCachedNdefMessage();
        NdefRecord[] ndefRecords = ndefMesg.getRecords();
        int len = ndefRecords.length;

        //Convert byte array given by nfc tag, to a string
        byte[] mesg = null;
        String[] recTypes = new String[len];     // will contain the NDEF record types
        for (int i = 0; i < len; i++) {
            recTypes[i] = new String(ndefRecords[i].getType());
            mesg = ndefRecords[i].getPayload();
            s = new String(mesg);
        }

        s = s.substring(3); //Trim the returned string

        try {
            PackageInfo p = pacMan.getPackageInfo(packageName, 0);
            packageName = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("yourtag", "Error Package name not found ", e);
        }

        //End tag interaction
        ndef.close();

        //Find file directory in phone internal memory based on the tag data read.
        //Tag contents are always strings of the name of the story folder
        File directory = new File (context.getFilesDir() + File.separator + "Tag" + File.separator + s);

        //Based on the directory given, list the names of the contained story files and add them to the files array, which is returned by this method.
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {

            Log.i("NFC_Tag_Files_Format", files[i].getName());

        }

        filesOnTag = files;
        return filesOnTag;
    }

    //Write files to nfc tag
    private void write(Tag tag) throws IOException, FormatException {

        //Create a record to write on to the nfc tag (the tags consist of a store of memory "records")
        String fileToWrite = tag_data;
        NdefRecord[] records = {
                createRecord(fileToWrite)
        };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    //Create an nfc tag record - the tags consist of multiple memory records
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        // set status byte (see NDEF spec for actual bits)
        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        //copy langbytes and textbytes into payload
        System.arraycopy(textBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);

        return recordNFC;
    }

    //Control the nfc write functionality
    //To do: could be assimilated by write() method
    public boolean doWrite(Tag mytag, String tag_data) {

        boolean success = false;
        Toast.makeText(context, "Saving story.", Toast.LENGTH_LONG).show();
        this.mytag = mytag;
        this.tag_data = tag_data;

        try {
            if (mytag == null) {
                Toast.makeText(context, "Error Detected", Toast.LENGTH_LONG).show();
                Log.i("TagNull Exception", "");
//                Toast.makeText(this, this.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
            } else {
                //write(message.getText().toString(),mytag);
                Log.i("Reached", mytag.toString());
                write(mytag);
                success = true;
                Toast.makeText(context, "Story saved to object.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, "Error Detected", Toast.LENGTH_LONG).show();
            Log.i("IOException Exception", "");
            e.printStackTrace();
            success = false;
        } catch (FormatException e) {
            Toast.makeText(context, "Error Detected", Toast.LENGTH_LONG).show();
            Log.i("Format Exception", "");

            e.printStackTrace();
            success = false;
        }

        return success;
    }

    //Turn nfc write filters on
    void WriteModeOn(NfcAdapter adapter, PendingIntent pendingIntent, IntentFilter writeTagFilters[]){

        adapter.enableForegroundDispatch(activity, pendingIntent, writeTagFilters, null);
    }

    //Turn nfc write filters off
    void WriteModeOff(NfcAdapter adapter){
        adapter.disableForegroundDispatch(activity);
    }

    //Turn nfc read filters on
    void ReadModeOn(NfcAdapter adapter, PendingIntent pendingIntent, IntentFilter readTagFilters[]){

        adapter.enableForegroundDispatch(activity, pendingIntent, readTagFilters, null);
    }

    //Turn nfc read filters off
    void ReadModeOff(NfcAdapter adapter){
        adapter.disableForegroundDispatch(activity);
    }
}
